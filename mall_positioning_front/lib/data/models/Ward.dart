import 'Location.dart';

class Ward {
  final String id;
  final String name;
  final String? avatarUrl;
  final Location? location;
  final bool isOnline;
  final String lastUpdate;

  Ward({
    required this.id,
    required this.name,
    this.avatarUrl,
    this.location,
    required this.isOnline,
    required this.lastUpdate,
  });
}
