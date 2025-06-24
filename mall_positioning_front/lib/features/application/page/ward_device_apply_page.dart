import 'package:flutter/material.dart';
import 'package:mall_positioning_front/core/network/dio_service.dart';
import 'package:mall_positioning_front/core/services/auth_service.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

class WardDeviceApplyPage extends StatefulWidget {
  const WardDeviceApplyPage({super.key});

  @override
  State<WardDeviceApplyPage> createState() => _WardDeviceApplyPageState();
}

class _WardDeviceApplyPageState extends State<WardDeviceApplyPage> {
  // 表单控制器
  final TextEditingController _deviceIdController = TextEditingController(); // 设备ID输入框
  final _formKey = GlobalKey<FormState>(); // 表单验证key

  // 状态变量
  bool _isLoading = false; // 加载状态
  bool _hasRequestedId = false; // 是否已申请过设备ID
  String? _userEmail; // 用户邮箱缓存

  @override
  void initState() {
    super.initState();
    _initializePage(); // 初始化页面
  }

  @override
  void dispose() {
    // 释放控制器资源
    _deviceIdController.dispose();
    super.dispose();
  }

  /// 初始化页面
  void _initializePage() async {
    // 1. 获取用户邮箱
    final authService = Provider.of<AuthService>(context, listen: false);
    _userEmail = authService.userEmail;

    // 2. 检查是否已申请过设备ID
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _hasRequestedId = prefs.getBool('hasRequestedDeviceId') ?? false;
    });
  }

  /// 申请设备ID
  Future<void> _requestDeviceId() async {
    // 验证邮箱是否有效
    if (_userEmail == null || _userEmail!.isEmpty) {
      _showMessage('未获取到邮箱，请重新登录', Colors.red);
      return;
    }

    setState(() => _isLoading = true);

    try {
      // 调用API申请设备ID
      final response = await DioService().dio.post(
        '/msm/applyDevice_app/$_userEmail',
      );

      // 处理响应
      if (response.data['code'] == 0) {
        _showMessage('设备ID已发送到您的邮箱，请查收', Colors.green);

        // 持久化申请状态
        final prefs = await SharedPreferences.getInstance();
        await prefs.setBool('hasRequestedDeviceId', true);

        setState(() => _hasRequestedId = true);
      } else {
        _showMessage(response.data['message'] ?? '请求失败', Colors.red);
      }
    } catch (e) {
      _showMessage('网络错误或服务器异常', Colors.red);
    } finally {
      setState(() => _isLoading = false);
    }
  }

  /// 绑定设备
  Future<void> _bindDevice() async {
    // 表单验证
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {


      // 调用绑定API
      final response = await DioService().dio.post(
        '/device/bindDevice_app',
        data: _deviceIdController.text.trim(),
      );

      // 处理响应
      if (response.data['code'] == 0) {
        _showMessage('设备绑定成功', Colors.green);

        // 清除申请状态
        final prefs = await SharedPreferences.getInstance();
        await prefs.remove('hasRequestedDeviceId');

        // 返回上级页面
        Navigator.pop(context, true);
      } else {
        _showMessage(response.data['message'] ?? '绑定失败', Colors.red);
      }
    } catch (e) {
      _showMessage('网络错误或服务器异常', Colors.red);
    } finally {
      setState(() => _isLoading = false);
    }
  }

  /// 显示提示消息
  void _showMessage(String msg, Color color) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg), backgroundColor: color),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('设备绑定'),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: _hasRequestedId
            ? _buildBindForm() // 显示绑定表单
            : _buildRequestForm(), // 显示申请表单
      ),
    );
  }

  /// 构建申请设备ID表单
  Widget _buildRequestForm() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          '申请设备ID',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 16),
        Text(
          '我们将向您的邮箱 ${_userEmail ?? '未获取邮箱'} 发送设备ID',
          style: TextStyle(color: Colors.grey[600]),
        ),
        const SizedBox(height: 24),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton(
            onPressed: _isLoading ? null : _requestDeviceId,
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
            ),
            child: _isLoading
                ? const SizedBox(
              width: 20,
              height: 20,
              child: CircularProgressIndicator(
                strokeWidth: 2,
                color: Colors.white,
              ),
            )
                : const Text('申请设备ID'),
          ),
        ),
      ],
    );
  }

  /// 构建设备绑定表单
  Widget _buildBindForm() {
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '绑定设备',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 24),
          // 设备ID输入框
          TextFormField(
            controller: _deviceIdController,
            decoration: const InputDecoration(
              labelText: '设备ID',
              hintText: '请输入邮箱中收到的设备ID',
              border: OutlineInputBorder(),
            ),
            validator: (value) => value?.isEmpty ?? true ? '请输入设备ID' : null,
          ),
          const SizedBox(height: 16),

          // 操作按钮
          Row(
            children: [
              // 重新申请按钮
              Expanded(
                child: OutlinedButton(
                  onPressed: () => setState(() => _hasRequestedId = false),
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                  child: const Text('重新申请ID'),
                ),
              ),
              const SizedBox(width: 16),
              // 绑定设备按钮
              Expanded(
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _bindDevice,
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                  child: _isLoading
                      ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  )
                      : const Text('绑定设备'),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}