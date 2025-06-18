import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

class MsmService{
  final String _baseUrl = 'http://localhost:8001/api/msm';
  final storage = const FlutterSecureStorage();

  // 发送邮箱验证码
  Future<void> sendEmailCode(String email) async {
    try {
      final uri = Uri.parse('$_baseUrl/sendEmail/$email');
      final response = await http.get(
        uri,
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode != 200) {
        throw Exception('发送验证码失败: ${response.body}');
      }
    } catch (e) {
      throw Exception('发送验证码错误: $e');
    }
  }
}