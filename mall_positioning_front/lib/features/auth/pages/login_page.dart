import 'package:flutter/material.dart';
import 'package:mall_positioning_front/pages/auth/register.dart';
import 'package:mall_positioning_front/services/auth_service.dart';
import 'package:mall_positioning_front/services/msm_service.dart';
import 'package:mall_positioning_front/widgets/verification_code_input.dart';
import 'package:provider/provider.dart';

/// 登录页面
/// 提供账号密码登录和邮箱验证码登录两种方式
class LoginPage extends StatefulWidget {
  static const routeName = '/login';
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  // region ----------------------------- 控制器和状态 -----------------------------
  final _formKey = GlobalKey<FormState>(); // 表单Key
  final _accountController = TextEditingController(); // 账号控制器
  final _passwordController = TextEditingController(); // 密码控制器
  final _emailController = TextEditingController(); // 邮箱控制器
  final _verificationCodeKey = GlobalKey<VerificationCodeInputState>(); // 验证码组件Key

  bool _isLoading = false; // 加载状态
  int _loginType = 0; // 登录类型 0:账号密码 1:邮箱验证码
  // endregion

  // region ----------------------------- 生命周期 -----------------------------
  @override
  void dispose() {
    _accountController.dispose();
    _passwordController.dispose();
    _emailController.dispose();
    super.dispose();
  }
  // endregion

  // region ----------------------------- 业务方法 -----------------------------
  /// 处理登录逻辑
  Future<void> _handleLogin() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;

    setState(() => _isLoading = true);
    final authService = Provider.of<AuthService>(context, listen: false);

    try {
      if (_loginType == 0) {
        // 账号密码登录
        await authService.login(
          _accountController.text,
          _passwordController.text,
        );
      } else {
        // 邮箱验证码登录
        final code = _verificationCodeKey.currentState?.code;
        if (code == null || code.isEmpty) throw Exception('请填写验证码');

        await authService.loginWithEmailCode(
          _emailController.text,
          code,
        );
      }

      if (!mounted) return;
      Navigator.pushReplacementNamed(context, '/');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString())),
      );
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }
  // endregion

  // region ----------------------------- 构建方法 -----------------------------
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('用户登录')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            _buildLoginTypeToggle(), // 登录方式切换
            const SizedBox(height: 20),
            _buildLoginForm(),       // 登录表单
            const SizedBox(height: 30),
            _buildLoginButton(),     // 登录按钮
            const SizedBox(height: 20),
            _buildRegisterLink(),    // 注册链接
          ],
        ),
      ),
    );
  }

  /// 构建登录方式切换按钮
  Widget _buildLoginTypeToggle() {
    return ToggleButtons(
      isSelected: [_loginType == 0, _loginType == 1],
      onPressed: (index) => setState(() => _loginType = index),
      borderRadius: BorderRadius.circular(8),
      children: const [
        Padding(
          padding: EdgeInsets.symmetric(horizontal: 16),
          child: Text('账号密码登录'),
        ),
        Padding(
          padding: EdgeInsets.symmetric(horizontal: 16),
          child: Text('邮箱验证码登录'),
        ),
      ],
    );
  }

  /// 构建登录表单
  Widget _buildLoginForm() {
    return Form(
      key: _formKey,
      child: _loginType == 0 ? _buildAccountForm() : _buildEmailForm(),
    );
  }

  /// 构建账号密码表单
  Widget _buildAccountForm() {
    return Column(
      children: [
        TextFormField(
          controller: _accountController,
          decoration: const InputDecoration(labelText: '账号'),
          validator: (value) => value?.isEmpty ?? true ? '请输入账号' : null,
        ),
        const SizedBox(height: 16),
        TextFormField(
          controller: _passwordController,
          decoration: const InputDecoration(labelText: '密码'),
          obscureText: true,
          validator: (value) => value?.isEmpty ?? true ? '请输入密码' : null,
        ),
      ],
    );
  }

  /// 构建邮箱验证码表单
  Widget _buildEmailForm() {
    return Column(
      children: [
        TextFormField(
          controller: _emailController,
          decoration: const InputDecoration(labelText: '电子邮箱'),
          keyboardType: TextInputType.emailAddress,
          validator: (value) {
            if (value?.isEmpty ?? true) return '请输入邮箱';
            if (!RegExp(r'^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value!)) {
              return '请输入有效的邮箱地址';
            }
            return null;
          },
        ),
        const SizedBox(height: 16),
        VerificationCodeInput(
          key: _verificationCodeKey,
          onSendCode: () => MsmService().sendEmailCode(_emailController.text),
        ),
      ],
    );
  }

  /// 构建登录按钮
  Widget _buildLoginButton() {
    return ElevatedButton(
      onPressed: _isLoading ? null : _handleLogin,
      style: ElevatedButton.styleFrom(
        minimumSize: const Size(double.infinity, 50),
      ),
      child: _isLoading
          ? const CircularProgressIndicator(color: Colors.white)
          : const Text('登录'),
    );
  }

  /// 构建注册链接
  Widget _buildRegisterLink() {
    return TextButton(
      onPressed: () => Navigator.pushNamed(context, RegisterPage.routeName),
      child: const Text('还没有账号？立即注册'),
    );
  }
// endregion
}