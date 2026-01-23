# 测试账号使用指南

## 📋 测试用户列表

本系统包含 5 个测试用户，每个用户都有对应的钱包地址：

| 用户名 | 钱包地址 | Hardhat 账户 | 初始 USDC |
|--------|----------|--------------|-----------|
| alice | `0xf39...2266` | Account #0 | 1,000,000 |
| bob | `0x7099...79C8` | Account #1 | 0 |
| charlie | `0x3C44...293BC` | Account #2 | 0 |
| david | `0x90F7...b906` | Account #3 | 0 |
| eve | `0x15d3...6A65` | Account #4 | 0 |

**注意：** Alice 有两个钱包地址（测试一个用户多钱包场景）
- 主钱包：`0xf39...2266` (metamask)
- 副钱包：`0x996...4dc` (walletconnect)

---

## 🚀 快速开始

### 方法一：使用 SQL 脚本（推荐）

#### 1. 连接到数据库

```bash
# 使用 MySQL 客户端
mysql -u root -p

# 或使用 Docker
docker exec -it mysql-container mysql -u root -p
```

#### 2. 选择数据库

```sql
USE market_db;
```

#### 3. 执行测试数据脚本

```bash
# 在项目根目录
mysql -u root -p market_db < market-backend/src/main/resources/db/migration/test_data.sql
```

或在 MySQL 客户端中：

```sql
source /path/to/market-backend/src/main/resources/db/migration/test_data.sql;
```

#### 4. 验证数据

```sql
SELECT
    u.id,
    u.nickname,
    u.uid,
    uw.wallet_address,
    uw.chain_id,
    uw.wallet_type,
    uw.is_primary
FROM user u
LEFT JOIN user_wallet uw ON u.id = uw.user_id
WHERE u.delete_flag = 0
ORDER BY u.id, uw.is_primary DESC;
```

**预期输出：**

```
+----+----------+---------------+-----------------------------------+----------+-------------+------------+
| id | nickname | uid           | wallet_address                    | chain_id | wallet_type | is_primary |
+----+----------+---------------+-----------------------------------+----------+-------------+------------+
|  1 | alice    | uid_alice_001 | 0xf39...2266                      |    31337 | metamask    |          1 |
|  1 | alice    | uid_alice_001 | 0x996...4dc                       |    31337 | walletconnect |         0 |
|  2 | bob      | uid_bob_001   | 0x7099...79C8                     |    31337 | metamask    |          1 |
|  3 | charlie  | uid_char...   | 0x3C44...293BC                    |    31337 | metamask    |          1 |
|  4 | david    | uid_david_001 | 0x90F7...b906                     |    31337 | metamask    |          1 |
|  5 | eve      | uid_eve_001   | 0x15d3...6A65                     |    31337 | metamask    |          1 |
+----+----------+---------------+-----------------------------------+----------+-------------+------------+
```

---

### 方法二：批量铸造 USDC

如果数据库已经有用户数据，直接给所有测试账号铸造 USDC：

#### 1. 确保 Hardhat 节点正在运行

```bash
cd market-contract
npx hardhat node --hostname 0.0.0.0
```

#### 2. 确保合约已部署

```bash
cd market-contract
npx hardhat run scripts/deploy_usdc.js --network localhost
```

#### 3. 批量铸造 USDC

```bash
cd market-contract
npx hardhat run scripts/mint_to_all_users.js --network localhost
```

**输出示例：**

```
===== 批量铸造 USDC =====

部署者地址: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266
铸造数量: 10000.0 USDC/用户

正在铸造给 alice (0xf39...2266)...
  ✅ 成功! 余额: 1000000 → 1010000 USDC

正在铸造给 bob (0x7099...79C8)...
  ✅ 成功! 余额: 0 → 10000 USDC

正在铸造给 charlie (0x3C44...293BC)...
  ✅ 成功! 余额: 0 → 10000 USDC

...

===== 最终余额 =====
alice                0xf39...2266 余额: 1010000 USDC
bob                  0x7099...79C8 余额: 10000 USDC
charlie              0x3C44...293BC 余额: 10000 USDC
david                0x90F7...b906 余额: 10000 USDC
eve                  0x15d3...6A65 余额: 10000 USDC
alice_wallet2        0x996...4dc   余额: 10000 USDC

✅ 批量铸造完成!
```

---

## 🦊 在 MetaMask 中导入测试账号

### 导入账号到 MetaMask

#### Account #1 (Alice)

1. 打开 MetaMask
2. 点击右上角圆形图标 → **"导入账户"**
3. 选择 **"输入私钥"**
4. 粘贴私钥：
   ```
   0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80
   ```
5. 点击 **"导入"**

#### Account #2 (Bob)

私钥：
```
0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d
```

#### Account #3 (Charlie)

私钥：
```
0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a
```

#### Account #4 (David)

私钥：
```
0x7c852118294e51e653712a81e05800f419141751be58f605c371e15141b007a6
```

#### Account #5 (Eve)

私钥：
```
0x47e179ec197488593b187f80a00eb0da91f1b9d0b13f8733639f19c30a34926a
```

---

## 🧪 测试场景

### 场景 1：查询已注册用户（Alice）

**步骤：**
1. 在 User Management 页面输入：`0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266`
2. 点击搜索
3. 显示结果：
   - 用户昵称：alice
   - 钱包地址：0xf39...2266
   - 余额：1,000,000+ USDC

### 场景 2：查询已注册用户（Bob）

**步骤：**
1. 输入：`0x70997970C51812dc3A010C7d01b50e0d17dc79C8`
2. 点击搜索
3. 显示结果：
   - 用户昵称：bob
   - 钱包地址：0x7099...79C8
   - 余额：10,000 USDC（铸造后）

### 场景 3：查询未注册用户

**步骤：**
1. 输入随机地址：`0x1234567890123456789012345678901234567890`
2. 点击搜索
3. 显示错误：**"钱包不存在于系统中"**

### 场景 4：一个用户多个钱包（Alice）

**测试目标：** 验证一个用户可以有多个钱包地址

**步骤：**
1. 查询 Alice 的主钱包：`0xf39...2266` ✅ 找到
2. 查询 Alice 的副钱包：`0x996...4dc` ✅ 找到（同一个用户 ID）
3. 两个钱包都属于 alice（user_id = 1）

### 场景 5：给不同用户 Mock USDC

**步骤：**
1. 切换到 Alice 的账号 → Mock 1000 USDC → 余额增加
2. 切换到 Bob 的账号 → Mock 500 USDC → 余额增加
3. 每个用户的余额独立管理

---

## 📊 测试数据查询

### 查询所有测试用户

```sql
SELECT id, nickname, uid, avatar
FROM user
WHERE delete_flag = 0
ORDER BY id;
```

### 查询所有钱包地址

```sql
SELECT
    u.nickname,
    uw.wallet_address,
    uw.chain_id,
    uw.wallet_type,
    uw.is_primary
FROM user_wallet uw
JOIN user u ON uw.user_id = u.id
WHERE u.delete_flag = 0
ORDER BY u.id, uw.is_primary DESC;
```

### 查询特定用户的钱包

```sql
-- 查询 alice 的所有钱包
SELECT
    uw.wallet_address,
    uw.wallet_type,
    uw.is_primary,
    uw.created_time
FROM user_wallet uw
WHERE uw.user_id = 1  -- alice
ORDER BY uw.is_primary DESC;
```

### 统计每个用户的钱包数量

```sql
SELECT
    u.nickname,
    COUNT(uw.id) as wallet_count
FROM user u
LEFT JOIN user_wallet uw ON u.id = uw.user_id
WHERE u.delete_flag = 0
GROUP BY u.id, u.nickname
ORDER BY u.id;
```

---

## 🔄 重置测试数据

### 清空所有测试数据

```sql
-- 删除钱包（先删除外键关联）
DELETE FROM user_wallet WHERE user_id IN (1, 2, 3, 4, 5);

-- 删除用户
DELETE FROM user WHERE id IN (1, 2, 3, 4, 5);

-- 重置自增 ID
ALTER TABLE user AUTO_INCREMENT = 1;
ALTER TABLE user_wallet AUTO_INCREMENT = 1;
```

### 重新导入测试数据

```bash
mysql -u root -p market_db < market-backend/src/main/resources/db/migration/test_data.sql
```

---

## 🛠️ 高级用法

### 添加新的测试用户

**步骤：**

1. **插入用户记录：**
   ```sql
   INSERT INTO `user` (`uid`, `nickname`, `avatar`, `delete_flag`, `created_time`, `updated_time`)
   VALUES ('uid_frank_001', 'frank', 'https://api.dicebear.com/7.x/avataaars/svg?seed=frank', 0, NOW(3), NOW(3));
   ```

2. **插入钱包记录：**
   ```sql
   INSERT INTO `user_wallet` (`user_id`, `wallet_address`, `chain_id`, `wallet_type`, `is_primary`, `created_time`, `updated_time`)
   VALUES (LAST_INSERT_ID(), '0xYourWalletAddressHere', 31337, 'metamask', 1, NOW(3), NOW(3));
   ```

3. **给新用户铸造 USDC：**
   ```javascript
   // 在 Hardhat console 中
   const usdc = await ethers.getContractAt("MockUSDC", "0x5FbDB2315678afecb367f032d93F642f64180aa3");
   await usdc.mint("0xYourWalletAddressHere", ethers.parseUnits("10000", 6));
   ```

### 为现有用户添加第二个钱包

```sql
-- 给 alice 添加第三个钱包
INSERT INTO `user_wallet` (`user_id`, `wallet_address`, `chain_id`, `wallet_type`, `is_primary`, `created_time`, `updated_time`)
VALUES (1, '0xAnotherWalletAddress', 31337, 'metamask', 0, NOW(3), NOW(3));
```

---

## 📝 常见问题

### Q1: 执行 SQL 脚本报错 "Duplicate entry"

**原因：** 用户已存在

**解决：** 先删除旧数据
```sql
DELETE FROM user_wallet WHERE user_id IN (1, 2, 3, 4, 5);
DELETE FROM user WHERE id IN (1, 2, 3, 4, 5);
```

### Q2: 铸造 USDC 时提示 "insufficient funds"

**原因：** 部署者账户没有足够 ETH

**解决：** 使用 Hardhat 默认账户（Account #0，有 10000 ETH）

### Q3: MetaMask 无法导入账号

**原因：** 私钥格式错误或网络不对

**解决：**
- 确认复制完整的私钥（无空格）
- 确认 MetaMask 连接到 Hardhat Local 网络

### Q4: 查询用户时返回 null

**检查：**
1. 数据库中是否有该用户
2. 钱包地址是否正确（检查大小写）
3. chain_id 是否匹配（应该是 31337）

---

## ✅ 完整测试流程

### 1. 准备环境

```bash
# 启动 Hardhat
cd market-contract
npx hardhat node --hostname 0.0.0.0

# 部署合约
npx hardhat run scripts/deploy_usdc.js --network localhost

# 批量铸造 USDC
npx hardhat run scripts/mint_to_all_users.js --network localhost
```

### 2. 导入数据库

```bash
mysql -u root -p market_db < market-backend/src/main/resources/db/migration/test_data.sql
```

### 3. 启动后端

```bash
cd market-backend
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd market-web
npm run dev
```

### 5. 测试

1. 访问 `http://localhost:5173`
2. 登录管理员账号
3. 进入 User Management
4. 测试查询各个用户
5. 测试 Mock USDC 功能

---

## 🎉 完成！

现在你有：
- ✅ 5 个测试用户（alice, bob, charlie, david, eve）
- ✅ 每个用户都有钱包地址
- ✅ Alice 有两个钱包（测试多钱包场景）
- ✅ 所有用户都有初始 USDC 余额
- ✅ 可以在前端查询和测试

祝测试顺利！🚀
