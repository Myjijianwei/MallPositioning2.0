import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../services/auth_service.dart';
import '../widgets/custom_button.dart';

class WelcomePage extends StatelessWidget {
  const WelcomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          // 顶部宣传区
          Container(
            padding: EdgeInsets.all(24),
            color: Colors.blue[50],
            child: Column(
              children: [
                Image.asset('assets/logo.png', height: 180),
                SizedBox(height: 16),
                Text(
                  '智能安全监护解决方案',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                SizedBox(height: 8),
                Text(
                  '为家人提供全天候位置守护',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ),
          ),

          // 功能预览区
          Expanded(
            child: ListView(
              children: [
                _buildFeatureCard(
                  context,
                  Icons.location_on,
                  '实时位置监控',
                  '随时查看家人当前位置',
                  onTap: () => _enterDemoMode(context, '/demo/monitor'),
                ),
                _buildFeatureCard(
                  context,
                  Icons.fence,
                  '电子围栏',
                  '设置安全区域，越界自动警报',
                  onTap: () => _enterDemoMode(context, '/demo/fence'),
                ),
                _buildFeatureCard(
                  context,
                  Icons.history,
                  '历史轨迹',
                  '回放家人活动路线',
                  onTap: () => _enterDemoMode(context, '/demo/history'),
                ),
                _buildFeatureCard(
                  context,
                  Icons.notifications,
                  '智能警报',
                  '越界/离线/低电量即时通知',
                  onTap: () => _enterDemoMode(context, '/demo/alerts'),
                ),
              ],
            ),
          ),

          // 底部操作区
          Padding(
            padding: EdgeInsets.all(16),
            child: Column(
              children: [
                CustomButton(
                  text: '立即体验演示版',
                  onPressed: () => _enterDemoMode(context, '/demo/monitor'),
                  backgroundColor: Colors.grey[200],
                  foregroundColor: Colors.grey[800],
                ),
                SizedBox(height: 12),
                OutlinedButton(
                  onPressed: () => Navigator.pushNamed(context, '/register'),
                  style: OutlinedButton.styleFrom(
                    minimumSize: Size(double.infinity, 48),
                    side: BorderSide(color: Theme.of(context).colorScheme.primary),
                  ),
                  child: Text('注册完整版'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFeatureCard(BuildContext context, IconData icon, String title, String subtitle, {VoidCallback? onTap}) {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Row(
            children: [
              Icon(icon, color: Theme.of(context).colorScheme.primary),
              SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: Theme.of(context).textTheme.titleMedium),
                    SizedBox(height: 4),
                    Text(subtitle, style: Theme.of(context).textTheme.bodySmall),
                  ],
                ),
              ),
              Icon(Icons.chevron_right, color: Colors.grey),
            ],
          ),
        ),
      ),
    );
  }

  void _enterDemoMode(BuildContext context, String route) {
    Navigator.pushNamed(context, route);
    // 可以在这里设置游客标识
    Provider.of<AuthService>(context, listen: false).setGuestMode();
  }
}