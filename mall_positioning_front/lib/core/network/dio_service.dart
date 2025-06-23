import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class DioService {
  static final DioService _instance = DioService._internal();
  late final Dio dio;

  // 安全存储
  static const _storage = FlutterSecureStorage();

  factory DioService() => _instance;

  DioService._internal() {
    // 基础配置
    dio = Dio(BaseOptions(
      // baseUrl: 'http://localhost:8001/api', // 基础路径
      baseUrl: 'http://10.12.34.203:8001/api', // 替换为实际的IP地址
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 15),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ));

    // 添加拦截器
    dio.interceptors.addAll([
      _authInterceptor(),
      _loggingInterceptor(),
      _errorInterceptor(),
    ]);
  }

  // 认证拦截器
  Interceptor _authInterceptor() {
    return InterceptorsWrapper(
      onRequest: (options, handler) async {
        // 跳过公开API
        if (_isPublicAPI(options.path)) {
          return handler.next(options);
        }

        // 添加Token
        final token = await _storage.read(key: 'jwt_token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }

        return handler.next(options);
      },
      onError: (error, handler) async {
        // 处理401错误
        if (error.response?.statusCode == 401) {
          try {
            final newToken = await _refreshToken();
            error.requestOptions.headers['Authorization'] = 'Bearer $newToken';
            return handler.resolve(await dio.fetch(error.requestOptions));
          } catch (e) {
            await _clearTokens();
            // 这里可以添加全局跳转登录页的逻辑
            return handler.reject(error);
          }
        }
        return handler.next(error);
      },
    );
  }

  // 日志拦截器
  Interceptor _loggingInterceptor() {
    return InterceptorsWrapper(
      onRequest: (options, handler) {
        debugPrint('REQUEST[${options.method}] => PATH: ${options.path}');
        return handler.next(options);
      },
      onResponse: (response, handler) {
        debugPrint(
            'RESPONSE[${response.statusCode}] => PATH: ${response.requestOptions.path}');
        return handler.next(response);
      },
      onError: (error, handler) {
        debugPrint(
            'ERROR[${error.response?.statusCode}] => PATH: ${error.requestOptions.path}');
        return handler.next(error);
      },
    );
  }

  // 错误拦截器
  Interceptor _errorInterceptor() {
    return InterceptorsWrapper(
      onError: (error, handler) {
        // 统一错误格式处理
        if (error.response != null) {
          error = DioException(
            requestOptions: error.requestOptions,
            response: error.response,
            type: DioExceptionType.badResponse,
            error: error.response?.data?['message'] ?? error.message,
          );
        }
        return handler.next(error);
      },
    );
  }

  // Token刷新
  static Future<String> _refreshToken() async {
    final refreshToken = await _storage.read(key: 'refresh_token');
    if (refreshToken == null) throw Exception('No refresh token');

    final dio = Dio();
    final response = await dio.post(
      '/auth/refresh',
      data: {'refresh_token': refreshToken},
    );

    await _persistTokens(
      accessToken: response.data['access_token'],
      refreshToken: response.data['refresh_token'],
    );
    return response.data['access_token'];
  }

  // Token持久化
  static Future<void> _persistTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    await Future.wait([
      _storage.write(key: 'jwt_token', value: accessToken),
      _storage.write(key: 'refresh_token', value: refreshToken),
    ]);
  }

  // 清理Token
  static Future<void> _clearTokens() async {
    await Future.wait([
      _storage.delete(key: 'jwt_token'),
      _storage.delete(key: 'refresh_token'),
    ]);
  }

  // 判断公开API
  static bool _isPublicAPI(String path) {
    return const [
      '/auth/login',
      '/auth/register',
      '/auth/refresh',
      '/auth/loginByEmail',

    ].any((p) => path.startsWith(p));
  }
}