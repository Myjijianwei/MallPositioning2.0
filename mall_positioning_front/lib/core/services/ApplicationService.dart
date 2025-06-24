// lib/core/services/ApplicationService.dart
import 'package:dio/dio.dart';
import '../../data/models/Application.dart';
import '../network/dio_service.dart';

class ApplicationService {
  final Dio _dio = DioService().dio;

  Future<Application> submitApplication(String wardDeviceId) async {
    try {
      final response = await _dio.post(
        '/apply/submit_app?wardDeviceId=$wardDeviceId',
      );
      return Application.fromJson(response.data);
    } on DioException catch (e) {
      throw Exception(e.response?.data?['message'] ?? '提交申请失败');
    }
  }

  Future<List<Application>> getApplications() async {
    try {
      final response = await _dio.post('/apply/getApplications_app');

      // 处理多种可能的响应格式
      if (response.data is List) {
        return (response.data as List)
            .map((json) => Application.fromJson(json))
            .toList();
      } else if (response.data is Map) {
        final data = response.data['data'] ?? response.data['items'];
        if (data is List) {
          return data.map((json) => Application.fromJson(json)).toList();
        }
      }
      throw Exception('返回数据格式不正确');
    } on DioException catch (e) {
      throw Exception(e.response?.data?['message'] ?? '加载申请记录失败');
    }
  }

  Future<bool> confirmApplication(int applicationId, bool isApproved) async {
    try {
      final response = await _dio.post(
        '/apply/confirm',
        data: {
          'notificationId': applicationId,
          'isApproved': isApproved,
        },
      );
      return response.data['success'] ?? false;
    } on DioException catch (e) {
      throw Exception(e.response?.data?['message'] ?? '确认申请失败');
    }
  }
}