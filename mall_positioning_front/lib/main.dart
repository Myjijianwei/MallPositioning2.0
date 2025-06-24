import 'package:flutter/material.dart';
import 'package:mall_positioning_front/core/network/dio_service.dart';
import 'package:mall_positioning_front/features/application/page/ward_device_apply_page.dart';
import 'package:provider/provider.dart';
import 'core/services/auth_service.dart';
import 'core/services/navigation_service.dart';
import 'core/utils/auth_route_guard.dart';
import 'features/application/apply_routes.dart';
import 'features/application/page/guardian_bind_requests_page.dart';
import 'features/auth/auth_routes.dart';
import 'features/auth/pages/login_page.dart';
import 'features/auth/pages/register_page.dart';
import 'features/auth/pages/role_selection_page.dart';
import 'features/home/home_page.dart';
import 'features/profile/page/email_update_page.dart';
import 'features/profile/page/profile_info_page.dart';
import 'features/profile/profile_routes.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 初始化AuthService并自动检查登录状态
  final authService = AuthService();
  await authService.initialize();
  // 初始化全局Dio（会自动配置拦截器）
  DioService();

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => authService),
        // 其他 Provider
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '商城防走失APP',
      debugShowCheckedModeBanner: false,
      theme: _buildAppTheme(),
      initialRoute: '/',
      routes: _buildAppRoutes(),
      navigatorKey: NavigationService.navigatorKey,
      navigatorObservers: [AuthRouteGuard()],
      onGenerateRoute: (settings) => MaterialPageRoute(
        builder: (_) => Scaffold(
          appBar: AppBar(title: const Text('页面不存在')),
          body: const Center(child: Text('您访问的页面不存在')),
        ),
      ),
    );
  }

  ThemeData _buildAppTheme() {
    return ThemeData(
      colorScheme: ColorScheme.light(
        primary: Colors.grey[800]!,
        secondary: Colors.blueGrey[600]!,
        surface: Colors.white,
        onPrimary: Colors.white,
        onSurface: Colors.grey[800]!,
      ),
      appBarTheme: AppBarTheme(
        elevation: 0,
        backgroundColor: Colors.white,
        iconTheme: IconThemeData(color: Colors.grey[800]),
        titleTextStyle: TextStyle(
          color: Colors.grey[800],
          fontSize: 20,
          fontWeight: FontWeight.w600,
        ),
      ),
      scaffoldBackgroundColor: Colors.grey[50],
      inputDecorationTheme: InputDecorationTheme(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: Colors.grey[300]!),
        ),
        filled: true,
        fillColor: Colors.white,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 14,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          foregroundColor: Colors.white,
          backgroundColor: Colors.grey[800],
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          padding: const EdgeInsets.symmetric(vertical: 14),
        ),
      ),
    );
  }

  Map<String, WidgetBuilder> _buildAppRoutes() {
    return {
      '/': (context) => HomePage(),
      AuthRoutes.login: (context) => LoginPage(),
      AuthRoutes.roleSelection: (context) => const RoleSelectionPage(),
      AuthRoutes.register: (context) {
        // 直接从/register访问时，可以重定向到身份选择页
        return const RoleSelectionPage();
      },
      ProfileRoutes.profile_info: (context) => ProfileInfoPage(),
      ProfileRoutes.email_update: (context) => EmailUpdatePage(),
      ApplyRoutes.ward_device_apply: (context) => WardDeviceApplyPage(),
      ApplyRoutes.guardian_bind_requests_page: (context) => GuardianBindRequestsPage(),
    };
  }
}
