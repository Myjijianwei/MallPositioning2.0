// lib/pages/application_page.dart
import 'package:flutter/material.dart';
import '../../../core/services/ApplicationService.dart';
import '../../../data/models/Application.dart';

class GuardianBindRequestsPage extends StatefulWidget {
  const GuardianBindRequestsPage({Key? key}) : super(key: key);

  @override
  _GuardianBindRequestsPageState createState() => _GuardianBindRequestsPageState();
}

class _GuardianBindRequestsPageState extends State<GuardianBindRequestsPage> {
  final ApplicationService _applicationService = ApplicationService();
  final TextEditingController _wardDeviceIdController = TextEditingController();

  List<Application> _applications = [];
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadApplications();
  }

  Future<void> _loadApplications() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final applications = await _applicationService.getApplications();
      setState(() {
        _applications = applications;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('加载申请记录失败: ${e.toString()}')),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _submitApplication() async {
    if (_wardDeviceIdController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请输入被监护人设备ID')),
      );
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      await _applicationService.submitApplication(_wardDeviceIdController.text);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('申请提交成功')),
      );
      _wardDeviceIdController.clear();
      await _loadApplications();
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('提交申请失败: ${e.toString()}')),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _handleApplicationAction(int applicationId, bool isApproved) async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final success = await _applicationService.confirmApplication(
        applicationId,
        isApproved,
      );
      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('申请已${isApproved ? '通过' : '拒绝'}')),
        );
        await _loadApplications();
      }
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('操作失败: ${e.toString()}')),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Color _getStatusColor(String status) {
    switch (status.toLowerCase()) {
      case 'pending':
        return Colors.orange;
      case 'approved':
        return Colors.green;
      case 'rejected':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('设备绑定申请管理'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // 提交申请卡片
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Text(
                      '发送绑定申请',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: _wardDeviceIdController,
                      decoration: const InputDecoration(
                        labelText: '被监护人设备ID',
                        border: OutlineInputBorder(),
                      ),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _isLoading ? null : _submitApplication,
                      child: _isLoading
                          ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                          : const Text('提交申请'),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            // 申请记录卡片
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Text(
                      '申请记录',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    if (_errorMessage != null)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 16),
                        child: Text(
                          _errorMessage!,
                          style: const TextStyle(color: Colors.red),
                        ),
                      ),
                    _isLoading
                        ? const Center(child: CircularProgressIndicator())
                        : _applications.isEmpty
                        ? const Center(child: Text('没有申请记录'))
                        : ListView.builder(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      itemCount: _applications.length,
                      itemBuilder: (context, index) {
                        final application = _applications[index];
                        return Card(
                          margin: const EdgeInsets.only(bottom: 8),
                          child: Padding(
                            padding: const EdgeInsets.all(12.0),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text(
                                      '申请ID: ${application.id}',
                                      style: const TextStyle(
                                          fontWeight: FontWeight.bold),
                                    ),
                                    Chip(
                                      label: Text(
                                        application.status,
                                        style: TextStyle(
                                          color: _getStatusColor(application.status),
                                        ),
                                      ),
                                      backgroundColor: _getStatusColor(application.status)
                                          .withOpacity(0.1),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 8),
                                Text('被监护人设备ID: ${application.wardDeviceId}'),
                                const SizedBox(height: 8),
                                Text('申请时间: ${application.createdAt.toString()}'),
                                if (application.status == 'pending')
                                  const SizedBox(height: 8),
                                if (application.status == 'pending')
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.end,
                                    children: [
                                      TextButton(
                                        onPressed: () => _handleApplicationAction(
                                            application.id!, false),
                                        child: const Text('拒绝'),
                                        style: TextButton.styleFrom(
                                          foregroundColor: Colors.red,
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      ElevatedButton(
                                        onPressed: () => _handleApplicationAction(
                                            application.id!, true),
                                        child: const Text('通过'),
                                      ),
                                    ],
                                  ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _wardDeviceIdController.dispose();
    super.dispose();
  }
}