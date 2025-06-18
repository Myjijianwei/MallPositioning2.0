import 'package:flutter/material.dart';

import '../../widgets/custom_button.dart';

class DemoLiveMonitorPage extends StatelessWidget {
  const DemoLiveMonitorPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('演示模式 - 实时监控'),
        actions: [
          _DemoBadge(),
        ],
      ),
      body: Stack(
        children: [
          // 模拟地图和虚拟设备
          _buildDemoMap(context),

          // 功能限制提示浮层
          Positioned(
            bottom: 16,
            left: 16,
            right: 16,
            child: _buildDemoLimitationBanner(context),
          ),
        ],
      ),
    );
  }

  Widget _buildDemoMap(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.map, size: 100, color: Colors.grey[300]),
          SizedBox(height: 16),
          Text(
            '虚拟监护设备位置',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          SizedBox(height: 8),
          Text(
            '纬度: 39.9042, 经度: 116.4074\n(北京天安门广场模拟数据)',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }

  Widget _buildDemoLimitationBanner(BuildContext context) {
    return Card(
      color: Colors.amber[50],
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.info, color: Colors.amber),
                SizedBox(width: 8),
                Text(
                  '演示模式限制',
                  style: Theme.of(context).textTheme.titleSmall,
                ),
              ],
            ),
            SizedBox(height: 8),
            Text(
              '您正在查看演示数据。注册后可以:',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children: [
                Chip(
                  label: Text('添加真实设备'),
                  backgroundColor: Colors.white,
                ),
                Chip(
                  label: Text('设置电子围栏'),
                  backgroundColor: Colors.white,
                ),
                Chip(
                  label: Text('接收实时警报'),
                  backgroundColor: Colors.white,
                ),
              ],
            ),
            SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: CustomButton(
                text: '立即注册',
                onPressed: () => Navigator.pushReplacementNamed(context, '/register'),
                padding: EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DemoBadge extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.grey[200],
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.supervised_user_circle, size: 16, color: Colors.grey),
          SizedBox(width: 4),
          Text('演示模式', style: TextStyle(fontSize: 12)),
        ],
      ),
    );
  }
}