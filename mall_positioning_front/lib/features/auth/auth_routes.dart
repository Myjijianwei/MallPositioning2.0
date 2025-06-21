import 'package:flutter/material.dart';
import 'package:mall_positioning_front/features/auth/pages/login_page.dart';
import 'package:mall_positioning_front/features/auth/pages/register_page.dart';

class AuthRoutes {
  static const String login = '/login';
  static const String register = '/register';

  static Map<String, WidgetBuilder> getRoutes(BuildContext context) {
    return {
      login: (context) => const LoginPage(),
      register: (context) => const RegisterPage(),
    };
  }

  // 路由守卫示例
  static Route<dynamic>? onGenerateRoute(RouteSettings settings) {
    // 可以在这里添加路由守卫逻辑
    switch (settings.name) {
      case login:
        return MaterialPageRoute(builder: (_) => const LoginPage());
      case register:
        return MaterialPageRoute(builder: (_) => const RegisterPage());
      default:
        return null;
    }
  }
}