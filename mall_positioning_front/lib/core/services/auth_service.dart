import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'navigation_service.dart';

/// 用户角色枚举
/// - guardian: 监护人
/// - ward: 被监护人
enum UserRole { guardian, ward }

/// 认证服务类（核心状态管理）
/// 管理用户登录状态、Token、用户信息及API请求
class AuthService extends ChangeNotifier {
  // region ----------------------------- 存储与HTTP客户端 -----------------------------
  /// 安全存储实例（用于存储JWT Token）
  static const _storage = FlutterSecureStorage();

  /// Dio HTTP客户端（全局单例）
  /// 配置基础API地址和超时时间
  static final Dio _dio = Dio(BaseOptions(
    baseUrl: 'http://localhost:8001/api/app',
    connectTimeout: const Duration(seconds: 15),
  ));

  // endregion

  // region ----------------------------- 用户状态属性 -----------------------------
  /// 当前用户角色（默认监护人）
  UserRole _userRole = UserRole.guardian;

  /// 登录状态标识
  bool _isLoggedIn = false;

  /// 用户昵称
  String? _userNickname;

  /// 用户邮箱
  String? _userEmail;

  /// 用户头像URL
  String? _userAvatar;

  /// 资料加载状态
  bool _isProfileLoading = false;

  // endregion

  // region ----------------------------- 公共访问属性 -----------------------------
  UserRole get userRole => _userRole;
  bool get isLoggedIn => _isLoggedIn;
  String? get userNickname => _userNickname;
  String? get userEmail => _userEmail;
  String? get userAvatar => _userAvatar;
  bool get isProfileLoading => _isProfileLoading;

  // endregion

  // region ----------------------------- 初始化方法 -----------------------------
  /// 初始化Dio拦截器
  /// 必须在使用前调用（通常在main.dart中初始化）
  static void initialize() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: _addAuthHeader,  // 请求拦截器
      onError: _handleAuthError,  // 错误拦截器
    ));
  }
  // endregion

  // region ----------------------------- 拦截器方法 -----------------------------
  /// 请求拦截器：自动添加Authorization头
  static Future<void> _addAuthHeader(
      RequestOptions options,
      RequestInterceptorHandler handler,
      ) async {
    // 跳过公开API的Token添加
    if (!_isPublicAPI(options.path)) {
      final token = await _storage.read(key: 'jwt_token');
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  /// 错误拦截器：处理401未授权错误
  static Future<void> _handleAuthError(
      DioError error,
      ErrorInterceptorHandler handler,
      ) async {
    if (error.response?.statusCode == 401) {
      try {
        // 尝试刷新Token
        final newToken = await _refreshToken();
        error.requestOptions.headers['Authorization'] = 'Bearer $newToken';
        // 使用新Token重试请求
        return handler.resolve(await _dio.fetch(error.requestOptions));
      } catch (e) {
        // 刷新失败时清除Token并跳转登录页
        await _storage.delete(key: 'jwt_token');

        // 优先使用请求上下文的导航
        final context = error.requestOptions.extra['context'] as BuildContext?;
        if (context != null && context.mounted) {
          Navigator.of(context).pushReplacementNamed('/login');
        } else {
          // 回退到全局导航
          NavigationService.goToLogin();
        }
      }
    }
    handler.next(error);
  }

  /// 判断是否为公开API（不需要Token的接口）
  static bool _isPublicAPI(String path) {
    return const [
      '/auth/login',
      '/auth/register',
      '/auth/refresh',
      '/auth/loginByEmail'
    ].any(path.contains);
  }
  // endregion

  // region ----------------------------- Token管理 -----------------------------
  /// 刷新Access Token
  static Future<String> _refreshToken() async {
    final refreshToken = await _storage.read(key: 'refresh_token');
    final response = await _dio.post('/auth/refresh', data: {
      'refresh_token': refreshToken,
    });
    // 存储新Token
    await _storage.write(key: 'jwt_token', value: response.data['token']);
    return response.data['token'];
  }
  // endregion

  // region ----------------------------- 认证状态检查 -----------------------------
  /// 检查当前认证状态
  /// 返回值：true表示已认证，false表示未认证
  Future<bool> checkAuthentication() async {
    final token = await _storage.read(key: 'jwt_token');
    if (token == null) {
      _updateLoginStatus(false);
      return false;
    }

    try {
      final response = await _dio.get('/auth/checkToken');
      _updateLoginStatus(response.statusCode == 200);
      if (_isLoggedIn) await _loadUserProfile(); // 自动加载用户资料
      return _isLoggedIn;
    } catch (e) {
      _updateLoginStatus(false);
      return false;
    }
  }
  // endregion

  // region ----------------------------- 用户资料操作 -----------------------------
  /// 加载用户资料
  Future<void> _loadUserProfile() async {
    if (_isProfileLoading) return;
    _setProfileLoading(true);

    try {
      final response = await _dio.get('/profile');
      final data = response.data['data'];
      _updateUserInfo(
        data['nickname'],
        data['email'],
        data['avatarUrl'],
      );
    } catch (e) {
      debugPrint('加载用户资料失败: $e');
    } finally {
      _setProfileLoading(false);
    }
  }

  /// 更新用户资料（含头像上传）
  Future<void> updateProfile({
    required String nickname,
    required String email,
    String? avatarPath,
  }) async {
    _setProfileLoading(true);

    try {
      final request = _dio.put(
        '/profile',
        data: FormData.fromMap({
          'nickname': nickname,
          'email': email,
          if (avatarPath != null)
            'avatar': await MultipartFile.fromFile(avatarPath),
        }),
      );
      await _loadUserProfile(); // 刷新本地资料
    } finally {
      _setProfileLoading(false);
    }
  }
  // endregion

  // region ----------------------------- 登录/注销操作 -----------------------------
  /// 账号密码注册
  Future<void> register({
    required String userAccount,
    required String userPassword,
    required String checkPassword,
    required String email,
    required String code,
    required String userRole,
  }) async {
    try {
      final response = await _dio.post('/auth/register', data: {
        'userAccount': userAccount,
        'userPassword': userPassword,
        'checkPassword': checkPassword,
        'email': email,
        'code': code,
        'userRole': userRole,
      });

      if (response.statusCode != 200) {
        throw Exception(response.data['message'] ?? '注册失败');
      }

      // 注册成功后自动登录
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('注册错误: ${e.toString()}');
    }
  }
  /// 账号密码登录
  Future<void> login(String userAccount, String userPassword) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'userAccount': userAccount,
        'userPassword': userPassword,
      });
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('登录错误: ${e.toString()}');
    }
  }

  /// 邮箱验证码登录
  Future<void> loginWithEmailCode(String email, String code) async {
    try {
      final response = await _dio.post('/auth/loginByEmail', data: {
        'email': email,
        'code': code,
      });
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('邮箱登录错误: ${e.toString()}');
    }
  }

  /// 处理登录响应（存储Token）
  Future<void> _handleLoginResponse(Response response) async {
    if (response.statusCode == 200) {
      await _storage.write(key: 'jwt_token', value: response.data['token']);
      await _storage.write(key: 'refresh_token', value: response.data['refresh_token']);
      _updateLoginStatus(true);
    } else {
      throw Exception(response.data['message'] ?? '登录失败');
    }
  }

  /// 注销登录
  Future<void> logout() async {
    await _storage.delete(key: 'jwt_token');
    await _storage.delete(key: 'refresh_token');
    _updateLoginStatus(false);
  }
  // endregion

  // region ----------------------------- 状态更新方法 -----------------------------
  /// 更新登录状态（内部方法）
  void _updateLoginStatus(bool isLoggedIn) {
    _isLoggedIn = isLoggedIn;
    notifyListeners(); // 通知监听者状态变化
  }

  /// 设置资料加载状态（内部方法）
  void _setProfileLoading(bool loading) {
    _isProfileLoading = loading;
    notifyListeners();
  }

  /// 更新用户信息（内部方法）
  void _updateUserInfo(String? nickname, String? email, String? avatar) {
    _userNickname = nickname;
    _userEmail = email;
    _userAvatar = avatar;
    notifyListeners();
  }

  void toggleRole() {
    _userRole = _userRole == UserRole.guardian ? UserRole.ward : UserRole.guardian;
    notifyListeners(); // 通知监听者状态变化
  }
// endregion
}