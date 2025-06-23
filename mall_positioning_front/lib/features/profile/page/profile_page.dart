import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/services/auth_service.dart';
import '../../auth/pages/login_page.dart';

/// 个人中心页面
/// 功能：
/// 1. 显示用户基本信息
/// 2. 提供功能入口
/// 3. 处理登录/登出状态
class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);
    final isGuardian = authService.userRole == UserRole.guardian;
    final isLoggedIn = authService.isLoggedIn;

    return Scaffold(
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // region ----------------------------- 用户头像区 -----------------------------
            const SizedBox(height: 20),
            CircleAvatar(
              radius: 50,
              backgroundColor: Colors.blue[100],
              child: const Icon(Icons.person, size: 50),
            ),
            const SizedBox(height: 16),
            Text(
              isLoggedIn ? '用户昵称' : '未登录',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 8),
            Text(
              isGuardian ? '监护人模式' : '被监护人模式',
              style: TextStyle(color: Colors.grey[600]),
            ),
            const SizedBox(height: 32),
            // endregion

            // region ----------------------------- 未登录时的角色切换 -----------------------------
            if (!isLoggedIn) ...[
              ElevatedButton(
                onPressed: authService.toggleRole,
                child: Text('切换为${isGuardian ? '被监护人' : '监护人'}模式'),
              ),
              const SizedBox(height: 20),
            ],
            // endregion

            // region ----------------------------- 功能入口 -----------------------------
            _buildProfileItem(context, Icons.person, '个人资料'),
            _buildProfileItem(context, Icons.ac_unit, '换绑邮箱'),
            _buildProfileItem(context, Icons.settings, '系统设置'),
            _buildProfileItem(context, Icons.help, '帮助中心'),
            // endregion

            // region ----------------------------- 登录/登出按钮 -----------------------------
            const SizedBox(height: 40),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: isLoggedIn ? Colors.red[100] : Colors.blue[100],
              ),
              onPressed: () async {
                if (isLoggedIn) {
                  await authService.logout();
                } else {
                  Navigator.pushNamed(context, LoginPage.routeName);
                }
              },
              child: Text(
                isLoggedIn ? '退出登录' : '登录/注册',
                style: TextStyle(
                  color: isLoggedIn ? Colors.red : Colors.blue,
                ),
              ),
            ),
            // endregion
          ],
        ),
      ),
    );
  }

  /// 构建个人中心功能项
  Widget _buildProfileItem(BuildContext context, IconData icon, String title) {
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      trailing: const Icon(Icons.chevron_right),
      onTap: () => _navigateToFeature(context, title),
    );
  }

  /// 根据标题跳转到对应功能页
  void _navigateToFeature(BuildContext context, String title) {
    switch (title) {
      case '个人资料':
        Navigator.pushNamed(context, '/profile_info');
        break;
      case '换绑邮箱':
        Navigator.pushNamed(context, '/email_update');
        break;
      case '系统设置':
        Navigator.pushNamed(context, '/settings');
        break;
      case '帮助中心':
        Navigator.pushNamed(context, '/help');
        break;
    }
  }
}