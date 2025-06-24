import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/services/auth_service.dart';
import '../../data/models/FunctionItem.dart';
import '../profile/page/profile_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}


class _HomePageState extends State<HomePage> {
  int _currentIndex = 0;

  List<Widget> get _pages => [
    const EnhancedHomeDashboard(),
    const PlaceholderPage(title: '功能开发中'),
    const PlaceholderPage(title: '消息功能开发中'),
    const ProfilePage(),
  ];

  @override
  Widget build(BuildContext context) {
    final isGuardian = context.watch<AuthService>().userRole == UserRole.guardian;

    return Scaffold(
      appBar: AppBar(
        title: const Text('商城防走失'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications),
            onPressed: () => setState(() => _currentIndex = 2),
          ),
        ],
      ),
      body: _pages[_currentIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        type: BottomNavigationBarType.fixed,
        selectedItemColor: Colors.blue[800],
        unselectedItemColor: Colors.grey,
        onTap: (index) => setState(() => _currentIndex = index),
        items: [
          const BottomNavigationBarItem(icon: Icon(Icons.home), label: '首页'),
          BottomNavigationBarItem(
            icon: Icon(isGuardian ? Icons.location_searching : Icons.location_on),
            label: isGuardian ? '监护' : '位置',
          ),
          const BottomNavigationBarItem(icon: Icon(Icons.message), label: '消息'),
          const BottomNavigationBarItem(icon: Icon(Icons.person), label: '我的'),
        ],
      ),
    );
  }
}

/// 增强版首页
class EnhancedHomeDashboard extends StatelessWidget {
  const EnhancedHomeDashboard({super.key});

  @override
  Widget build(BuildContext context) {
    final authService = context.watch<AuthService>();
    final isGuardian = authService.userRole == UserRole.guardian;
    final userName = authService.userNickname ?? '用户';

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          // 1. 增强的欢迎卡片
          _buildEnhancedWelcomeCard(context, isGuardian, userName),

          const SizedBox(height: 24),

          // 2. 紧急求助按钮 (仅被监护人)
          if (!isGuardian) _buildEmergencyButtonSection(context),

          const SizedBox(height: 24),

          // 3. 功能网格
          Text(
            isGuardian ? '监护功能' : '安全功能',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: 16),
          _buildFunctionGrid(isGuardian),
        ],
      ),
    );
  }

  /// 增强的欢迎卡片
  Widget _buildEnhancedWelcomeCard(BuildContext context, bool isGuardian, String userName) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '$userName，${_getDayPeriod()}好',
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    isGuardian ? '您正在监护家人' : '您处于安全监护中',
                    style: TextStyle(color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: isGuardian ? Colors.blue[50] : Colors.green[50],
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                isGuardian ? '监护人' : '被监护人',
                style: TextStyle(
                  color: isGuardian ? Colors.blue : Colors.green,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// 紧急求助区域
  Widget _buildEmergencyButtonSection(BuildContext context) {
    return Column(
      children: [
        const Text(
          '紧急情况下可使用以下功能',
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: _buildEmergencyButton(
                icon: Icons.emergency,
                color: Colors.red,
                text: '一键求助',
                onTap: () => _showEmergencyDialog(context),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildEmergencyButton(
                icon: Icons.volume_up,
                color: Colors.orange,
                text: '声音警报',
                onTap: () => _triggerSoundAlarm(context),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildEmergencyButton({
    required IconData icon,
    required Color color,
    required String text,
    required VoidCallback onTap,
  }) {
    return Material(
      borderRadius: BorderRadius.circular(12),
      color: color.withOpacity(0.1),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Column(
            children: [
              Icon(icon, color: color, size: 32),
              const SizedBox(height: 8),
              Text(
                text,
                style: TextStyle(
                  color: color,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// 功能网格
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

  Widget _buildFunctionCard(FunctionItem func) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: func.onTap,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(func.icon, size: 32, color: func.color),
            const SizedBox(height: 8),
            Text(
              func.title,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            if (func.description != null) ...[
              const SizedBox(height: 4),
              Text(
                func.description!,
                style: TextStyle(fontSize: 12, color: Colors.grey[600]),
              ),
            ],
          ],
        ),
      ),
    );
  }

  // ========== 功能项数据 ==========
  static const _guardianFunctions = [
    FunctionItem(
      icon: Icons.location_searching,
      title: '实时监护',
      color: Colors.blue,
      description: '查看家人位置',
    ),
    FunctionItem(
      icon: Icons.fence,
      title: '地理围栏',
      color: Colors.orange,
      description: '设置安全区域',
    ),
    FunctionItem(
      icon: Icons.group,
      title: '成员管理',
      color: Colors.purple,
    ),
    FunctionItem(
      icon: Icons.history,
      title: '历史轨迹',
      color: Colors.green,
    ),
  ];

  static const _wardFunctions = [
    FunctionItem(
      icon: Icons.location_on,
      title: '位置共享',
      color: Colors.blue,
      description: '共享我的位置',
    ),
    FunctionItem(
      icon: Icons.shopping_cart,
      title: '商城导航',
      color: Colors.green,
    ),
    FunctionItem(
      icon: Icons.devices,
      title: '我的设备',
      color: Colors.orange,
    ),
    FunctionItem(
      icon: Icons.emergency,
      title: '紧急功能',
      color: Colors.red,
    ),
  ];

  // ========== 辅助方法 ==========
  String _getDayPeriod() {
    final hour = DateTime.now().hour;
    if (hour < 6) return '凌晨';
    if (hour < 11) return '早上';
    if (hour < 13) return '中午';
    if (hour < 18) return '下午';
    return '晚上';
  }

  void _showEmergencyDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('一键求助'),
        content: const Text('确定要发送紧急求助信号吗？附近的工作人员将会收到通知。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(ctx);
              ScaffoldMessenger.of(ctx).showSnackBar(
                const SnackBar(content: Text('已发送求助信号')),
              );
            },
            child: const Text('确定', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }

  void _triggerSoundAlarm(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('已触发声音警报')),
    );
  }
}



/// 占位页面
class PlaceholderPage extends StatelessWidget {
  final String title;
  const PlaceholderPage({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return Center(child: Text(title));
  }
}