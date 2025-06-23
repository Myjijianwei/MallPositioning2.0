import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/services/auth_service.dart';
import '../profile/page/profile_page.dart';

/// 主应用页面框架
/// 包含底部导航栏和四个主要功能模块：
/// 1. 首页仪表盘
/// 2. 定位功能页
/// 3. 消息中心页
/// 4. 个人中心页
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _currentIndex = 0; // 当前选中的底部导航栏索引

  /// 各功能页面的Widget列表
  final List<Widget> _pages = [
    const HomeDashboard(),              // 首页
    const PlaceholderPage(title: '定位功能开发中'),
    const PlaceholderPage(title: '消息功能开发中'),
    const ProfilePage(),                // 个人中心
  ];

  @override
  Widget build(BuildContext context) {
    final isGuardian = context.watch<AuthService>().userRole == UserRole.guardian;

    return Scaffold(
      appBar: AppBar(
        title: const Text('商城防走失'),
        centerTitle: true,
        actions: [
          // 消息通知快捷入口
          IconButton(
            icon: const Icon(Icons.notifications),
            onPressed: () => setState(() => _currentIndex = 2),
          ),
        ],
      ),
      body: _pages[_currentIndex], // 显示当前选中的页面
      bottomNavigationBar: _buildBottomNavBar(isGuardian),
    );
  }

  /// 构建底部导航栏
  Widget _buildBottomNavBar(bool isGuardian) {
    return BottomNavigationBar(
      currentIndex: _currentIndex,
      type: BottomNavigationBarType.fixed,
      selectedItemColor: Colors.blue[800],
      unselectedItemColor: Colors.grey,
      onTap: (index) => setState(() => _currentIndex = index),
      items: [
        const BottomNavigationBarItem(icon: Icon(Icons.home), label: '首页'),
        BottomNavigationBarItem(
          icon: Icon(isGuardian ? Icons.location_searching : Icons.location_on),
          label: isGuardian ? '监护' : '位置', // 根据角色显示不同标签
        ),
        const BottomNavigationBarItem(icon: Icon(Icons.message), label: '消息'),
        const BottomNavigationBarItem(icon: Icon(Icons.person), label: '我的'),
      ],
    );
  }
}

/// 首页仪表盘组件
class HomeDashboard extends StatelessWidget {
  const HomeDashboard({super.key});

  @override
  Widget build(BuildContext context) {
    final isGuardian = context.watch<AuthService>().userRole == UserRole.guardian;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          _buildWelcomeCard(), // 欢迎卡片
          const SizedBox(height: 24),
          Text(
            isGuardian ? '监护功能' : '安全功能',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: 16),
          _buildFunctionGrid(isGuardian), // 功能网格
        ],
      ),
    );
  }

  /// 构建欢迎卡片
  Widget _buildWelcomeCard() {
    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '安全守护，安心购物',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: Colors.blue[800],
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text('实时定位监控，超出围栏自动报警'),
                ],
              ),
            ),
            const Icon(Icons.safety_check, size: 50, color: Colors.blue),
          ],
        ),
      ),
    );
  }

  /// 构建功能网格
  Widget _buildFunctionGrid(bool isGuardian) {
    final functions = isGuardian ? _guardianFunctions : _wardFunctions;

    return GridView.count(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisCount: 2,
      childAspectRatio: 1.2,
      mainAxisSpacing: 16,
      crossAxisSpacing: 16,
      children: functions.map((func) => _buildFunctionCard(func)).toList(),
    );
  }

  /// 构建单个功能卡片
  Widget _buildFunctionCard(FunctionItem func) {
    return Card(
      elevation: 2,
      child: InkWell(
        onTap: () {}, // TODO: 实现具体功能跳转
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(func.icon, size: 40, color: func.color),
            const SizedBox(height: 8),
            Text(func.title),
          ],
        ),
      ),
    );
  }
}

/// 功能项数据模型
class FunctionItem {
  final IconData icon;
  final String title;
  final Color color;

  const FunctionItem({
    required this.icon,
    required this.title,
    required this.color,
  });
}

/// 监护人专属功能列表
const _guardianFunctions = [
  FunctionItem(icon: Icons.location_searching, title: '实时监护', color: Colors.blue),
  FunctionItem(icon: Icons.history, title: '历史轨迹', color: Colors.green),
  FunctionItem(icon: Icons.fence, title: '地理围栏', color: Colors.orange),
  FunctionItem(icon: Icons.group, title: '成员管理', color: Colors.purple),
];

/// 被监护人专属功能列表
const _wardFunctions = [
  FunctionItem(icon: Icons.location_on, title: '位置共享', color: Colors.blue),
  FunctionItem(icon: Icons.emergency, title: '紧急求助', color: Colors.red),
  FunctionItem(icon: Icons.shopping_cart, title: '商城导航', color: Colors.green),
  FunctionItem(icon: Icons.devices, title: '设备管理', color: Colors.orange),
];

/// 占位页面（用于未开发完成的功能）
class PlaceholderPage extends StatelessWidget {
  final String title;
  const PlaceholderPage({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return Center(child: Text(title));
  }
}