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
    final theme = Theme.of(context);
    final authService = Provider.of<AuthService>(context);
    final isGuardian = authService.userRole == UserRole.guardian;
    final isLoggedIn = authService.isLoggedIn;
    final textTheme = theme.textTheme;

    return Scaffold(
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 用户信息横版卡片
            _buildHorizontalUserCard(context, theme, authService, isLoggedIn, isGuardian),
            const SizedBox(height: 32),

            // 功能入口标题
            Padding(
              padding: const EdgeInsets.only(left: 8.0),
              child: Text(
                '功能入口',
                style: textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: theme.colorScheme.onSurface,
                ),
              ),
            ),
            const SizedBox(height: 16),

            // 功能入口列表
            _buildProfileItem(context, Icons.person, '个人资料'),
            _buildProfileItem(context, Icons.email, '换绑邮箱'),
            _buildProfileItem(context, Icons.settings, '系统设置'),
            _buildProfileItem(context, Icons.help, '帮助中心'),
            if (isLoggedIn && !isGuardian)
              _buildProfileItem(context, Icons.settings_applications, "设备申请"),
            if (isLoggedIn && isGuardian)
              _buildProfileItem(context, Icons.link, "设备绑定"),
            const SizedBox(height: 60),

            // 登录/登出按钮
            _buildAuthButton(context, isLoggedIn, authService),
          ],
        ),
      ),
    );
  }

  /// 构建横版用户信息卡片
  Widget _buildHorizontalUserCard(
      BuildContext context,
      ThemeData theme,
      AuthService authService,
      bool isLoggedIn,
      bool isGuardian,
      ) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
        side: BorderSide(
          color: theme.dividerColor.withOpacity(0.1),
          width: 1,
        ),
      ),
      child: InkWell(
        onTap: isLoggedIn ? () => Navigator.pushNamed(context, '/profile_info') : null,
        borderRadius: BorderRadius.circular(20),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // 用户头像
              CircleAvatar(
                radius: 40,
                backgroundColor: theme.colorScheme.primary.withOpacity(0.1),
                child: isLoggedIn && authService.userAvatar != null
                    ? ClipOval(
                  child: Image.network(
                    authService.userAvatar!,
                    fit: BoxFit.cover,
                    width: 80,
                    height: 80,
                    errorBuilder: (context, error, stackTrace) => const Icon(
                      Icons.person,
                      size: 40,
                      color: Colors.blue,
                    ),
                  ),
                )
                    : const Icon(
                  Icons.person,
                  size: 40,
                  color: Colors.blue,
                ),
              ),
              const SizedBox(width: 20),

              // 用户信息
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      isLoggedIn ? (authService.userNickname ?? '未设置昵称') : '未登录',
                      style: theme.textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: theme.colorScheme.onSurface,
                      ),
                    ),
                    if (isLoggedIn && authService.userEmail != null) ...[
                      const SizedBox(height: 4),
                      Text(
                        authService.userEmail!,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurface.withOpacity(0.6),
                          fontSize: 14,
                        ),
                      ),
                    ],
                    const SizedBox(height: 12),

                    // 模式标签
                    Row(
                      children: [
                        Container(
                          width: 6,
                          height: 6,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: isGuardian ? Colors.blue : Colors.purple,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Text(
                          isGuardian ? '监护人模式' : '被监护人模式',
                          style: theme.textTheme.labelMedium?.copyWith(
                            color: isGuardian ? Colors.blue.shade700 : Colors.purple.shade700,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              // 未登录时的角色切换按钮
              if (!isLoggedIn)
                Padding(
                  padding: const EdgeInsets.only(left: 16),
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: theme.colorScheme.primary.withOpacity(0.1),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    ),
                    onPressed: authService.toggleRole,
                    child: Text(
                      '切换模式',
                      style: TextStyle(
                        color: theme.colorScheme.primary,
                        fontWeight: FontWeight.w500,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  /// 构建个人中心功能项
  Widget _buildProfileItem(BuildContext context, IconData icon, String title) {
    return Card(
      elevation: 0,
      margin: const EdgeInsets.only(bottom: 1),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        leading: Icon(
          icon,
          color: Theme.of(context).colorScheme.primary,
        ),
        title: Text(
          title,
          style: Theme.of(context).textTheme.bodyLarge,
        ),
        trailing: const Icon(
          Icons.chevron_right,
          color: Colors.grey,
        ),
        onTap: () => _navigateToFeature(context, title),
      ),
    );
  }

  /// 构建登录/登出按钮
  Widget _buildAuthButton(BuildContext context, bool isLoggedIn, AuthService authService) {
    return ElevatedButton(
      style: ElevatedButton.styleFrom(
        backgroundColor: isLoggedIn ? Colors.red[100] : Colors.blue[100],
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        padding: const EdgeInsets.symmetric(vertical: 16),
        elevation: 0,
      ),
      onPressed: () async {
        if (isLoggedIn) {
          await authService.logout();
        } else {
          Navigator.pushNamed(context, LoginPage.routeName);
        }
      },
      child: Center(
        child: Text(
          isLoggedIn ? '退出登录' : '登录/注册',
          style: TextStyle(
            color: isLoggedIn ? Colors.red : Colors.blue,
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
          textAlign: TextAlign.center,
        ),

      ),
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
      case '设备申请':
        Navigator.pushNamed(context, '/ward_device_apply');
        break;
      case '设备绑定':
        Navigator.pushNamed(context, '/guardian_bind_requests_page');
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
