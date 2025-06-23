import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:mall_positioning_front/core/network/dio_service.dart';
import 'navigation_service.dart';

/// 用户角色枚举
enum UserRole { guardian, ward }

/// 认证服务（使用全局Dio实例）
class AuthService extends ChangeNotifier {
  final Dio _dio = DioService().dio;  // 使用全局Dio实例
  static const _storage = FlutterSecureStorage();

  // 用户状态
  UserRole _userRole = UserRole.guardian;
  bool _isLoggedIn = false;
  String? _userNickname;
  String? _userEmail;
  String? _userAvatar;
  String? _userProfile;
  bool _isProfileLoading = false;

  // 单例模式
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  // 公共属性
  UserRole get userRole => _userRole;
  bool get isLoggedIn => _isLoggedIn;
  String? get userNickname => _userNickname;
  String? get userEmail => _userEmail;
  String? get userAvatar => _userAvatar;
  String? get userProfile => _userProfile;
  bool get isProfileLoading => _isProfileLoading;

  // region ---------------------------- 初始化方法 ----------------------------
  Future<void> initialize() async {
    await _autoLogin();  // 自动登录检查
  }
  // endregion

  // region ---------------------------- Token管理 ----------------------------
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


  static Future<void> _clearTokens() async {
    await Future.wait([
      _storage.delete(key: 'jwt_token'),
      _storage.delete(key: 'refresh_token'),
    ]);
  }

  static Future<String?> getAccessToken() async {
    return await _storage.read(key: 'jwt_token');
  }
  // endregion

  // region ---------------------------- 自动登录 ----------------------------
  Future<void> _autoLogin() async {
    try {
      final token = await getAccessToken();
      if (token != null) {
        _updateLoginStatus(true);
        final isValid = await _silentCheckToken(token);
        if (!isValid) {
          await _clearTokens();
          _updateLoginStatus(false);
        } else {
          await _loadUserProfile();
        }
      }
    } catch (e) {
      await _clearTokens();
      debugPrint('自动登录失败: $e');
    }
  }

  Future<bool> _silentCheckToken(String token) async {
    try {
      final response = await _dio.get(
        '/app/auth/checkToken',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }
  // endregion

  // region ----------------------------  认证状态检查 ----------------------------
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

  // region ---------------------------- 登录/注册/注销（完全保留） ----------------------------
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
        '/app/auth/register',
        data: {
          'userAccount': userAccount,
          'userPassword': userPassword,
          'checkPassword': checkPassword,
          'email': email,
          'code': code,
          'userRole': userRole,
        },
      );
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('注册错误: ${e.toString()}');
    }
  }

  Future<void> login(String userAccount, String userPassword) async {
    try {
      final response = await _dio.post(
        '/app/auth/login',
        data: {'userAccount': userAccount, 'userPassword': userPassword},
      );
      await _handleLoginResponse(response);
    } catch (e) {
      throw Exception('登录错误: ${e.toString()}');
    }
  }

  Future<void> loginWithEmailCode(String email, String code) async {
    try {
      final response = await _dio.post(
        '/app/auth/loginByEmail',
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

  Future<void> logout() async {
    await _clearTokens();
    _updateLoginStatus(false);
  }
  // endregion

  // region ---------------------------- 用户资料操作 ----------------------------
  Future<void> _loadUserProfile() async {
    if (_isProfileLoading) return;
    _setProfileLoading(true);

    try {
      final response = await _dio.get('/app/profile/getProfile');
      final data = response.data['data'];
      _updateUserInfo(
        data['userName'],
        data['email'],
        data['userAvatar'],
        data['userProfile'],
      );
    } catch (e) {
      debugPrint('加载资料失败: $e');
    } finally {
      _setProfileLoading(false);
    }
  }

  Future<void> updateProfile({
    required String nickname,
    required String email,
    required String profile,
    String? avatarPath,
  }) async {
    _setProfileLoading(true);
    try {
      final formData = FormData.fromMap({
        'nickname': nickname,
        'email': email,
        'profile': profile,
        if (avatarPath != null)
          'avatar': await MultipartFile.fromFile(avatarPath),
      });
      await _dio.put('/app/profile', data: formData);
      await _loadUserProfile();
    } finally {
      _setProfileLoading(false);
    }
  }

  Future<void> updateEmail({
    required String email,
    required String code,
  }) async {
    try {
      final response = await _dio.put(
        '/app/profile/updateEmail',
        data: {
          'email': email,
          'code': code,
        },
      );

      debugPrint('邮箱更新响应: ${response.data}');

      // 处理返回数据
      if (response.statusCode == 200) {
        await _loadUserProfile();
      } else {
        throw Exception(response.data['message'] ?? '更新邮箱失败');
      }
    } on DioException catch (e) {
      debugPrint('Dio错误详情: ${e.response?.data}');
      throw Exception(e.response?.data?['message'] ?? '更新邮箱失败: ${e.message}');
    }
  }
  // endregion

  // region ---------------------------- 状态管理 ----------------------------
  void _updateLoginStatus(bool isLoggedIn) {
    _isLoggedIn = isLoggedIn;
    notifyListeners();
  }

  void _setProfileLoading(bool loading) {
    _isProfileLoading = loading;
    notifyListeners();
  }

  void _updateUserInfo(String? nickname, String? email, String? avatar, String? profile) {
    _userNickname = nickname;
    _userEmail = email;
    _userAvatar = avatar;
    _userProfile = profile;
    notifyListeners();
  }

  void toggleRole() {
    _userRole = _userRole == UserRole.guardian ? UserRole.ward : UserRole.guardian;
    notifyListeners();
  }
// endregion
}