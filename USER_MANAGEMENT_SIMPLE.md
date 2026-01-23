# 用户查询与 Mock USDC 功能 - 简化版

## 📋 功能概述

管理员可以通过钱包地址查询用户是否在平台注册，然后直接在前端给该钱包地址 Mock（铸造）USDC。

## 🔧 后端接口

### 查询钱包信息

**接口**: `POST /api/admin/wallet/query`

**请求参数**:
```json
{
  "walletAddress": "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"
}
```

**响应**:
```json
{
  "success": true,
  "code": 1000,
  "message": "success",
  "data": {
    "walletAddress": "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
    "userId": 1,
    "nickname": "alice",
    "avatar": "https://...",
    "chainId": 31337,
    "walletType": "metamask",
    "isPrimary": 1,
    "createdTime": "2026-01-23 12:00:00"
  }
}
```

**错误响应**（钱包不存在）:
```json
{
  "success": false,
  "code": 1001,
  "message": "钱包不存在于系统中"
}
```

## 🚀 完整流程

### 1. 启动后端

```bash
cd market-backend
mvn spring-boot:run
```

### 2. 启动前端

```bash
cd market-web
npm run dev
```

### 3. 管理员登录

访问 `http://localhost:5173/admin-login`

### 4. 进入用户管理

点击顶部导航的 **"👥 User Management"** tab

### 5. 查询用户

1. 输入钱包地址：`0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266`
2. 点击 **"🔍 搜索"**
3. 查看结果：
   - ✅ **找到用户**：显示用户信息（昵称、创建时间等）
   - ❌ **未找到**：提示"钱包不存在于系统中"

### 6. Mock USDC

1. 点击用户卡片右侧的 **"🪙 Mock USDC"** 按钮
2. 在弹窗中选择或输入数量
3. 点击 **"✅ 确认铸造"**
4. 在 MetaMask 中确认交易
5. 等待交易完成

## 📊 数据库表

### user_wallet 表

```sql
CREATE TABLE user_wallet (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  wallet_address VARCHAR(42) NOT NULL UNIQUE,
  chain_id INT NOT NULL,
  wallet_type VARCHAR(50),
  is_primary TINYINT DEFAULT 0,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_wallet_address (wallet_address)
);
```

## 🎯 测试流程

### 场景：给已注册用户 Mock USDC

```bash
# 1. 查询用户（已注册）
# 输入: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266
# 结果: 显示用户信息

# 2. 点击 Mock USDC
# 选择: 10,000 USDC
# 确认交易

# 3. MetaMask 确认
# Gas 费用: ~30,000 gas

# 4. 交易完成
# 余额更新: 10,000 USDC
```

### 场景：查询未注册用户

```bash
# 1. 查询用户（未注册）
# 输入: 0x70997970C51812dc3A010C7d01b50e0d17dc79C8
# 结果: "钱包不存在于系统中"

# 2. 仍然可以 Mock USDC
# 点击 "🪙 Mock USDC" 按钮
# 选择数量并确认
# 完成铸造
```

## ✅ 关键改动

### 后端

1. **新增接口**: `POST /api/admin/wallet/query`
   - 只查询数据库，不调用合约
   - 返回用户注册信息

2. **新增文件**:
   - `WalletQuery.java` - 查询请求
   - `WalletVO.java` - 响应对象

### 前端

1. **UserManagement.vue**:
   - 调用后端 API 查询用户
   - 使用 `faucet.js` 的 `mintTestUSDC` 函数 Mock USDC
   - 前端直接调用合约，不经过后端

## 🔗 文件清单

### 后端

```
market-backend/src/main/java/com/market/business/
├── controller/
│   └── AdminController.java          ✅ 添加查询接口
├── service/
│   ├── AdminService.java              ✅ 添加查询方法
│   └── impl/
│       └── AdminServiceImpl.java       ✅ 实现查询逻辑
├── query/
│   └── WalletQuery.java               ✅ 新增
└── vo/
    └── WalletVO.java                   ✅ 新增
```

### 前端

```
market-web/src/
├── components/
│   ├── UserManagement.vue             ✅ 用户管理组件
│   └── AdminDashboard.vue             ✅ 已集成
├── utils/
│   └── faucet.js                      ✅ Mock USDC 工具
└── vite.config.js                     ✅ 路径别名配置
```

## 🎉 完成！

现在整个流程已经打通：
1. 后端提供查询接口
2. 前端调用合约 Mock USDC
3. 测试网可以完整运行
