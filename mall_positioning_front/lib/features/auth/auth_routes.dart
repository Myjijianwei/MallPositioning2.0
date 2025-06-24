import 'package:flutter/material.dart';
import 'package:mall_positioning_front/features/auth/pages/login_page.dart';
import 'package:mall_positioning_front/features/auth/pages/register_page.dart';
import 'package:mall_positioning_front/features/auth/pages/role_selection_page.dart';

class AuthRoutes {
  static const String login = '/login';
  static const String register = '/register';
  static const String roleSelection = '/role-selection';

  static Map<String, WidgetBuilder> getRoutes(BuildContext context) {
    return {
      login: (context) => const LoginPage(),
      roleSelection: (context) => const RoleSelectionPage(),
      // 保留原有register路由，但重定向到roleSelection
      register: (context) => const RoleSelectionPage(),
    };
  }

  // 路由守卫示例
  static Route<dynamic>? onGenerateRoute(RouteSettings settings) {
    // 可以在这里添加路由守卫逻辑
    switch (settings.name) {
      case login:
        return MaterialPageRoute(builder: (_) => const LoginPage());
      case roleSelection:
        return MaterialPageRoute(builder: (_) => const RoleSelectionPage());
      case register:
      // 处理带参数的注册路由
        final role = settings.arguments as String?;
        if (role != null && (role == 'guardian' || role == 'ward')) {
          return MaterialPageRoute(
            builder: (_) => RegisterPage(role: role),
            settings: settings,
          );
        }
        // 如果没有参数或参数无效，跳转到身份选择页
        return MaterialPageRoute(builder: (_) => const RoleSelectionPage());
      default:
        return null;
    }
  }

  // 便捷导航方法
  static void navigateToRegister(BuildContext context, {String? role}) {
    if (role != null) {
      Navigator.pushNamed(context, register, arguments: role);
    } else {
      Navigator.pushNamed(context, roleSelection);
    }
  }
}