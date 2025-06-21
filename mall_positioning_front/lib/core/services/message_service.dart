import 'package:http/http.dart' as http;

/// 短信/邮件服务
/// 负责验证码发送等通信功能
class MsmService {
  final String _baseUrl = 'http://localhost:8001/api/msm';

  /// 发送邮箱验证码
  /// [email] 目标邮箱地址
  Future<void> sendEmailCode(String email) async {
    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/sendEmail/$email'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode != 200) {
        throw Exception('发送失败: ${response.body}');
      }
    } catch (e) {
      throw Exception('网络错误: ${e.toString()}');
    }
  }
}