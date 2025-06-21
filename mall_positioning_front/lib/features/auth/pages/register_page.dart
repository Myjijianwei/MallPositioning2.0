import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/services/auth_service.dart';
import '../../../core/services/message_service.dart';
import '../../../core/widgets/custom_button.dart';
import '../../../core/widgets/custom_text_field.dart';
import '../../../core/widgets/verification_code_input.dart';

/// 注册页面
/// 包含用户基本信息填写和验证码验证
class RegisterPage extends StatefulWidget {
  static const routeName = '/register';
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  // region ----------------------------- 控制器和状态 -----------------------------
  final _formKey = GlobalKey<FormState>(); // 表单Key
  final _accountController = TextEditingController(); // 账号控制器
  final _emailController = TextEditingController(); // 邮箱控制器
  final _passwordController = TextEditingController(); // 密码控制器
  final _confirmPasswordController = TextEditingController(); // 确认密码控制器
  final _verificationCodeKey = GlobalKey<VerificationCodeInputState>(); // 验证码组件Key

  bool _isLoading = false; // 加载状态
  String? _selectedRole; // 选择的角色

  /// 角色选项
  static const List<Map<String, dynamic>> roleOptions = [
    {'value': 'guardian', 'label': '监护人'},
    {'value': 'ward', 'label': '被监护人'},
  ];
  // endregion

  // region ----------------------------- 生命周期 -----------------------------
  @override
  void dispose() {
    _accountController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }
  // endregion

  // region ----------------------------- 业务方法 -----------------------------
  /// 处理注册逻辑
  Future<void> _handleRegister() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;

    // 密码一致性验证
    if (_passwordController.text != _confirmPasswordController.text) {
      _showErrorMessage('两次输入的密码不一致');
      return;
    }

    // 角色选择验证
    if (_selectedRole == null) {
      _showErrorMessage('请选择身份');
      return;
    }

    // 验证码验证
    final code = _verificationCodeKey.currentState?.code;
    if (code == null || code.isEmpty) {
      _showErrorMessage('请填写验证码');
      return;
    }

    setState(() => _isLoading = true);
    final authService = Provider.of<AuthService>(context, listen: false);

    try {
      await authService.register(
        userAccount: _accountController.text,
        userPassword: _passwordController.text,
        checkPassword: _confirmPasswordController.text,
        email: _emailController.text,
        code: code,
        userRole: _selectedRole!,
      );

      if (!mounted) return;

      _showSuccessMessage('注册成功，请登录');
      Navigator.pushReplacementNamed(context, '/login');
    } catch (e) {
      if (!mounted) return;
      _showErrorMessage('注册失败: $e');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  /// 发送邮箱验证码
  Future<void> _sendEmailCode() async {
    if (_emailController.text.isEmpty) {
      _showErrorMessage('请输入邮箱');
      return;
    }

    if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(_emailController.text)) {
      _showErrorMessage('请输入有效的电子邮箱');
      return;
    }

    try {
      await MsmService().sendEmailCode(_emailController.text);
      _showSuccessMessage('验证码已发送');
    } catch (e) {
      _showErrorMessage('发送失败: $e');
    }
  }

  /// 显示错误消息
  void _showErrorMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
      ),
    );
  }

  /// 显示成功消息
  void _showSuccessMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.green,
      ),
    );
  }
  // endregion

  // region ----------------------------- 构建方法 -----------------------------
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('用户注册')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              _buildNameField(), // 用户名输入
              const SizedBox(height: 20),
              _buildEmailField(), // 邮箱输入
              const SizedBox(height: 20),
              _buildVerificationCodeInput(), // 验证码输入
              const SizedBox(height: 20),
              _buildPasswordField(), // 密码输入
              const SizedBox(height: 20),
              _buildConfirmPasswordField(), // 确认密码输入
              const SizedBox(height: 20),
              _buildRoleDropdown(), // 角色选择
              const SizedBox(height: 30),
              _buildRegisterButton(), // 注册按钮
              const SizedBox(height: 20),
              _buildLoginRedirect(), // 登录跳转
            ],
          ),
        ),
      ),
    );
  }

  /// 构建用户名输入框
  Widget _buildNameField() {
    return CustomTextField(
      controller: _accountController,
      labelText: '用户名',
      validator: (value) => value?.isEmpty ?? true ? '请输入用户名' : null,
    );
  }

  /// 构建邮箱输入框
  Widget _buildEmailField() {
    return CustomTextField(
      controller: _emailController,
      labelText: '电子邮箱',
      keyboardType: TextInputType.emailAddress,
      validator: (value) {
        if (value?.isEmpty ?? true) return '请输入电子邮箱';
        if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value!)) {
          return '请输入有效的电子邮箱';
        }
        return null;
      },
    );
  }

  /// 构建验证码输入组件
  Widget _buildVerificationCodeInput() {
    return VerificationCodeInput(
      key: _verificationCodeKey,
      onSendCode: _sendEmailCode,
    );
  }

  /// 构建密码输入框
  Widget _buildPasswordField() {
    return CustomTextField(
      controller: _passwordController,
      labelText: '密码',
      obscureText: true,
      validator: (value) {
        if (value?.isEmpty ?? true) return '请输入密码';
        if (value!.length < 6) return '密码至少需要6个字符';
        return null;
      },
    );
  }

  /// 构建确认密码输入框
  Widget _buildConfirmPasswordField() {
    return CustomTextField(
      controller: _confirmPasswordController,
      labelText: '确认密码',
      obscureText: true,
      validator: (value) {
        if (value?.isEmpty ?? true) return '请确认密码';
        if (value != _passwordController.text) return '两次输入的密码不一致';
        return null;
      },
    );
  }

  /// 构建角色下拉选择
  Widget _buildRoleDropdown() {
    return DropdownButtonFormField<String>(
      value: _selectedRole,
      decoration: const InputDecoration(
        labelText: '身份',
        border: OutlineInputBorder(),
      ),
      items: roleOptions.map((role) {
        return DropdownMenuItem<String>(
          value: role['value'],
          child: Text(role['label']),
        );
      }).toList(),
      onChanged: (value) => setState(() => _selectedRole = value),
      validator: (value) => value == null ? '请选择身份' : null,
    );
  }

  /// 构建注册按钮
  Widget _buildRegisterButton() {
    return CustomButton(
      text: '注册',
      isLoading: _isLoading,
      onPressed: _isLoading ? null : _handleRegister,
    );
  }

  /// 构建登录跳转链接
  Widget _buildLoginRedirect() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        const Text('已有账号?'),
        TextButton(
          onPressed: () => Navigator.pushNamed(context, '/login'),
          child: const Text('立即登录'),
        ),
      ],
    );
  }
// endregion
}