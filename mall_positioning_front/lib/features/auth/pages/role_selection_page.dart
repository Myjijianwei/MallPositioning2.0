import 'package:flutter/material.dart';
import 'register_page.dart';

class RoleSelectionPage extends StatelessWidget {
  static const routeName = '/role-selection';

  const RoleSelectionPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('选择您的身份')),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                '请选择您的身份',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 40),
              _buildRoleCard(
                context,
                icon: Icons.family_restroom,
                title: '监护人',
                description: '创建账户管理家庭成员',
                role: 'guardian',
              ),
              const SizedBox(height: 20),
              _buildRoleCard(
                context,
                icon: Icons.child_care,
                title: '被监护人',
                description: '创建账户接受监护人管理',
                role: 'ward',
              ),
              const SizedBox(height: 40),
              TextButton(
                onPressed: () {
                  // 跳转到登录页面
                  Navigator.pushReplacementNamed(context, '/login');
                },
                child: const Text('已有账号？立即登录'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildRoleCard(
      BuildContext context, {
        required IconData icon,
        required String title,
        required String description,
        required String role,
      }) {
    return Card(
      elevation: 4,
      child: InkWell(
        borderRadius: BorderRadius.circular(8),
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => RegisterPage(role: role),
            ),
          );
        },
        child: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            children: [
              Icon(icon, size: 50, color: Theme.of(context).primaryColor),
              const SizedBox(height: 16),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                description,
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.grey[600],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}