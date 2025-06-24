import 'package:flutter/material.dart';
import 'package:mall_positioning_front/features/application/page/guardian_bind_requests_page.dart';
import 'package:mall_positioning_front/features/application/page/ward_device_apply_page.dart';
import 'package:mall_positioning_front/features/profile/page/email_update_page.dart';
import 'package:mall_positioning_front/features/profile/page/profile_info_page.dart';

class ApplyRoutes {
  static const String ward_device_apply = '/ward_device_apply';
  static const String guardian_bind_requests_page = '/guardian_bind_requests_page';

  static Map<String, WidgetBuilder> getRoutes(BuildContext context) {
    return {
      ward_device_apply: (context) => const WardDeviceApplyPage(),
      guardian_bind_requests_page: (context) => const GuardianBindRequestsPage(),
    };
  }

  // 路由守卫示例
  static Route<dynamic>? onGenerateRoute(RouteSettings settings) {
    // 可以在这里添加路由守卫逻辑
    switch (settings.name) {
      case ward_device_apply:
        return MaterialPageRoute(builder: (_) => const WardDeviceApplyPage());
      case guardian_bind_requests_page:
        return MaterialPageRoute(builder: (_) => const GuardianBindRequestsPage());
      default:
        return null;
    }
  }
}