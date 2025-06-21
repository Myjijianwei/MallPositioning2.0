# mall_positioning_front

# 目录结构
lib/
├── core/                        # 核心基础功能
│   ├── services/                # 服务层
│   │   ├── auth_service.dart    # 认证服务
│   │   ├── message_service.dart # 消息服务
│   │   └── navigation_service.dart
│   ├── utils/                   # 工具类
│   │   ├── auth_route_guard.dart
│   │   └── constants.dart
│   └── widgets/                 # 全局通用组件
│       ├── custom_button.dart
│       ├── custom_text_field.dart
│       └── verification_code_input.dart
│
├── data/                        # 数据层
│   ├── models/                  # 数据模型
│   └── repositories/            # 数据仓库(建议添加)
│
├── features/                    # 功能模块
│   ├── auth/                    # 认证模块
│   │   ├── pages/               # 页面
│   │   │   ├── login_page.dart  
│   │   │   └── register_page.dart
│   │   └── auth_routes.dart     # 模块路由
│   │
│   ├── home/                    # 首页模块
│   │   └── home_page.dart       # 
│   │
│   └── profile/                 # 个人资料模块
│       ├── profile_info_page.dart
│       └── profile_page.dart
│
└── main.dart                    # 应用入口