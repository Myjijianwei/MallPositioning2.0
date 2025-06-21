import 'package:flutter/cupertino.dart';

/// 全局导航服务（单例模式）
class NavigationService {
  // 全局导航键
  static final GlobalKey<NavigatorState> navigatorKey =
  GlobalKey<NavigatorState>();

  /// 获取当前上下文
  static BuildContext? get context => navigatorKey.currentContext;

  /// 普通路由跳转
  static Future<T?> navigateTo<T>(String routeName) {
    return navigatorKey.currentState!.pushNamed<T>(routeName);
  }

  /// 跳转登录页（替换当前路由）
  static void goToLogin() {
    navigatorKey.currentState?.pushReplacementNamed('/login');
  }
}