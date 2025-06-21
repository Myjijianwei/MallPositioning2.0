class User{
  final String id;
  final String email;
  final String userName;
  final String token;

  User({
    required this.id,
    required this.email,
    required this.userName,
    required this.token,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      email: json['email'],
      userName: json['userName'],
      token: json['token'],
    );
  }
}