import 'package:flutter/material.dart';
import 'package:mall_positioning_front/services/auth_service.dart';
import 'package:provider/provider.dart';

/// 路由守卫组件
/// 功能：
/// 1. 拦截未授权的路由访问
/// 2. 自动跳转到登录页
/// 3. 保留原始路由参数以便登录后跳回
class AuthRouteGuard extends NavigatorObserver {
  /// 路由入栈时触发检查
  @override
  void didPush(Route route, Route? previousRoute) {
    super.didPush(route, previousRoute);
    _checkProtectedRoute(route);
  }

  /// 路由替换时触发检查
  @override
  void didReplace({Route? newRoute, Route? oldRoute}) {
    super.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    if (newRoute != null) _checkProtectedRoute(newRoute);
  }

  /// 检查受保护路由
  Future<void> _checkProtectedRoute(Route route) async {
    final context = navigator?.context;
    if (context == null) return;

    final authService = Provider.of<AuthService>(context, listen: false);

    /// 需要认证的路由列表
    const protectedRoutes = [
      '/profile',
      '/settings',
    ];

    /// 如果访问的是受保护路由且未登录
    if (protectedRoutes.contains(route.settings.name)) {
      final isAuthenticated = await authService.checkAuthentication();
      if (!isAuthenticated) {
        /// 使用microtask确保在路由栈稳定后操作
        Future.microtask(() {
          navigator?.pop(); // 退出当前路由
          _redirectToLogin(context); // 跳转登录页
        });
      }
    }
  }

  /// 跳转到登录页并携带原始路由信息
  void _redirectToLogin(BuildContext context) {
    Navigator.pushNamed(
      context,
      '/login',
      arguments: ModalRoute.of(context)?.settings.name, // 保存原始路由
    );
  }
}