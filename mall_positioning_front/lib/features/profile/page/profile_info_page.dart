import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../../core/services/auth_service.dart';
import '../../../core/services/message_service.dart';
import '../../../core/widgets/verification_code_input.dart';

class ProfileInfoPage extends StatefulWidget {
  static const routeName = '/profile';
  const ProfileInfoPage({super.key});

  @override
  State<ProfileInfoPage> createState() => _ProfileInfoPageState();
}

class _ProfileInfoPageState extends State<ProfileInfoPage> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nicknameController;
  late TextEditingController _emailController;
  late TextEditingController _profileController;
  String? _avatarPath;
  String? _originalEmail;
  bool _showVerification = false;
  String? _verificationCode;
  bool _isEmailVerified = false;
  final _verificationCodeKey = GlobalKey<VerificationCodeInputState>();

  @override
  void initState() {
    super.initState();
    final authService = context.read<AuthService>();
    _nicknameController = TextEditingController(text: authService.userNickname);
    _emailController = TextEditingController(text: authService.userEmail);
    _profileController = TextEditingController(text: authService.userProfile);
    _avatarPath = authService.userAvatar;
    _originalEmail = authService.userEmail;
  }

  @override
  void dispose() {
    _nicknameController.dispose();
    _emailController.dispose();
    _profileController.dispose();
    super.dispose();
  }

  Future<void> _pickImage() async {
    try {
      final picker = ImagePicker();
      final pickedFile = await picker.pickImage(
        source: ImageSource.gallery,
        imageQuality: 85,
        maxWidth: 800,
      );

      if (pickedFile != null) {
        setState(() => _avatarPath = pickedFile.path);
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('头像已选择，请点击保存'))
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('图片选择失败: ${e.toString()}'))
      );
    }
  }

  void _checkEmailChange() {
    final newEmail = _emailController.text.trim();
    setState(() {
      _showVerification = newEmail != _originalEmail && newEmail.isNotEmpty;
      if (!_showVerification) _isEmailVerified = false;
    });
  }

  Future<void> _verifyEmail() async {
    if (_verificationCode == null || _verificationCode!.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('请输入验证码'))
      );
      return;
    }

    try {
      await MsmService().verifyEmailCode(
        email: _emailController.text,
        code: _verificationCode!,
      );

      setState(() => _isEmailVerified = true);
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('邮箱验证成功'))
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('验证失败: ${e.toString()}'))
      );
    }
  }

  Future<void> _submitChanges() async {
    if (!_formKey.currentState!.validate()) return;

    if (_showVerification && !_isEmailVerified) {
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('请先完成邮箱验证'))
      );
      return;
    }

    try {
      final authService = context.read<AuthService>();
      await authService.updateProfile(
        nickname: _nicknameController.text,
        email: _emailController.text,
        profile: _profileController.text,
        avatarPath: _avatarPath,
      );

      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('个人信息已保存'))
      );
      Navigator.pop(context);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('保存失败: ${e.toString()}'))
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('个人资料'),
        actions: [
          IconButton(
            icon: const Icon(Icons.check),
            onPressed: _submitChanges,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              _buildAvatarSection(),
              const SizedBox(height: 30),
              _buildNicknameField(),
              const SizedBox(height: 20),
              _buildEmailField(),
              if (_showVerification) ...[
                const SizedBox(height: 20),
                _buildVerificationSection(),
              ],
              const SizedBox(height: 20),
              _buildProfileField(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAvatarSection() {
    return Column(
      children: [
        GestureDetector(
          onTap: _pickImage,
          child: CircleAvatar(
            radius: 50,
            backgroundColor: Colors.grey[200],
            child: _buildAvatarContent(),
          ),
        ),
        const SizedBox(height: 10),
        Text(
          '点击修改头像',
          style: Theme.of(context).textTheme.bodySmall,
        ),
      ],
    );
  }

  Widget _buildAvatarContent() {
    if (_avatarPath == null) {
      final authService = context.read<AuthService>();
      if (authService.userAvatar == null) {
        return const Icon(Icons.person, size: 50, color: Colors.white);
      }
      return _buildNetworkAvatar(authService.userAvatar!);
    }

    if (_avatarPath!.startsWith('http')) {
      return _buildNetworkAvatar(_avatarPath!);
    } else {
      return ClipOval(
        child: Image.file(
          File(_avatarPath!),
          width: 100,
          height: 100,
          fit: BoxFit.cover,
        ),
      );
    }
  }

  Widget _buildNetworkAvatar(String url) {
    return ClipOval(
      child: CachedNetworkImage(
        imageUrl: url,
        width: 100,
        height: 100,
        fit: BoxFit.cover,
        httpHeaders: const {
          // 'Referer': 'http://localhost',
          'Referer': 'http://10.0.2.2'// 保持与您OSS配置一致的Referer
        },
        placeholder: (context, url) => Container(
          color: Colors.grey[300],
          child: const Center(child: CircularProgressIndicator()),
        ),
        errorWidget: (context, url, error) => Container(
          color: Colors.grey[300],
          child: const Icon(Icons.error),
        ),
      ),
    );
  }

  Widget _buildNicknameField() {
    return TextFormField(
      controller: _nicknameController,
      decoration: const InputDecoration(
        labelText: '昵称',
        prefixIcon: Icon(Icons.person_outline),
        border: OutlineInputBorder(),
      ),
      validator: (value) => value?.isEmpty ?? true ? '请输入昵称' : null,
    );
  }

  Widget _buildEmailField() {
    return TextFormField(
      controller: _emailController,
      decoration: InputDecoration(
        labelText: '邮箱',
        prefixIcon: const Icon(Icons.email_outlined),
        suffixIcon: _isEmailVerified
            ? const Icon(Icons.verified, color: Colors.green)
            : null,
        border: const OutlineInputBorder(),
      ),
      keyboardType: TextInputType.emailAddress,
      onChanged: (_) => _checkEmailChange(),
      validator: (value) {
        if (value?.isEmpty ?? true) return '请输入邮箱';
        if (!value!.contains('@')) return '请输入有效的邮箱地址';
        return null;
      },
    );
  }

  Widget _buildProfileField() {
    return TextFormField(
      controller: _profileController,
      decoration: const InputDecoration(
        labelText: '个人简介',
        prefixIcon: Icon(Icons.description_outlined),
        border: OutlineInputBorder(),
      ),
      maxLines: 3,
      maxLength: 200,
    );
  }

  Widget _buildVerificationSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(
              child: VerificationCodeInput(
                key: _verificationCodeKey,
                onSendCode: () => MsmService().sendEmailCode(_emailController.text),
              ),
            ),
            const SizedBox(width: 10),
            ElevatedButton(
              onPressed: _isEmailVerified ? null : _verifyEmail,
              child: const Text('验证'),
            ),
          ],
        ),
        if (_isEmailVerified)
          Padding(
            padding: const EdgeInsets.only(top: 8),
            child: Text(
              '邮箱已验证',
              style: TextStyle(
                color: Theme.of(context).primaryColor,
                fontSize: 12,
              ),
            ),
          ),
      ],
    );
  }
}