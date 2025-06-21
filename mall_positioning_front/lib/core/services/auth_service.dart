import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'navigation_service.dart';

/// 用户角色枚举
/// - guardian: 监护人角色
/// - ward: 被监护人角色
enum UserRole { guardian, ward }

/// 认证服务类（单例模式）
/// 负责处理用户认证、Token管理、用户资料等核心功能
class AuthService extends ChangeNotifier {
  // region ---------------------------- 1. 安全存储配置 ----------------------------
  /// 使用FlutterSecureStorage进行加密存储
  /// Android平台启用加密SharedPreferences
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(
      encryptedSharedPreferences: true, // 启用Android加密存储
      sharedPreferencesName: 'AuthStorage', // 独立存储空间名称
    ),
  );

  // endregion

  // region ---------------------------- 2. HTTP客户端配置 ----------------------------
  /// Dio实例配置
  /// - baseUrl: 后端API基础地址
  /// - connectTimeout: 连接超时15秒
  static final Dio _dio = Dio(
    BaseOptions(
      baseUrl: 'http://localhost:8001/api/app', // 替换为实际API地址
      connectTimeout: const Duration(seconds: 15),
    ),
  );

  // endregion

  // region ---------------------------- 3. 用户状态管理 ----------------------------
  UserRole _userRole = UserRole.guardian; // 当前用户角色
  bool _isLoggedIn = false; // 登录状态标志
  String? _userNickname; // 用户昵称
  String? _userEmail; // 用户邮箱
  String? _userAvatar; // 用户头像URL
  bool _isProfileLoading = false; // 资料加载状态
  // endregion

  // region ---------------------------- 4. 单例模式实现 ----------------------------
  /// 单例实例
  static final AuthService _instance = AuthService._internal();

  /// 工厂构造函数
  factory AuthService() => _instance;

  /// 私有构造函数
  AuthService._internal();

  // endregion

  // region ---------------------------- 5. 公共属性访问器 ----------------------------
  UserRole get userRole => _userRole;

  bool get isLoggedIn => _isLoggedIn;

  String? get userNickname => _userNickname;

  String? get userEmail => _userEmail;

  String? get userAvatar => _userAvatar;

  bool get isProfileLoading => _isProfileLoading;

  // endregion

  // region ---------------------------- 6. 初始化方法 ----------------------------
  /// 初始化认证服务
  /// 1. 添加请求/响应拦截器
  /// 2. 尝试自动登录
  Future<void> initialize() async {
    // 添加Dio拦截器
    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: _addAuthHeader, // 请求拦截器
        onError: _handleAuthError, // 错误拦截器
      ),
    );

    // 尝试自动登录
    await _autoLogin();
  }

  // endregion

  // region ---------------------------- 7. 自动登录系统 ----------------------------
  /// 自动登录流程：
  /// 1. 检查本地是否存在Token
  /// 2. 验证Token有效性
  /// 3. 有效则加载用户资料，无效则清理Token
  Future<void> _autoLogin() async {
    try {
      final token = await getAccessToken();
      if (token != null) {
        // 先更新UI状态为已登录（避免跳转到登录页）
        _updateLoginStatus(true);

        // 静默验证Token（不显示错误提示）
        final isValid = await _silentCheckToken(token);
        if (!isValid) {
          // Token无效时清理本地存储
          await _clearTokens();
          _updateLoginStatus(false);
        } else {
          // Token有效时加载用户资料
          await _loadUserProfile();
        }
      }
    } catch (e) {
      // 任何异常都清理Token保证安全
      await _clearTokens();
      debugPrint('自动登录失败: $e');
    }
  }

  /// 静默Token验证（不抛出异常）
  /// 用于后台自动检查Token有效性
  Future<bool> _silentCheckToken(String token) async {
    try {
      final response = await _dio.get(
        '/auth/checkToken',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  // endregion

  // region ---------------------------- 8. 拦截器实现 ----------------------------
  /// 请求拦截器：自动添加Authorization头
  static Future<void> _addAuthHeader(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    // 排除公开API（登录/注册等）
    if (!_isPublicAPI(options.path)) {
      final token = await getAccessToken();
      if (token != null) {
        options.headers['Authorization'] = 'Bearer $token';
      }
    }
    handler.next(options);
  }

  /// 错误拦截器：处理401未授权错误
  static Future<void> _handleAuthError(
    DioException error,
    ErrorInterceptorHandler handler,
  ) async {
    // 仅处理401错误
    if (error.response?.statusCode == 401) {
      try {
        // 尝试刷新Token
        final newToken = await _refreshToken();

        // 使用新Token重试请求
        error.requestOptions.headers['Authorization'] = 'Bearer $newToken';
        return handler.resolve(await _dio.fetch(error.requestOptions));
      } catch (e) {
        // 刷新失败时清理Token并跳转登录页
        await _clearTokens();
        NavigationService.goToLogin();
      }
    }
    handler.next(error);
  }

  /// 判断是否为公开API（不需要认证的接口）
  static bool _isPublicAPI(String path) => const [
    '/auth/login',
    '/auth/register',
    '/auth/refresh',
    '/auth/loginByEmail',
  ].any(path.contains);

  // endregion

  // region ---------------------------- 9. Token管理 ----------------------------
  /// 持久化Token（原子操作）
  /// 同时存储access_token和refresh_token
  static Future<void> _persistTokens({
    required String accessToken,
    String? refreshToken,
  }) async {
    try {
      await Future.wait([
        _storage.write(key: 'jwt_token', value: accessToken),
        if (refreshToken != null)
          _storage.write(key: 'refresh_token', value: refreshToken),
      ]);
    } catch (e) {
      // 存储失败时清理可能已写入的部分数据
      await _clearTokens();
      rethrow;
    }
  }

  /// 清理Token（原子操作）
  static Future<void> _clearTokens() async {
    await Future.wait([
      _storage.delete(key: 'jwt_token'),
      _storage.delete(key: 'refresh_token'),
    ]);
  }

  /// 获取Access Token
  static Future<String?> getAccessToken() async {
    try {
      return await _storage.read(key: 'jwt_token');
    } catch (e) {
      debugPrint('读取Token失败: $e');
      return null;
    }
  }

  /// 刷新Token
  static Future<String> _refreshToken() async {
    final refreshToken = await _storage.read(key: 'refresh_token');
    if (refreshToken == null) throw Exception('无有效Refresh Token');

    final response = await _dio.post(
      '/auth/refreshToken',
      data: {'refresh_token': refreshToken},
    );

    // 存储新Token
    await _persistTokens(accessToken: response.data['data']);
    return response.data['data'];
  }

  // endregion

  // region ---------------------------- 10. 认证状态检查 ----------------------------
  /// 检查认证状态（带UI提示）
  /// 用于需要显示加载状态的场景
  Future<bool> checkAuthentication() async {
    final token = await getAccessToken();
    if (token == null) {
      _updateLoginStatus(false);
      return false;
    }

    try {
      final isValid = await _silentCheckToken(token);
      _updateLoginStatus(isValid);
      if (isValid) await _loadUserProfile();
      return isValid;
    } catch (e) {
      _updateLoginStatus(false);
      return false;
    }
  }

  // endregion

  // region ---------------------------- 11. 用户资料操作 ----------------------------
  /// 加载用户资料
  Future<void> _loadUserProfile() async {
    if (_isProfileLoading) return;
    _setProfileLoading(true);

    try {
      final response = await _dio.get('/profile');
      final data = response.data['data'];
      _updateUserInfo(data['nickname'], data['email'], data['avatarUrl']);
    } catch (e) {
      debugPrint('加载资料失败: $e');
    } finally {
      _setProfileLoading(false);
    }
  }

  /// 更新用户资料
  /// 支持更新昵称、邮箱和头像
  Future<void> updateProfile({
    required String nickname,
    required String email,
    String? avatarPath,
  }) async {
    _setProfileLoading(true);
    try {
      // 使用FormData上传文件
      await _dio.put(
        '/profile',
        data: FormData.fromMap({
          'nickname': nickname,
          'email': email,
          if (avatarPath != null)
            'avatar': await MultipartFile.fromFile(avatarPath),
        }),
      );
      await _loadUserProfile(); // 更新后重新加载资料
    } finally {
      _setProfileLoading(false);
    }
  }

  // endregion

  // region ---------------------------- 12. 登录/注册/注销 ----------------------------
  /// 用户注册
  Future<void> register({
    required String userAccount,
    required String userPassword,
    required String checkPassword,
    required String email,
    required String code,
    required String userRole,
  }) async {
    try {
      final response = await _dio.post(
        '/auth/register',
        data: {
          'userAccount': userAccount,
          'userPassword': userPassword,
          'checkPassword': checkPassword,
          'email': email,
          'code': code,
          'userRole': userRole,
        },
      );

      if (response.statusCode != 200) {
        throw Exception(response.data['message'] ?? '注册失败');
      }
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('注册错误: ${e.toString()}');
    }
  }

  /// 账号密码登录
  Future<void> login(String userAccount, String userPassword) async {
    try {
      final response = await _dio.post(
        '/auth/login',
        data: {'userAccount': userAccount, 'userPassword': userPassword},
      );
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('登录错误: ${e.toString()}');
    }
  }

  /// 邮箱验证码登录
  Future<void> loginWithEmailCode(String email, String code) async {
    try {
      final response = await _dio.post(
        '/auth/loginByEmail',
        data: {'email': email, 'code': code},
      );
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('邮箱登录错误: ${e.toString()}');
    }
  }

  /// 处理登录响应（统一处理Token存储）
  Future<void> _handleLoginResponse(Response response) async {
    try {
      if (response.statusCode == 200) {
        final data = response.data as Map<String, dynamic>;
        // 持久化Token
        await _persistTokens(
          accessToken: data['data'] as String,
          refreshToken: data['refresh_token'] as String?,
        );

        // 更新状态
        _updateLoginStatus(true);
        await _loadUserProfile(); // 立即加载用户资料
      } else {
        throw Exception(response.data['message'] ?? '登录失败');
      }
    } catch (e) {
      await _clearTokens();
      rethrow;
    }
  }

  /// 用户注销
  Future<void> logout() async {
    await _clearTokens();
    _updateLoginStatus(false);
  }

  // endregion

  // region ---------------------------- 13. 状态更新方法 ----------------------------
  /// 更新登录状态
  void _updateLoginStatus(bool isLoggedIn) {
    _isLoggedIn = isLoggedIn;
    notifyListeners(); // 通知监听者状态变化
  }

  /// 设置资料加载状态
  void _setProfileLoading(bool loading) {
    _isProfileLoading = loading;
    notifyListeners();
  }

  /// 更新用户信息
  void _updateUserInfo(String? nickname, String? email, String? avatar) {
    _userNickname = nickname;
    _userEmail = email;
    _userAvatar = avatar;
    notifyListeners();
  }

  /// 切换用户角色
  void toggleRole() {
    _userRole = _userRole == UserRole.guardian
        ? UserRole.ward
        : UserRole.guardian;
    notifyListeners();
  }

  // endregion
}
