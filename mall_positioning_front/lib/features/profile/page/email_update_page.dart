import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/services/auth_service.dart';
import '../../../core/services/message_service.dart';
import '../../../core/widgets/verification_code_input.dart';

class EmailUpdatePage extends StatefulWidget {
  static const routeName = '/profile/email-update';
  const EmailUpdatePage({super.key});

  @override
  State<EmailUpdatePage> createState() => _EmailUpdatePageState();
}

class _EmailUpdatePageState extends State<EmailUpdatePage> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  bool _isLoading = false;
  String? _currentEmail;
  final _verificationCodeKey = GlobalKey<VerificationCodeInputState>();

  @override
  void initState() {
    super.initState();
    _currentEmail = context.read<AuthService>().userEmail;
  }

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _updateEmail() async {
    if (!_formKey.currentState!.validate()) return;

    final code = _verificationCodeKey.currentState?.code ?? '';
    if (code.isEmpty) {
      _showError('请输入验证码');
      return;
    }

    final newEmail = _emailController.text.trim();
    if (newEmail == _currentEmail) {
      _showError('新邮箱不能与当前邮箱相同');
      return;
    }

    setState(() => _isLoading = true);
    try {
      debugPrint('正在更新邮箱: $newEmail');
      await context.read<AuthService>().updateEmail(
        email: newEmail,
        code: code,
      );

      _showSuccess('邮箱更新成功');
      if (mounted) Navigator.pop(context);
    } on DioException catch (e) {
      final errorMsg = e.response?.data?['message'] ?? e.message;
      _showError('更新失败: $errorMsg');
    } catch (e) {
      _showError('发生错误: ${e.toString()}');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
        duration: const Duration(seconds: 3),
      ),
    );
  }

  void _showSuccess(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.green,
        duration: const Duration(seconds: 2),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('更换邮箱'),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 当前邮箱显示
              if (_currentEmail != null) ...[
                const Text(
                  '当前邮箱',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                Text(
                  _currentEmail!,
                  style: const TextStyle(fontSize: 14, color: Colors.grey),
                ),
                const SizedBox(height: 20),
              ],

              // 新邮箱输入
              TextFormField(
                controller: _emailController,
                decoration: const InputDecoration(
                  labelText: '新邮箱地址',
                  border: OutlineInputBorder(),
                  hintText: '请输入有效邮箱',
                ),
                keyboardType: TextInputType.emailAddress,
                validator: (value) {
                  if (value?.isEmpty ?? true) return '请输入邮箱';
                  if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value!)) {
                    return '邮箱格式不正确';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 20),

              // 验证码输入
              VerificationCodeInput(
                key: _verificationCodeKey,
                onSendCode: () => MsmService().sendEmailCode(_emailController.text),
              ),
              const SizedBox(height: 30),

              // 提交按钮
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _updateEmail,
                  style: ElevatedButton.styleFrom(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: _isLoading
                      ? const CircularProgressIndicator(
                    color: Colors.white,
                    strokeWidth: 2,
                  )
                      : const Text(
                    '确认更换',
                    style: TextStyle(fontSize: 16),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}