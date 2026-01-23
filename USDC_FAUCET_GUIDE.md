# USDC 水龙头测试指南

## 📋 目录

1. [环境准备](#环境准备)
2. [MetaMask 配置](#metamask-配置)
3. [启动服务](#启动服务)
4. [前端使用](#前端使用)
5. [后端 API](#后端-api)
6. [常见问题](#常见问题)

---

## 🔧 环境准备

### 前置条件

- Node.js >= 16.x
- npm 或 yarn
- Hardhat
- MetaMask 浏览器插件

### 项目结构

```
lightmarket/
├── market-contract/     # 智能合约
├── market-web/          # 前端 (Vue.js)
└── market-backend/      # 后端 (Spring Boot)
```

---

## 🦊 MetaMask 配置

### 1. 添加 Hardhat Local 网络

**方法一：手动添加（推荐）**

1. 打开 MetaMask
2. 点击网络下拉菜单
3. 点击 **"添加网络"** → **"手动添加网络"**
4. 填写以下信息：

```
网络名称: Hardhat Local
新的 RPC URL: http://127.0.0.1:8545
链 ID: 31337
货币符号: ETH
```

5. 点击 **"保存"**

**方法二：使用开发者工具**

在浏览器控制台（F12）粘贴：

```javascript
window.ethereum.request({
  method: 'wallet_addEthereumChain',
  params: [{
    chainId: '0x7a69',
    chainName: 'Hardhat Local',
    nativeCurrency: {
      name: 'ETH',
      symbol: 'ETH',
      decimals: 18
    },
    rpcUrls: ['http://127.0.0.1:8545'],
    blockExplorerUrls: []
  }]
}).then(() => console.log('✅ 网络添加成功'))
  .catch(e => console.error('❌ 失败:', e));
```

### 2. 导入测试账户

**账户列表（Hardhat 默认测试账户）：**

```
Account #0: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266 (默认 10000 ETH)
Account #1: 0x70997970C51812dc3A010C7d01b50e0d17dc79C8
Account #2: 0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC
Account #3: 0x90F79bf6EB2c4f870365E785982E1f101E93b906
Account #4: 0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65
```

**导入步骤：**

1. 点击 MetaMask 右上角的圆形图标
2. 点击 **"导入账户"**
3. 选择 **"输入私钥"**
4. 输入私钥（见下方）
5. 点击 **"导入"**

**私钥列表（仅测试网使用）：**

```
Account #0: 0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80
Account #1: 0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d
Account #2: 0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a
Account #3: 0x7c852118294e51e653712a81e05800f419141751be58f605c371e15141b007a6
Account #4: 0x47e179ec197488593b187f80a00eb0da91f1b9d0b13f8733639f19c30a34926a
```

### 3. 添加 USDC 代币到 MetaMask

**步骤：**

1. 打开 MetaMask
2. 向下滚动到 **"代币"** 部分
3. 点击 **"导入代币"**
4. 选择 **"自定义代币"** 标签
5. 填写信息：

```
代币合约地址: 0x5FbDB2315678afecb367f032d93F642f64180aa3
代币符号: USDC
小数位数: 6
```

6. 点击 **"下一步"** → **"导入代币"**

添加成功后，MetaMask 会显示：
- ETH: 10,000
- USDC: 1,000,100+ （取决于铸造数量）

---

## 🚀 启动服务

### 1. 启动 Hardhat 本地节点

```bash
cd market-contract
npx hardhat node --hostname 0.0.0.0
```

**保持这个终端运行！** 你会看到：

```
Started HTTP and WebSocket JSON-RPC server at http://0.0.0.0:8545/

Accounts
========
Account #0: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266 (10000 ETH)
Private Key: 0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80
...
```

### 2. 部署智能合约（如果还未部署）

```bash
# 新开一个终端
cd market-contract

# 部署 USDC 合约
npx hardhat run scripts/deploy_usdc.js --network localhost
```

输出示例：

```
Deploying MockUSDC...
MockUSDC deployed to: 0x5FbDB2315678afecb367f032d93F642f64180aa3
Minted 1000000.0 USDC to deployer: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266
```

### 3. 启动后端

```bash
# 新开一个终端
cd market-backend
mvn spring-boot:run
```

等待看到：

```
Started MarketBackendApplication in X.XXX seconds
```

### 4. 启动前端

```bash
# 新开一个终端
cd market-web
npm run dev
```

访问：`http://localhost:5173`

---

## 💻 前端使用

### 访问用户管理页面

1. 打开浏览器访问：`http://localhost:5173`
2. 登录管理员账号
3. 点击顶部导航的 **"👥 User Management"** tab

### 网络状态检查

页面会显示当前网络状态：

```
✅ 已连接 Hardhat Local (0xf39...2266)
```

如果显示警告 ⚠️：
- 点击 **"连接钱包"** 按钮
- 或点击 **"切换网络"** 按钮

### 查询用户余额

1. 在搜索框输入钱包地址：
   ```
   0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266
   ```

2. 点击 **"🔍 搜索"**

3. 查看结果：
   ```
   用户地址: 0xf39...2266
   用户昵称: alice
   USDC 余额: 1,000,100 USDC
   ```

### 铸造 USDC（Mock）

1. 点击用户卡片右侧的 **"🪙 Mock USDC"** 按钮

2. 在弹窗中选择数量：
   - 快速选项：100、1,000、10,000、100,000
   - 或输入自定义数量

3. 点击 **"✅ 确认铸造"**

4. MetaMask 会弹出确认窗口：
   - 查看交易信息
   - 点击 **"确认"**

5. 等待交易完成（几秒钟）

6. 成功提示：
   ```
   ✅ 成功铸造 100 USDC!
   新余额: 1,000,200 USDC
   ```

7. 在 MetaMask 中查看更新后的余额

### 完整流程示例

```
1. 输入地址: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266
2. 点击搜索 → 显示余额: 1,000,100 USDC
3. 点击 Mock USDC
4. 选择数量: 1,000
5. 点击确认铸造
6. MetaMask 确认交易
7. 交易完成！
8. 新余额: 1,001,100 USDC ✅
```

---

## 🔌 后端 API

### 查询钱包信息

**接口：** `POST /api/admin/wallet/query`

**请求：**

```bash
curl -X POST http://localhost:9999/api/admin/wallet/query \
  -H "Content-Type: application/json" \
  -d '{
    "walletAddress": "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"
  }'
```

**成功响应（200 OK）：**

```json
{
  "success": true,
  "code": 1000,
  "message": "success",
  "data": {
    "walletAddress": "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
    "userId": 1,
    "nickname": "alice",
    "avatar": "https://example.com/avatar.png",
    "chainId": 31337,
    "walletType": "metamask",
    "isPrimary": 1,
    "createdTime": "2026-01-23 12:00:00"
  }
}
```

**错误响应（钱包不存在）：**

```json
{
  "success": false,
  "code": 1001,
  "message": "钱包不存在于系统中"
}
```

**前端调用示例：**

```javascript
const response = await fetch('http://localhost:9999/api/admin/wallet/query', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    walletAddress: '0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266'
  })
});

const data = await response.json();
if (data.success && data.code === 1000) {
  console.log('用户昵称:', data.data.nickname);
}
```

---

## 🔧 常见问题

### Q1: MetaMask 显示"未识别的链 ID"

**原因：** MetaMask 还没有添加 Hardhat Local 网络

**解决：**
1. 在 MetaMask 中手动添加网络
2. 填写 RPC URL: `http://127.0.0.1:8545`
3. 填写 Chain ID: `31337`
4. 点击"保存"

### Q2: 前端显示"网络错误：当前 Chain ID 11155111"

**原因：** 当前连接的是 Sepolia 测试网，不是 Hardhat 本地网

**解决：**
1. 打开 MetaMask
2. 点击网络下拉菜单
3. 选择 **"Hardhat Local"** 或 **"localhostHadhat"**
4. 刷新前端页面

### Q3: 点击"确认铸造"后一直 loading

**原因：** 交易没有发送或被 MetaMask 拦截

**解决：**
1. 打开浏览器控制台（F12）查看错误
2. 检查 MetaMask 设置：
   - 设置 → 安全与隐私 → 关闭"清除活动标签数据"
   - 设置 → 安全与隐私 → 关闭"使用盲签名功能"
3. 在 MetaMask 中查看是否有待确认交易

### Q4: 查询余额失败

**错误信息：** `[Faucet] 查询余额失败: 请切换到正确的网络!`

**原因：** 钱包连接到了错误的网络

**解决：**
1. 确认 MetaMask 连接到 **Hardhat Local**（Chain ID: 31337）
2. 刷新前端页面
3. 重新查询

### Q5: MetaMask 看不到 USDC 余额

**原因：** USDC 是自定义代币，需要手动添加

**解决：**
1. 在 MetaMask 中点击"导入代币"
2. 粘贴合约地址：`0x5FbDB2315678afecb367f032d93F642f64180aa3`
3. 点击"下一步" → "导入代币"

### Q6: Hardhat 节点停止了

**症状：** 交易一直 pending，前端报错

**检查：**
```bash
lsof -i :8545
```

**解决：**
```bash
cd market-contract
npx hardhat node --hostname 0.0.0.0
```

### Q7: 交易失败 "gas required exceeds allowance"

**原因：** 账户没有足够的 ETH 支付 gas

**解决：**
1. 确认使用的是 Hardhat 测试账户（有 10000 ETH）
2. 检查 MetaMask 中 ETH 余额
3. 如果余额不足，切换到 Account #0

### Q8: 前端无法连接钱包

**错误：** `请安装 MetaMask 钱包`

**解决：**
1. 安装 MetaMask 浏览器插件
2. 刷新页面
3. 点击"连接钱包"

### Q9: 铸造后余额没有更新

**原因：** 交易还在确认中

**解决：**
1. 等待几秒钟
2. 在 MetaMask 中查看交易状态
3. 点击"重新查询"或刷新页面

### Q10: 切换网络失败 {code: 4902}

**错误：** `Unrecognized chain ID "0xc35"`

**原因：** MetaMask 中没有这个网络

**解决：**
1. 不要使用前端的"切换网络"按钮
2. 直接在 MetaMask 中手动切换到 **"Hardhat Local"**
3. 如果没有，先添加网络再切换

---

## 📊 测试场景

### 场景 1：给已注册用户 Mock USDC

**前置条件：**
- 用户已在数据库注册
- 钱包地址：`0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266`
- 当前余额：1,000,100 USDC

**步骤：**
1. 搜索用户地址
2. 显示用户信息（昵称、余额等）
3. 点击 Mock USDC
4. 输入数量：10,000
5. 确认交易
6. MetaMask 确认
7. 等待完成
8. 新余额：1,010,100 USDC ✅

### 场景 2：查询未注册用户

**步骤：**
1. 搜索未注册的地址：`0x70997970C51812dc3A010C7d01b50e0d17dc79C8`
2. 显示错误：**"钱包不存在于系统中"**
3. 仍然可以点击 Mock USDC 给该地址铸造（前端直接调用合约）

### 场景 3：批量测试

**测试多个账户：**

```bash
# 使用脚本批量查询余额
cd market-contract
npx hardhat run scripts/check_balance.js --network localhost
```

**输出：**
```
账户 #0: 0xf39...2266 - 余额: 1,000,100 USDC
账户 #1: 0x7099...79C8 - 余额: 0 USDC
账户 #2: 0x3C44...293BC - 余额: 0 USDC
```

---

## 🎯 完整测试清单

- [ ] MetaMask 已添加 Hardhat Local 网络
- [ ] MetaMask 已导入测试账户
- [ ] MetaMask 已添加 USDC 代币
- [ ] Hardhat 节点正在运行（端口 8545）
- [ ] Mock USDC 合约已部署
- [ ] 后端服务正在运行（端口 9999）
- [ ] 前端服务正在运行（端口 5173）
- [ ] 前端显示"✅ 已连接 Hardhat Local"
- [ ] 可以成功查询用户余额
- [ ] 可以成功铸造 USDC
- [ ] MetaMask 中可以看到 USDC 余额更新
- [ ] 后端 API 可以正常返回数据

---

## 📝 注意事项

⚠️ **仅用于测试网！**
- 本功能仅用于开发测试
- 不要在主网使用
- 测试私钥不要泄露

⚠️ **重置数据：**
- 停止 Hardhat 节点会清空所有数据
- 重新启动节点需要重新部署合约
- 数据库数据不受影响

⚠️ **Gas 费用：**
- Hardhat 本地网络 Gas 费为 0
- 测试网需要真实的测试 ETH

---

## 🆘 获取帮助

如果遇到其他问题：

1. **查看日志：**
   - 浏览器控制台（F12 → Console）
   - Hardhat 节点输出
   - 后端日志

2. **检查连接：**
   ```bash
   # 检查 Hardhat 节点
   lsof -i :8545

   # 检查后端
   lsof -i :9999

   # 检查前端
   lsof -i :5173
   ```

3. **重启服务：**
   - 重启 Hardhat 节点
   - 重新部署合约
   - 刷新前端页面

---

## 🎉 完成！

现在你可以：
- ✅ 查询用户是否在平台注册
- ✅ 查询用户 USDC 余额
- ✅ 给任意地址 Mock USDC
- ✅ 在 MetaMask 中查看 USDC 余额
- ✅ 运行完整的测试流程

祝测试顺利！🚀
