
# 🍔 HiEat — 智慧餐饮点餐系统

一个集**用户点餐端**、**员工管理端**、**后台管理端**于一体的全栈餐饮外卖管理系统。

## 🏗 项目架构

```
HiEat/
├── HiEatServe/              # Java 后端（Spring Boot）
│   ├── common/              # 公共模块（工具类、常量、异常）
│   ├── pojo/                # 实体类
│   └── server/              # 服务端（Controller、Service、Mapper）
├── FoodTimeUniapp/          # 用户点餐端（UniApp 跨平台）
├── FoodTimeEmployee/        # 员工管理端（Vue3 + TS）
├── FoodTimeAdmin/           # 后台管理端（Vue3 + TS）
└── food_time2.sql           # 数据库初始化脚本
```

## 🛠 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.3 | 核心框架 |
| MyBatis | 2.2.0 | ORM 持久层 |
| MySQL | — | 关系型数据库 |
| Redis | — | 缓存 / Session |
| Druid | 1.2.1 | 数据库连接池 |
| JWT (jjwt) | 0.9.1 | 无状态登录认证 |
| Knife4j | 3.0.2 | API 接口文档（Swagger） |
| 阿里云 OSS | 3.10.2 | 文件/图片存储 |
| 微信支付 | API V3 | 支付功能 |
| Apache POI | 3.16 | Excel 导入导出 |
| Lombok | 1.18.36 | 简化代码 |

### 前端

| 项目 | 技术栈 |
|------|--------|
| **管理端** (FoodTimeAdmin) | Vue 3 + Vite + TypeScript + Element Plus + Pinia |
| **员工端** (FoodTimeEmployee) | Vue 3 + Vite + TypeScript + Element Plus + ECharts |
| **用户点餐端** (FoodTimeUniapp) | UniApp (Vue3) → 微信小程序 / H5 / Android / iOS |

## ✨ 功能模块

### 🧑 用户点餐端 (UniApp)

- 微信授权登录
- 菜品/套餐浏览、搜索
- 购物车管理
- 在线下单 & 微信支付
- 收货地址管理
- 订单追踪 & 催单
- 评价与反馈

### 👨‍🍳 员工管理端 (Web)

- 接单 / 拒单处理
- 订单状态流转（待处理 → 制作中 → 已完成）
- 菜品管理（CRUD、起售/停售）
- 套餐管理
- 数据报表看板（ECharts 可视化）
- 当日营业额、订单量统计

### 🖥 后台管理端 (Web)

- 分类管理（菜品分类 / 店铺分类）
- 轮播图管理
- 员工账号管理
- 区域管理（省/市/区）
- 公告管理
- 店铺信息配置
- 订单总览管理
- 评价管理

## 🚀 快速开始

### 环境要求

- JDK 8+
- MySQL 5.7+
- Redis 6+
- Maven 3.6+
- Node.js 16+
- 微信开发者工具（用于运行小程序端）

### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS hieat DEFAULT CHARSET utf8mb4;

-- 导入初始化脚本
source food_time2.sql;
```

### 2. 启动后端

```bash
cd HiEatServe

# 修改 application-dev.yml 中的数据库和 Redis 配置
# 填入你的真实信息（数据库密码、OSS 密钥、微信支付密钥等）

# 启动
mvn clean install -DskipTests
cd server
mvn spring-boot:run
```

启动后访问 Swagger 文档：`http://localhost:8080/doc.html`

### 3. 启动管理端

```bash
cd FoodTimeAdmin
npm install
npm run dev
```

### 4. 启动员工端

```bash
cd FoodTimeEmployee
npm install
npm run dev
```

### 5. 启动用户端（微信小程序）

```bash
cd FoodTimeUniapp

# 在 manifest.json 中配置你自己的微信小程序 appid

# H5 预览
npm run dev:h5

# 打包为微信小程序
npm run build:mp-weixin
```

## ⚠️ 安全提示

配置文件中的密钥已替换为占位符（`your-xxx`），实际开发请自行填入真实值：

- `HiEatServe/server/src/main/resources/application-dev.yml` — 开发环境配置
- `HiEatServe/server/src/main/resources/template/application-pro.yml` — 生产环境模板
- `HiEatServe/server/src/main/resources/application.yml` — JWT 签名密钥

**请勿将含真实密钥的配置文件提交到 Git！**

## 📄 License

[MIT](LICENSE)
