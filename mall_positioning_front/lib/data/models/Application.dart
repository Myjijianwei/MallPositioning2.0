// lib/data/models/Application.dart
class Application {
  final int? id;
  final String guardianId;
  final String wardDeviceId;
  final String status;
  final DateTime createdAt;
  final DateTime updatedAt;

  Application({
    this.id,
    required this.guardianId,
    required this.wardDeviceId,
    required this.status,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Application.fromJson(Map<String, dynamic> json) {
    return Application(
      id: json['id'] is int ? json['id'] : int.tryParse(json['id'].toString()),
      guardianId: json['guardian_id']?.toString() ?? '',
      wardDeviceId: json['ward_device_id']?.toString() ?? '',
      status: json['status']?.toString() ?? '',
      createdAt: DateTime.tryParse(json['created_at'] ?? '') ?? DateTime(1970),
      updatedAt: DateTime.tryParse(json['updated_at'] ?? '') ?? DateTime(1970),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'guardian_id': guardianId,
      'ward_device_id': wardDeviceId,
      'status': status,
      'created_at': createdAt.toIso8601String(),
      'updated_at': updatedAt.toIso8601String(),
    };
  }
}