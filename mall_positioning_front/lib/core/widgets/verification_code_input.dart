import 'dart:async';
import 'package:flutter/material.dart';

class VerificationCodeInput extends StatefulWidget {
  final Future<void> Function() onSendCode;
  final Key? key;

  const VerificationCodeInput({
    this.key,
    required this.onSendCode,
  }) : super(key: key);

  @override
  VerificationCodeInputState createState() => VerificationCodeInputState();
}

class VerificationCodeInputState extends State<VerificationCodeInput> {
  final TextEditingController _codeController = TextEditingController();
  int _countdownSeconds = 0;
  Timer? _countdownTimer;
  bool _isSending = false;

  String get code => _codeController.text;

  @override
  void dispose() {
    _codeController.dispose();
    _countdownTimer?.cancel();
    super.dispose();
  }

  Future<void> sendCode() async {
    if (_countdownSeconds > 0) return;

    setState(() => _isSending = true);

    try {
      await widget.onSendCode();
      _startCountdown();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('发送失败: $e')),
      );
    } finally {
      if (mounted) setState(() => _isSending = false);
    }
  }

  void _startCountdown() {
    setState(() => _countdownSeconds = 60);
    _countdownTimer?.cancel();
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_countdownSeconds == 1) {
        timer.cancel();
        if (mounted) setState(() => _countdownSeconds = 0);
      } else {
        if (mounted) setState(() => _countdownSeconds--);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: TextFormField(
            controller: _codeController,
            decoration: const InputDecoration(
              labelText: '验证码',
              border: OutlineInputBorder(),
            ),
            keyboardType: TextInputType.number,
            validator: (value) => value?.isEmpty ?? true ? '请输入验证码' : null,
          ),
        ),
        const SizedBox(width: 10),
        ElevatedButton(
          onPressed: (_countdownSeconds > 0 || _isSending) ? null : sendCode,
          child: _countdownSeconds > 0
              ? Text('$_countdownSeconds s')
              : _isSending
              ? const SizedBox(
            width: 20,
            height: 20,
            child: CircularProgressIndicator(strokeWidth: 2),
          )
              : const Text('发送验证码'),
        ),
      ],
    );
  }
}