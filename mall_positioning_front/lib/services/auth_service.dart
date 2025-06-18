import 'dart:convert';

import '../models/user.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;


class AuthService {
  final String _baseUrl = 'http://localhost:8001/api/app/user';
  final storage = const FlutterSecureStorage();

  // 注册用户
  Future<User> register(String email, String password, String name) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': email,
          'password': password,
          'name': name,
        }),
      );

      if (response.statusCode == 201) {
        final data = jsonDecode(response.body);
        await _saveUserToken(data['token']);
        return User.fromJson(data);
      } else {
        throw Exception('注册失败: ${response.body}');
      }
    } catch (e) {
      throw Exception('注册错误: $e');
    }
  }

  // 用户登录
  Future<void> login(String userAccount, String userPassword) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'userAccount':userAccount,
          'userPassword': userPassword,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        await _saveUserToken(data['data']);
        await exitGuestMode(); // 登录后退出演示模式
      } else {
        throw Exception('登录失败: ${response.body}');
      }
    } catch (e) {
      throw Exception('登录错误: $e');
    }
  }

  // 保存令牌到安全存储
  Future<void> _saveUserToken(String token) async {
    await storage.write(key: 'jwt_token', value: token);
  }

  // 获取当前用户令牌
  Future<String?> getToken() async {
    return await storage.read(key: 'jwt_token');
  }

  // 检查用户是否已登录
  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null;
  }

  // 退出登录
  Future<void> logout() async {
    await storage.delete(key: 'jwt_token');
  }

  // 邮箱发送验证码进行登录
  Future<void> loginWithEmailCode(String email, String code) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/loginByEmail'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': email,
          'code': code,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        await _saveUserToken(data['data']);
      } else {
        throw Exception('邮箱验证码登录失败: ${response.body}');
      }
    } catch (e) {
      throw Exception('邮箱验证码登录错误: $e');
    }
  }

  // 游客模式标识
  Future<void> setGuestMode() async {
    await storage.write(key: 'is_guest', value: 'true');
  }

  Future<bool> isGuestMode() async {
    return await storage.read(key: 'is_guest') == 'true';
  }

  Future<void> exitGuestMode() async {
    await storage.delete(key: '_guestKey');
  }

  // 获取演示数据
  Future<Map<String, dynamic>> getDemoData(String type) async {
    // 返回预设的演示数据
    switch (type) {
      case 'device':
        return {
          'id': 'demo-device-001',
          'name': '演示设备',
          'status': 'online',
          'battery': 85,
          'lastUpdate': DateTime.now().toIso8601String(),
          'location': {
            'lat': 39.9042,
            'lng': 116.4074,
            'accuracy': 15,
          }
        };
      case 'fence':
        return {
          'fences': [
            {
              'id': 'demo-fence-001',
              'name': '学校安全区(演示)',
              'points': [
                {'lat': 39.9030, 'lng': 116.4060},
                {'lat': 39.9030, 'lng': 116.4080},
                {'lat': 39.9050, 'lng': 116.4080},
                {'lat': 39.9050, 'lng': 116.4060},
              ],
              'alerts': 2,
            }
          ]
        };
    // 其他演示数据类型...
      default:
        return {};
    }
  }


}
