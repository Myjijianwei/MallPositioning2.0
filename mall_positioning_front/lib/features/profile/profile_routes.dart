import 'package:flutter/material.dart';
import 'package:mall_positioning_front/features/profile/page/email_update_page.dart';
import 'package:mall_positioning_front/features/profile/page/profile_info_page.dart';

class ProfileRoutes {
  static const String profile_info = '/profile_info';
  static const String email_update = '/email_update';

  static Map<String, WidgetBuilder> getRoutes(BuildContext context) {
    return {
      profile_info: (context) => const ProfileInfoPage(),
      email_update: (context) => const EmailUpdatePage(),
    };
  }

  // 路由守卫示例
  static Route<dynamic>? onGenerateRoute(RouteSettings settings) {
    // 可以在这里添加路由守卫逻辑
    switch (settings.name) {
      case profile_info:
        return MaterialPageRoute(builder: (_) => const ProfileInfoPage());
      case email_update:
        return MaterialPageRoute(builder: (_) => const EmailUpdatePage());
      default:
        return null;
    }
  }
}