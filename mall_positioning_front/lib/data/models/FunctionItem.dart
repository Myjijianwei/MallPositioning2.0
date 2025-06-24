import 'package:flutter/cupertino.dart';

/// 功能项模型
class FunctionItem {
  final IconData icon;
  final String title;
  final Color color;
  final String? description;
  final VoidCallback? onTap;

  const FunctionItem({
    required this.icon,
    required this.title,
    required this.color,
    this.description,
    this.onTap,
  });
}