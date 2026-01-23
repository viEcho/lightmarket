# 测试 USDC 水龙头使用指南

## 🪙 概述

本项目提供了三种方式来获取测试 USDC，用于在测试网络中进行交易测试。

## 🎯 三种获取方式

### 方式1：命令行脚本（推荐用于测试）

**文件位置**: `market-contract/scripts/mint_usdc.js`

#### 使用步骤

1. **部署 USDC 合约**（如果还没部署）
```bash
cd market-contract
npx hardhat run scripts/deploy_usdc.js --network localhost
```

2. **获取 USDC 合约地址**
```bash
# 部署后会输出类似：
# MockUSDC deployed to: 0x5FbDB2315678afecb367f032d93F642f64180aa3
```

3. **铸造测试 USDC**
```bash
# 给自己铸造 10,000 USDC
USDC_ADDRESS=0x5FbDB... AMOUNT=10000 npx hardhat run scripts/mint_usdc.js --network localhost

# 给指定地址铸造
USDC_ADDRESS=0x5FbDB... TO_ADDRESS=0x709... AMOUNT=50000 npx hardhat run scripts/mint_usdc.js --network localhost
```

#### 环境变量

| 变量 | 必需 | 默认值 | 说明 |
|------|------|--------|------|
| USDC_ADDRESS | ✅ | - | USDC 合约地址 |
| TO_ADDRESS | ❌ | 第一个签名者 | 接收地址 |
| AMOUNT | ❌ | 10000 | 铸造数量 |

#### 示例输出

```
========================================
🪙 铸造测试 USDC
========================================
USDC 合约地址: 0x5FbDB2315678afecb367f032d93F642f64180aa3
铸造数量: 10000
========================================

发送账户: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266
接收账户: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266

铸造前余额: 0.0 USDC

⏳ 正在铸造...
✅ 交易已提交: 0xabc...
✅ 交易已确认
Gas 使用: 32456

铸造后余额: 10000.0 USDC
增加数量: 10000.0 USDC

========================================
🎉 铸造成功!
========================================
```

---

### 方式2：前端工具函数

**文件位置**: `market-web/src/utils/faucet.js`

#### 在代码中使用

```javascript
import { mintTestUSDC, getUSDCBalance } from '@/utils/faucet'

// 铸造 10,000 USDC
const result = await mintTestUSDC(10000, {
  onTransactionHash: (hash) => {
    console.log('交易已提交:', hash)
  },
  onReceipt: (receipt) => {
    console.log('交易已确认')
  }
})

if (result.success) {
  console.log('铸造成功!', result.minted, 'USDC')
  console.log('余额:', result.balanceAfter)
}

// 查询余额
const balance = await getUSDCBalance()
console.log('当前余额:', balance, 'USDC')
```

#### 返回值

```javascript
{
  success: true,
  txHash: "0xabc...",
  balanceBefore: "0",
  balanceAfter: "10000",
  minted: "10000"
}
```

---

### 方式3：前端水龙头组件

**文件位置**: `market-web/src/components/TestFaucet.vue`

#### 在页面中使用

```vue
<script setup>
import TestFaucet from '@/components/TestFaucet.vue'
</script>

<template>
  <div>
    <h1>测试页面</h1>
    <TestFaucet />
  </div>
</template>
```

#### 功能特性

- ✅ 显示当前 USDC 余额
- ✅ 快速铸造选项（100/1,000/10,000/100,000 USDC）
- ✅ 自定义数量铸造
- ✅ 实时交易状态显示
- ✅ 自动刷新余额
- ✅ 交易浏览器链接

---

## 🚀 完整测试流程

### 场景：测试市场创建和交易

#### 1. 启动本地节点

```bash
cd market-contract
npx hardhat node
```

#### 2. 部署合约

**终端1**（保持 hardhat node 运行）
```bash
cd market-contract

# 部署 USDC
npx hardhat run scripts/deploy_usdc.js --network localhost
# 输出: MockUSDC deployed to: 0x5FbDB...

# 部署 Factory
npx hardhat run scripts/deploy_factory.js --network localhost
# 输出: MarketFactory deployed to: 0xaBc...

# 记录合约地址
```

#### 3. 铸造测试 USDC

```bash
# 设置环境变量
export USDC_ADDRESS=0x5FbDB2315678afecb367f032d93F642f64180aa3

# 给自己铸造 100,000 USDC（足够测试）
USDC_ADDRESS=0x5FbDB... AMOUNT=100000 npx hardhat run scripts/mint_usdc.js --network localhost
```

#### 4. 更新前端配置

**文件**: `market-web/.env`

```env
VITE_USDC_ADDRESS=0x5FbDB2315678afecb367f032d93F642f64180aa3
VITE_FACTORY_ADDRESS=0xaBc...
VITE_CHAIN_ID=31337
VITE_API_BASE_URL=http://localhost:9999/api
```

#### 5. 启动前端

**终端2**
```bash
cd market-web
npm run dev
```

#### 6. 在前端使用水龙头

访问前端页面，找到 `TestFaucet` 组件：

1. 连接 MetaMask（确保连接到 localhost:8545）
2. 点击快速铸造按钮（如 "10,000 USDC"）
3. 确认 MetaMask 交易
4. 等待交易确认
5. 查看余额更新

#### 7. 测试交易

使用获得的 USDC 测试交易功能：

- 创建市场（需要 1,000 USDC 初始流动性）
- 买入 YES
- 卖出 YES
- 查询持仓

---

## 📊 常用测试数量

| 场景 | 数量 | 说明 |
|------|------|------|
| 小额测试 | 100 USDC | 测试小额交易 |
| 创建市场 | 1,000 USDC | 市场初始流动性 |
| 正常交易 | 10,000 USDC | 多次交易测试 |
| 压力测试 | 100,000 USDC | 大额交易测试 |
| 完整测试 | 1,000,000 USDC | 全流程测试 |

---

## ⚠️ 注意事项

### 1. **仅用于测试网络**

⚠️ **不要在主网使用！** MockUSDC 的 `mint()` 函数任何人都可以调用，没有权限控制。

### 2. **网络配置**

确保 MetaMask 连接到正确的网络：

**本地 Hardhat 节点**:
```
Network Name: Hardhat Local
RPC URL: http://localhost:8545
Chain ID: 31337
Currency Symbol: ETH
```

**Sepolia 测试网**:
```
Network Name: Sepolia Test Network
RPC URL: https://sepolia.infura.io/v3/YOUR_PROJECT_ID
Chain ID: 11155111
Currency Symbol: SepoliaETH
```

### 3. **Gas 费用**

虽然在测试网络 ETH 可以从水龙头获取，但仍需注意：
- 每次铸造 USDC 消耗约 30,000 gas
- 确保钱包有足够的 ETH 支付 gas

### 4. **合约地址验证**

铸造前确认：
```bash
# 验证合约是否已部署
npx hardhat console --network localhost

> const usdc = await ethers.getContractAt("MockUSDC", "0x5FbDB...")
> await usdc.decimals()
# 6n

> await usdc.name()
# 'USD Coin'

> await usdc.symbol()
# 'USDC'
```

---

## 🔧 故障排除

### 问题1: "USDC 合约地址未配置"

**解决方案**:
```bash
# 检查 .env 文件
cat market-web/.env | grep USDC

# 或设置环境变量
export VITE_USDC_ADDRESS=0x5FbDB...
```

### 问题2: "请切换到正确的网络"

**解决方案**:
```bash
# 检查当前网络
npx hardhat console --network localhost
> (await ethers.provider.getNetwork()).chainId
# 31337n

# 检查 .env 配置
cat market-web/.env | grep CHAIN_ID
# VITE_CHAIN_ID=31337
```

### 问题3: MetaMask 无法连接

**解决方案**:
1. 检查 Hardhat 节点是否运行
2. 检查 RPC URL 是否为 `http://localhost:8545`
3. 检查 Chain ID 是否为 `31337`

### 问题4: 交易失败 "insufficient funds"

**解决方案**:
```bash
# 给自己铸造一些测试 ETH
npx hardhat console --network localhost
> await hre.network.provider.send("hardhat_setBalance", [
>   "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
>   "0x56BC75E2D630E0000" // 100 ETH
> ])
```

---

## 📚 相关文件

```
market-contract/
├── contracts/
│   └── MockUSDC.sol              # USDC 合约
├── scripts/
│   ├── deploy_usdc.js            # 部署脚本
│   └── mint_usdc.js              # 铸造脚本 ⭐
└── test/
    └── USDC.test.js              # 测试文件

market-web/
├── src/
│   ├── utils/
│   │   └── faucet.js             # 前端工具函数 ⭐
│   └── components/
│       └── TestFaucet.vue        # 水龙头组件 ⭐
└── .env                          # 环境变量配置
```

---

## 🎓 最佳实践

### 开发环境

```bash
# 1. 启动 Hardhat 节点
npx hardhat node

# 2. 部署合约（新终端）
npx hardhat run scripts/deploy_usdc.js --network localhost

# 3. 铸造大量 USDC 一次性使用
USDC_ADDRESS=0x... AMOUNT=1000000 npx hardhat run scripts/mint_usdc.js --network localhost

# 4. 启动前端
npm run dev

# 5. 在前端使用 TestFaucet 组件随时补充 USDC
```

### 自动化脚本

创建 `scripts/test-setup.sh`:

```bash
#!/bin/bash

# 部署 USDC
USDC_OUTPUT=$(npx hardhat run scripts/deploy_usdc.js --network localhost)
USDC_ADDRESS=$(echo "$USDC_OUTPUT" | grep "deployed to" | awk '{print $NF}')
export USDC_ADDRESS

# 部署 Factory
FACTORY_OUTPUT=$(npx hardhat run scripts/deploy_factory.js --network localhost)
FACTORY_ADDRESS=$(echo "$FACTORY_OUTPUT" | grep "deployed to" | awk '{print $NF}')
export FACTORY_ADDRESS

# 铸造 100,000 USDC
USDC_ADDRESS=$USDC_ADDRESS AMOUNT=100000 npx hardhat run scripts/mint_usdc.js --network localhost

echo "========================================"
echo "✅ 测试环境准备完成!"
echo "USDC: $USDC_ADDRESS"
echo "Factory: $FACTORY_ADDRESS"
echo "========================================"
```

使用：
```bash
chmod +x scripts/test-setup.sh
./scripts/test-setup.sh
```

---

## 🎉 总结

现在你有三种方式获取测试 USDC：

1. **命令行脚本** - 快速、批量铸造
2. **前端工具函数** - 在代码中调用
3. **前端水龙头组件** - UI 界面，用户体验好

选择最适合你的方式！🚀
