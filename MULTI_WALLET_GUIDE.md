# 多钱包测试指南 - 真实用户注册流程

## 🎯 目标

通过"连接钱包"功能，让后端**自动创建用户和钱包记录**，模拟真实用户注册流程。

---

## 📱 方法一：使用 MetaMask 创建多个账号（推荐）

### 步骤 1：在 MetaMask 中创建多个账号

#### 创建主账号（Account #1 - Alice）

1. **打开 MetaMask**
2. **点击右上角圆形图标**（显示当前账号）
3. **点击"创建账号"**
4. **输入账号名称**：`Alice`
5. **点击"创建"**
6. **记录账号地址**（例如：`0x123...abc`）

#### 创建第二个账号（Account #2 - Bob）

1. **再次点击圆形图标**
2. **点击"创建账号"**
3. **输入账号名称**：`Bob`
4. **点击"创建"**
5. **记录账号地址**

#### 重复创建更多账号

```
Account #3: Charlie
Account #4: David
Account #5: Eve
```

**提示：** MetaMask 会自动为每个账号生成不同的以太坊地址！

### 步骤 2：确保所有账号切换到 Hardhat Local 网络

1. **在每个账号下**，确认网络显示为 **"Hardhat Local"** 或 **"localhostHadhat"**
2. 如果不是，手动切换到 Hardhat Local 网络

### 步骤 3：给每个账号铸造 ETH（可选）

Hardhat 网络的所有账号默认都有无限 ETH，无需手动获取。

但如果你想从主账户转账：

```javascript
// 在浏览器控制台执行
const { ethers } = window;

// 切换到主账号（有 10000 ETH 的那个）
// 然后执行：
async function transferToAccount(toAddress, amount) {
  const provider = new ethers.BrowserProvider(window.ethereum);
  const signer = await provider.getSigner();

  const tx = await signer.sendTransaction({
    to: toAddress,
    value: ethers.parseEther(amount.toString())
  });

  console.log("Transaction hash:", tx.hash);
  await tx.wait();
  console.log("✅ 转账成功!");
}

// 使用示例：
// transferToAccount("0x70997970C51812dc3A010C7d01b50e0d17dc79C8", 1); // 转账 1 ETH
```

---

## 🔧 方法二：导入 Hardhat 测试账号（适合快速测试）

### 步骤 1：导入 Hardhat 默认账号

#### 导入 Account #0（Alice）

1. **打开 MetaMask**
2. **点击右上角圆形图标 → "导入账户"**
3. **选择"输入私钥"**
4. **粘贴私钥**：
   ```
   0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80
   ```
5. **点击"导入"**
6. **重命名账号为 "Alice"**（点击账号名称旁边的铅笔图标）

#### 导入 Account #1（Bob）

私钥：
```
0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d
```

#### 导入 Account #2（Charlie）

私钥：
```
0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a
```

#### 导入 Account #3（David）

私钥：
```
0x7c852118294e51e653712a81e05800f419141751be58f605c371e15141b007a6
```

#### 导入 Account #4（Eve）

私钥：
```
0x47e179ec197488593b187f80a00eb0da91f1b9d0b13f8733639f19c30a34926a
```

### 步骤 2：确认网络

确保所有导入的账号都连接到 **Hardhat Local**（Chain ID: 31337）

---

## 🌐 真实注册流程测试

### 流程图

```
用户打开前端
    ↓
点击"连接钱包"按钮
    ↓
MetaMask 弹出确认
    ↓
用户确认连接
    ↓
前端获取钱包地址
    ↓
前端请求后端 /user/nonce
    ↓
后端返回随机 nonce
    ↓
用户用钱包签名 nonce
    ↓
前端发送签名到后端 /user/login
    ↓
后端验证签名
    ↓
后端自动创建用户（如果不存在）
后端自动创建钱包记录
    ↓
返回 JWT token
    ↓
登录成功！
```

### 具体操作步骤

#### 测试账号 #1（Alice）注册

1. **确保 Alice 账号在 MetaMask 中选中**
2. **打开前端**：`http://localhost:5173`
3. **点击"连接钱包"** 按钮
4. **MetaMask 弹出确认** → 点击"下一步" → "连接"
5. **MetaMask 弹出签名请求** → 点击"签名"
6. **前端显示登录成功** ✅
7. **检查数据库**：应该看到 Alice 的记录已自动创建

#### 测试账号 #2（Bob）注册

1. **在 MetaMask 中切换到 Bob 账号**
   - 点击 MetaMask 顶部的账号名称
   - 选择 "Bob"
2. **刷新前端页面**（或使用无痕模式）
3. **点击"连接钱包"**
4. **重复签名流程**
5. **登录成功** ✅
6. **检查数据库**：看到 Bob 的记录

#### 重复测试其他账号

为 Charlie、David、Eve 重复以上步骤。

---

## 🗄️ 验证数据库记录

### 查看所有注册用户

```sql
SELECT
    u.id,
    u.uid,
    u.nickname,
    u.created_time,
    uw.wallet_address,
    uw.chain_id
FROM user u
LEFT JOIN user_wallet uw ON u.id = uw.user_id
WHERE u.delete_flag = 0
ORDER BY u.id DESC;
```

**预期结果：**

```
+----+---------------+----------+---------------------+-----------------------------------+----------+
| id | uid           | nickname | created_time        | wallet_address                    | chain_id |
+----+---------------+----------+---------------------+-----------------------------------+----------+
|  1 | uid_alice_xxx | alice    | 2026-01-23 12:00:00 | 0xf39Fd6e51aad88F6F4ce6aB...   |    31337 |
|  2 | uid_bob_xxx   | bob      | 2026-01-23 12:05:00 | 0x70997970C51812dc3A010C...   |    31337 |
|  3 | uid_charlie_xx| charlie  | 2026-01-23 12:10:00 | 0x3C44CdDdB6a900fa2b585d...   |    31337 |
+----+---------------+----------+---------------------+-----------------------------------+----------+
```

### 查看每个用户的钱包数量

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

## 🔍 后端 API 说明

### 1. 获取 Nonce

**接口：** `POST /user/nonce`

**请求：**
```json
{
  "walletAddress": "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
  "chainId": 31337
}
```

**响应：**
```json
{
  "success": true,
  "code": 1000,
  "message": "success",
  "data": {
    "nonce": "random_nonce_string_12345",
    "expiredAt": "2026-01-23T12:05:00"
  }
}
```

### 2. 钱包登录

**接口：** `POST /user/login`

**请求：**
```json
{
  "walletAddress": "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
  "chainId": 31337,
  "signature": "0xabc123...",
  "nickname": "alice",
  "avatar": "https://..."
}
```

**响应：**
```json
{
  "success": true,
  "code": 1000,
  "message": "success",
  "data": {
    "token": "jwt_token_here",
    "user": {
      "id": 1,
      "uid": "uid_alice_xxx",
      "nickname": "alice",
      "avatar": "https://..."
    }
  }
}
```

**后端逻辑：**
1. 验证签名是否正确
2. 检查钱包地址是否已注册
3. 如果未注册：
   - 自动创建 user 记录
   - 自动创建 user_wallet 记录
4. 如果已注册：
   - 更新最后登录时间
5. 返回 JWT token

---

## 🎨 前端连接钱包代码示例

### 步骤 1：请求 Nonce

```javascript
async function getNonce(walletAddress) {
  const response = await fetch('http://localhost:9999/user/nonce', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      walletAddress: walletAddress,
      chainId: 31337
    })
  });

  const data = await response.json();
  return data.data.nonce;
}
```

### 步骤 2：请求签名

```javascript
async function signMessage(nonce) {
  const provider = new ethers.BrowserProvider(window.ethereum);
  const signer = await provider.getSigner();

  const message = `Sign this message to verify your identity:\n\nNonce: ${nonce}`;
  const signature = await signer.signMessage(message);

  return signature;
}
```

### 步骤 3：发送登录请求

```javascript
async function loginWithWallet(walletAddress, signature, nickname, avatar) {
  const response = await fetch('http://localhost:9999/user/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      walletAddress: walletAddress,
      chainId: 31337,
      signature: signature,
      nickname: nickname,
      avatar: avatar
    })
  });

  const data = await response.json();
  return data.data;
}
```

### 步骤 4：完整流程

```javascript
async function connectWallet() {
  try {
    // 1. 连接 MetaMask
    const provider = new ethers.BrowserProvider(window.ethereum);
    const signer = await provider.getSigner();
    const walletAddress = await signer.getAddress();

    console.log("钱包地址:", walletAddress);

    // 2. 获取 nonce
    const nonce = await getNonce(walletAddress);
    console.log("获取 nonce:", nonce);

    // 3. 请求签名
    const signature = await signMessage(nonce);
    console.log("签名完成");

    // 4. 登录
    const result = await loginWithWallet(
      walletAddress,
      signature,
      "Alice",  // 昵称
      "https://api.dicebear.com/7.x/avataaars/svg?seed=alice"  // 头像
    );

    console.log("登录成功:", result);

    // 5. 保存 token
    localStorage.setItem('token', result.token);
    localStorage.setItem('user', JSON.stringify(result.user));

    alert("✅ 登录成功！后端已自动创建用户记录。");

  } catch (error) {
    console.error("连接失败:", error);
    alert("❌ 连接失败: " + error.message);
  }
}
```

---

## 🧪 完整测试流程

### 测试场景 1：第一个用户注册

**前置条件：**
- 后端正在运行（端口 9999）
- 前端正在运行（端口 5173）
- MetaMask 已安装并连接到 Hardhat Local

**步骤：**

1. **在 MetaMask 中切换到 Alice 账号**
2. **打开前端**：`http://localhost:5173`
3. **点击"连接钱包"**
4. **MetaMask 弹窗 → 点击"连接"**
5. **MetaMask 再次弹窗 → 点击"签名"**
6. **前端显示登录成功**
7. **在数据库中验证**：
   ```sql
   SELECT * FROM user WHERE nickname = 'alice';
   SELECT * FROM user_wallet WHERE wallet_address = '0xf39...';
   ```

### 测试场景 2：第二个用户注册

**步骤：**

1. **在 MetaMask 切换到 Bob 账号**
2. **打开无痕窗口**（或清除缓存）
3. **重复连接流程**
4. **验证数据库中有两个用户**

### 测试场景 3：同一用户重复登录

**步骤：**

1. **使用 Alice 账号再次连接**
2. **应该登录成功，但不会创建新用户**
3. **数据库中 Alice 的记录仍只有 1 条**

---

## 📝 测试清单

- [ ] MetaMask 中创建了 5 个账号
- [ ] 所有账号都连接到 Hardhat Local 网络
- [ ] 后端服务正在运行
- [ ] 前端服务正在运行
- [ ] Alice 账号成功注册 → 数据库有记录
- [ ] Bob 账号成功注册 → 数据库有记录
- [ ] Charlie 账号成功注册 → 数据库有记录
- [ ] David 账号成功注册 → 数据库有记录
- [ ] Eve 账号成功注册 → 数据库有记录
- [ ] 同一账号重复登录不会创建重复记录
- [ ] 每个用户都有独立的 user_id
- [ ] 每个用户都有对应的 user_wallet 记录

---

## 🎉 完成效果

测试完成后，你的数据库中应该有：

```
user 表：
├── id=1, nickname="alice", wallet="0xf39..."
├── id=2, nickname="bob", wallet="0x7099..."
├── id=3, nickname="charlie", wallet="0x3C44..."
├── id=4, nickname="david", wallet="0x90F7..."
└── id=5, nickname="eve", wallet="0x15d3..."

user_wallet 表：
├── user_id=1, wallet_address="0xf39..."
├── user_id=2, wallet_address="0x7099..."
├── user_id=3, wallet_address="0x3C44..."
├── user_id=4, wallet_address="0x90F7..."
└── user_id=5, wallet_address="0x15d3..."
```

每个用户都是通过真实的"连接钱包"流程创建的！

---

## 🆘 常见问题

### Q1: 连接钱包后前端没反应

**检查：**
1. 浏览器控制台是否有错误
2. 后端是否正在运行
3. MetaMask 是否切换到正确的网络

### Q2: 签名后后端返回错误

**可能原因：**
- nonce 过期
- 签名验证失败
- 钱包地址不匹配

**解决：**
- 重新获取 nonce
- 确认钱包地址正确
- 检查后端日志

### Q3: 数据库没有创建记录

**检查后端日志：**
- 是否有 SQL 错误
- 是否正确执行了 INSERT 语句

---

## 🚀 下一步

完成多账号测试后，你可以：

1. ✅ 测试用户之间的交易
2. ✅ 测试用户创建市场
3. ✅ 测试管理员审核
4. ✅ 测试完整的业务流程

祝测试顺利！🎉
