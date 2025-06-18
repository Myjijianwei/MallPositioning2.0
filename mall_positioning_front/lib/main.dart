import 'package:flutter/material.dart';
import 'package:mall_positioning_front/pages/demo/DemoLiveMonitor.dart';
import 'package:mall_positioning_front/pages/Welcome.dart';
import 'package:mall_positioning_front/pages/auth/login.dart';
import 'package:mall_positioning_front/pages/auth/register.dart';
import 'package:mall_positioning_front/pages/home/home.dart';
import 'package:mall_positioning_front/services/auth_service.dart';
import 'package:mall_positioning_front/utils/DemoRouteGuard.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';


Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final prefs = await SharedPreferences.getInstance();
  final token = prefs.getString('token');
  final String initialRoute = (token != null && token.isNotEmpty) ? '/home' : '/welcome';

  // 创建AuthService实例
  final authService = AuthService();

  runApp(
    // 使用Provider包裹整个应用
    Provider<AuthService>(
      create: (_) => authService,
      child: MyApp(initialRoute: initialRoute, authService: authService),
    ),
  );
}

class MyApp extends StatelessWidget {
  final String initialRoute;
  final AuthService authService;

  const MyApp({Key? key, required this.initialRoute, required this.authService}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '商城防走失APP',
      theme: ThemeData(
        colorScheme: ColorScheme.light(
          primary: Colors.grey[800]!,      // 深灰作为主色
          secondary: Colors.blueGrey[600]!, // 蓝灰作为次色
          surface: Colors.white,     // 背景色
          onPrimary: Colors.white,         // 主色上的文字颜色
          onSurface: Colors.grey[800]!,    // 表面上的文字颜色
        ),
        appBarTheme: AppBarTheme(
          elevation: 0,                    // 去除阴影
          backgroundColor: Colors.white,
          iconTheme: IconThemeData(color: Colors.grey[800]),
          titleTextStyle: TextStyle(
            color: Colors.grey[800],
            fontSize: 20,
            fontWeight: FontWeight.w600,
          ),
        ),
        scaffoldBackgroundColor: Colors.grey[50],
        cardTheme: CardThemeData(
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: BorderSide(color: Colors.grey[200]!, width: 1),
          ),
          margin: EdgeInsets.all(8),
        ),
        inputDecorationTheme: InputDecorationTheme(
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: BorderSide(color: Colors.grey[300]!),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: BorderSide(color: Colors.grey[300]!),
          ),
          filled: true,
          fillColor: Colors.white,
          contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        ),
        buttonTheme: ButtonThemeData(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
          padding: EdgeInsets.symmetric(vertical: 14),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            foregroundColor: Colors.white, backgroundColor: Colors.grey[800],
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            padding: EdgeInsets.symmetric(vertical: 14),
          ),
        ),
        textButtonTheme: TextButtonThemeData(
          style: TextButton.styleFrom(
            foregroundColor: Colors.grey[800],
          ),
        ),
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      initialRoute: initialRoute,
      routes: {
        '/welcome': (context) => WelcomePage(),      // 新增欢迎页
        '/login': (context) => LoginPage(),
        '/register': (context) => RegisterPage(),
        '/home': (context) => const HomePage(),
        '/demo/monitor': (context) => DemoLiveMonitorPage(), // 新增演示监控页
      },
      navigatorObservers: [
        DemoRouteGuard(authService: authService), // 添加演示模式路由守卫
      ],
    );
  }
}