import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:mall_positioning_front/services/auth_service.dart';
import 'package:provider/provider.dart';

/// 个人资料编辑页面
/// 功能：
/// 1. 修改头像
/// 2. 编辑昵称和邮箱
/// 3. 换绑邮箱验证
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
  String? _avatarPath;

  @override
  void initState() {
    super.initState();
    // 从AuthService初始化用户数据
    final authService = context.read<AuthService>();
    _nicknameController = TextEditingController(text: authService.userNickname);
    _emailController = TextEditingController(text: authService.userEmail);
    _avatarPath = authService.userAvatar;
  }

  @override
  void dispose() {
    _nicknameController.dispose();
    _emailController.dispose();
    super.dispose();
  }

  /// 从相册选择图片
  Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);

    if (pickedFile != null) {
      setState(() => _avatarPath = pickedFile.path);
      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('头像已更新'))
      );
      // TODO: 实际项目需上传到服务器
    }
  }

  /// 提交修改
  Future<void> _submitChanges() async {
    if (!_formKey.currentState!.validate()) return;

    try {
      final authService = context.read<AuthService>();
      // TODO: 调用实际更新接口
      // await authService.updateProfile(
      //   nickname: _nicknameController.text,
      //   email: _emailController.text,
      //   avatar: _avatarPath,
      // );

      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('个人信息已保存'))
      );
      Navigator.pop(context);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('保存失败: $e'))
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
              // 头像编辑区
              _buildAvatarSection(),
              const SizedBox(height: 40),
              // 昵称编辑
              _buildNicknameField(),
              const SizedBox(height: 20),
              // 邮箱编辑
              _buildEmailField(),
              const SizedBox(height: 30),
              // 换绑邮箱按钮
              _buildEmailUpdateButton(),
            ],
          ),
        ),
      ),
    );
  }

  /// 构建头像编辑区域
  Widget _buildAvatarSection() {
    return Column(
      children: [
        GestureDetector(
          onTap: _pickImage,
          child: CircleAvatar(
            radius: 50,
            backgroundImage: _avatarPath != null
                ? FileImage(File(_avatarPath!))
                : null,
            child: _avatarPath == null
                ? const Icon(Icons.person, size: 50)
                : null,
          ),
        ),
        const SizedBox(height: 20),
        Text(
          '点击修改头像',
          style: Theme.of(context).textTheme.bodySmall,
        ),
      ],
    );
  }

  /// 构建昵称输入框
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

  /// 构建邮箱输入框
  Widget _buildEmailField() {
    return TextFormField(
      controller: _emailController,
      decoration: const InputDecoration(
        labelText: '邮箱',
        prefixIcon: Icon(Icons.email_outlined),
        border: OutlineInputBorder(),
      ),
      keyboardType: TextInputType.emailAddress,
      validator: (value) {
        if (value?.isEmpty ?? true) return '请输入邮箱';
        if (!value!.contains('@')) return '请输入有效的邮箱地址';
        return null;
      },
    );
  }

  /// 构建换绑邮箱按钮
  Widget _buildEmailUpdateButton() {
    return OutlinedButton.icon(
      icon: const Icon(Icons.email),
      label: const Text('发送验证邮件'),
      onPressed: () {
        // TODO: 实现邮箱验证逻辑
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('验证邮件已发送'))
        );
      },
    );
  }
}