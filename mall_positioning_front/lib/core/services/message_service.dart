import 'package:dio/dio.dart';
import 'package:mall_positioning_front/core/network/dio_service.dart';

/// 短信/邮件服务
/// 负责验证码发送等通信功能
class MsmService {
  final Dio _dio;

  MsmService() : _dio = DioService().dio;

  /// 发送邮箱验证码
  /// [email] 目标邮箱地址
  Future<void> sendEmailCode(String email) async {
    try {
      final response = await _dio.get(
        '/msm/sendEmail/$email',
      );

      if (response.statusCode != 200) {
        throw Exception('发送失败: ${response.data}');
      }
    } catch (e) {
      throw Exception('网络错误: ${e.toString()}');
    }
  }
  Future<void> verifyEmailCode({
    required String email,
    required String code,
  }) async {
    try {
      final response = await _dio.post(
        '/msm/verifyEmail',
        data: {'email': email, 'code': code},
      );

      if (response.statusCode != 200) {
        throw Exception(response.data['message'] ?? '验证失败');
      }
    } catch (e) {
      throw Exception('验证错误: ${e.toString()}');
    }
  }

}