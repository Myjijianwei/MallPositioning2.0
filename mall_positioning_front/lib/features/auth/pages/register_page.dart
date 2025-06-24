import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:async';

import '../../../core/services/auth_service.dart';
import '../../../core/services/message_service.dart';
import '../../../core/widgets/custom_button.dart';
import '../../../core/widgets/custom_text_field.dart';

class RegisterPage extends StatefulWidget {
  final String role; // 'guardian' 或 'ward'

  const RegisterPage({super.key, required this.role});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  // region ---------------------------- 表单控制器 ----------------------------
  final _formKey = GlobalKey<FormState>(); // 表单Key
  final _userAccountController = TextEditingController(); // 用户名控制器
  final _emailController = TextEditingController(); // 邮箱控制器
  final _passwordController = TextEditingController(); // 密码控制器
  final _confirmPasswordController = TextEditingController(); // 确认密码控制器
  final _verificationCodeController = TextEditingController(); // 验证码控制器
  // endregion

  // region ---------------------------- 错误提示变量 ----------------------------
  String? _usernameError;
  String? _passwordError;
  String? _confirmPasswordError;
  String? _emailError;
  String? _verificationCodeError;
  // endregion

  // region ---------------------------- 状态变量 ----------------------------
  bool _isLoading = false; // 注册加载状态
  bool _canSendCode = true; // 是否可以发送验证码
  int _countdown = 0; // 验证码倒计时
  Timer? _timer; // 倒计时计时器
  // endregion

  // region ---------------------------- 生命周期 ----------------------------
  @override
  void dispose() {
    // 清理所有控制器和计时器
    _userAccountController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    _verificationCodeController.dispose();
    _timer?.cancel();
    super.dispose();
  }
  // endregion

  // region ---------------------------- 页面构建 ----------------------------
  @override
  Widget build(BuildContext context) {
    final roleName = widget.role == 'guardian' ? '监护人' : '被监护人';

    return Scaffold(
      appBar: _buildAppBar(roleName), // 构建AppBar
      body: _buildBody(roleName), // 构建主体内容
    );
  }

  /// 构建AppBar
  AppBar _buildAppBar(String roleName) {
    return AppBar(
      title: Text('$roleName注册'),
      leading: IconButton(
        icon: const Icon(Icons.arrow_back),
        onPressed: () => Navigator.pop(context),
      ),
    );
  }

  /// 构建页面主体内容
  Widget _buildBody(String roleName) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 注册身份提示
            Text(
              '您正在注册为$roleName',
              style: const TextStyle(fontSize: 16, color: Colors.grey),
            ),
            const SizedBox(height: 30),

            // 用户名输入
            _buildUsernameField(),
            const SizedBox(height: 20),

            // 密码输入
            _buildPasswordField(),
            const SizedBox(height: 20),

            // 确认密码输入
            _buildConfirmPasswordField(),
            const SizedBox(height: 20),

            // 邮箱输入
            _buildEmailField(),
            const SizedBox(height: 20),

            // 验证码输入和发送按钮
            _buildVerificationCodeRow(),
            const SizedBox(height: 30),

            // 注册按钮
            _buildRegisterButton(),
            const SizedBox(height: 20),

            // 登录跳转链接
            _buildLoginLink(),
          ],
        ),
      ),
    );
  }

  /// 构建用户名输入框
  Widget _buildUsernameField() {
    return CustomTextField(
      labelText: '用户名',
      controller: _userAccountController,
      keyboardType: TextInputType.text,
      prefixIcon: const Icon(Icons.person),
      errorText: _usernameError,
      onChanged: (value) {
        setState(() {
          if (value == null || value.isEmpty) {
            _usernameError = '请输入用户名';
          } else if (value.length < 6) {
            _usernameError = '用户名至少需要6个字符';
          } else {
            _usernameError = null;
          }
        });
      },
    );
  }

  /// 构建密码输入框
  Widget _buildPasswordField() {
    return CustomTextField(
      controller: _passwordController,
      labelText: '密码',
      obscureText: true,
      prefixIcon: const Icon(Icons.lock),
      errorText: _passwordError,
      onChanged: (value) {
        setState(() {
          if (value == null || value.isEmpty) {
            _passwordError = '请输入密码';
          } else if (value.length < 6) {
            _passwordError = '密码至少需要6个字符';
          } else {
            _passwordError = null;
          }
        });
      },
    );
  }

  /// 构建确认密码输入框
  Widget _buildConfirmPasswordField() {
    return CustomTextField(
      controller: _confirmPasswordController,
      labelText: '确认密码',
      obscureText: true,
      prefixIcon: const Icon(Icons.lock_outline),
      errorText: _confirmPasswordError,
      onChanged: (value) {
        setState(() {
          if (value == null || value.isEmpty) {
            _confirmPasswordError = '请确认密码';
          } else if (value != _passwordController.text) {
            _confirmPasswordError = '两次输入的密码不一致';
          } else {
            _confirmPasswordError = null;
          }
        });
      },
    );
  }

  /// 构建邮箱输入框
  Widget _buildEmailField() {
    return CustomTextField(
      controller: _emailController,
      labelText: '电子邮箱',
      keyboardType: TextInputType.emailAddress,
      prefixIcon: const Icon(Icons.email),
      errorText: _emailError,
      onChanged: (value) {
        setState(() {
          if (value == null || value.isEmpty) {
            _emailError = '请输入电子邮箱';
          } else if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value)) {
            _emailError = '请输入有效的电子邮箱';
          } else {
            _emailError = null;
          }
        });
      },
    );
  }

  /// 构建验证码输入行
  Widget _buildVerificationCodeRow() {
    return Row(
      children: [
        // 验证码输入框
        Expanded(
          child: CustomTextField(
            controller: _verificationCodeController,
            labelText: '验证码',
            keyboardType: TextInputType.number,
            prefixIcon: const Icon(Icons.message),
            errorText: _verificationCodeError,
            onChanged: (value) {
              setState(() {
                if (value == null || value.isEmpty) {
                  _verificationCodeError = '请输入验证码';
                } else {
                  _verificationCodeError = null;
                }
              });
            },
          ),
        ),
        const SizedBox(width: 10),

        // 获取验证码按钮
        SizedBox(
          width: 120,
          child: ElevatedButton(
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
            ),
            onPressed: _canSendCode ? _sendEmailCode : null,
            child: _canSendCode
                ? const Text('获取验证码')
                : Text('${_countdown}s后重试'),
          ),
        ),
      ],
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
  Widget _buildLoginLink() {
    return Center(
      child: TextButton(
        onPressed: () => Navigator.pushReplacementNamed(context, '/login'),
        child: const Text('已有账号？立即登录'),
      ),
    );
  }
  // endregion

  // region ---------------------------- 业务逻辑 ----------------------------
  /// 发送邮箱验证码
  Future<void> _sendEmailCode() async {
    // 验证邮箱格式
    if (_emailController.text.isEmpty) {
      _showErrorMessage('请输入邮箱');
      return;
    }

    if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(_emailController.text)) {
      _showErrorMessage('请输入有效的电子邮箱');
      return;
    }

    // 设置倒计时状态
    setState(() {
      _canSendCode = false;
      _countdown = 60;
    });

    try {
      // 发送验证码
      await MsmService().sendEmailCode(_emailController.text);
      _showSuccessMessage('验证码已发送至您的邮箱');

      // 启动倒计时
      _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
        setState(() => _countdown--);
        if (_countdown == 0) {
          timer.cancel();
          setState(() => _canSendCode = true);
        }
      });
    } catch (e) {
      // 发送失败重置状态
      setState(() => _canSendCode = true);
      _showErrorMessage('发送失败: $e');
    }
  }

  /// 处理注册逻辑
  Future<void> _handleRegister() async {
    // 表单验证
    if (!(_formKey.currentState?.validate() ?? false)) return;

    // 密码一致性验证
    if (_passwordController.text != _confirmPasswordController.text) {
      _showErrorMessage('两次输入的密码不一致');
      return;
    }

    // 验证码验证
    final code = _verificationCodeController.text;
    if (code.isEmpty) {
      _showErrorMessage('请填写验证码');
      return;
    }

    setState(() => _isLoading = true);
    final authService = Provider.of<AuthService>(context, listen: false);

    try {
      // 调用注册服务
      await authService.register(
        userAccount: _userAccountController.text,
        userPassword: _passwordController.text,
        checkPassword: _confirmPasswordController.text,
        email: _emailController.text,
        code: code,
        userRole: widget.role,
      );

      if (!mounted) return;

      // 注册成功处理
      _showSuccessMessage('注册成功，请登录');
      Navigator.pushReplacementNamed(context, '/login');
    } catch (e) {
      // 注册失败处理
      if (!mounted) return;
      _showErrorMessage('注册失败: $e');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  /// 显示错误消息
  void _showErrorMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: Colors.red),
    );
  }

  /// 显示成功消息
  void _showSuccessMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: Colors.green),
    );
  }
// endregion
}