import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../services/auth_service.dart';

class DemoRouteGuard extends NavigatorObserver {
  final AuthService authService;

  DemoRouteGuard({required this.authService});

  @override
  void didPush(Route route, Route? previousRoute) {
    super.didPush(route, previousRoute);
    _checkDemoRestrictions(route);
  }

  void _checkDemoRestrictions(Route route) async {
    final context = navigator?.context;
    if (context == null) return;

    final authService = Provider.of<AuthService>(context, listen: false);
    final isGuest = await authService.isGuestMode();

    if (isGuest && _isRestrictedRoute(route.settings.name)) {
      _showUpgradePrompt(context);
      navigator?.pop(); // 返回上一页
    }
  }

  bool _isRestrictedRoute(String? routeName) {
    const restrictedRoutes = [
      '/device/bind',
      '/fence/create',
      '/alerts/handle',
      // 其他需要真实账号的功能路由...
    ];
    return restrictedRoutes.contains(routeName);
  }

  void _showUpgradePrompt(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('功能受限'),
        content: Text('此功能需要注册账号后才能使用，是否立即注册？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('稍后'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.pushReplacementNamed(context, '/register');
            },
            child: Text('立即注册'),
          ),
        ],
      ),
    );
  }
}