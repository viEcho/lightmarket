# USDC 合约设置指南

## 问题说明

前端报错: `missing revert data (action="call", data=null...)`

这是因为前端无法连接到 Hardhat 节点来查询 USDC 余额。

## 解决方案

### 方法1: 启动 Hardhat 节点(推荐)

#### 步骤 1: 启动 Hardhat 节点

在 `market-contract` 目录下运行:

```bash
cd market-contract
npx hardhat node
```

保持这个终端窗口打开,节点会持续运行。

#### 步骤 2: 在另一个终端部署合约

```bash
cd market-contract
npx hardhat run scripts/deploy_usdc.js --network localhost
```

你会看到类似输出:

```
MockUSDC deployed to: 0x5FbDB2315678afecb367f032d93F642f64180aa3
```

#### 步骤 3: 更新前端 .env 文件

编辑 `market-web/.env`:

```env
VITE_USDC_ADDRESS=0x5FbDB2315678afecb367f032d93F642f64180aa3
VITE_CHAIN_ID=31337
VITE_FACTORY_ADDRESS=<你的工厂合约地址>
```

#### 步骤 4: 重启前端开发服务器

```bash
cd market-web
npm run dev
```

#### 步骤 5: 确保钱包连接到正确的网络

在 MetaMask 中:
1. 添加网络: http://localhost:8545
2. Chain ID: 31337
3. 切换到这个网络

### 方法2: 使用自动化脚本

在 `market-contract` 目录下运行:

```bash
chmod +x start-and-deploy.sh
./start-and-deploy.sh
```

这会自动:
- 启动 Hardhat 节点
- 部署 USDC 合约
- 显示配置信息

## 验证设置

### 1. 检查 Hardhat 节点是否运行

```bash
curl http://localhost:8545 \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}'
```

应该返回类似: `{"jsonrpc":"2.0","id":1,"result":"0x0"}`

### 2. 检查合约是否部署

在浏览器控制台(MetaMask连接后):

```javascript
// 检查 USDC 余额
const usdcAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
const account = "你的钱包地址";

// 调用合约(需要 web3 或 ethers)
// balanceOf(account)
```

## 常见问题

### Q: 前端仍然报错怎么办?

A: 检查以下几点:
1. Hardhat 节点是否正在运行?
2. MetaMask 是否连接到 http://localhost:8545?
3. .env 文件中的合约地址是否正确?
4. 前端开发服务器是否重启了?

### Q: Hardhat 节点停止后怎么办?

A: 重新运行 `npx hardhat node` 即可。之前的部署会丢失,需要重新部署合约。

### Q: 如何保持合约部署?

A: Hardhat 节点关闭后,所有部署的合约都会丢失。这是测试网络的特性。
在生产环境(测试网/主网),合约部署后会永久存在。

## 开发流程

1. **启动 Hardhat 节点** (每天开发时启动一次)
   ```bash
   cd market-contract
   npx hardhat node
   ```

2. **部署合约** (如果需要重新部署)
   ```bash
   npx hardhat run scripts/deploy_usdc.js --network localhost
   ```

3. **启动前端**
   ```bash
   cd market-web
   npm run dev
   ```

4. **开发测试**
   - 打开浏览器访问前端
   - 确保 MetaMask 连接到 localhost:8545
   - 开始测试功能

## 快速命令

```bash
# 终端1: 启动 Hardhat 节点
cd market-contract && npx hardhat node

# 终端2: 部署合约(如需要)
cd market-contract && npx hardhat run scripts/deploy_usdc.js --network localhost

# 终端3: 启动前端
cd market-web && npm run dev
```

## 注意事项

1. **节点必须保持运行**: Hardhat 节点关闭后,前端无法与合约交互
2. **合约地址会变化**: 每次重启节点,合约地址都会改变
3. **测试币余额**: 硬编码的账户地址(0xf39Fd...)默认有 10000 ETH
4. **USDC 余额**: 部署时会自动铸造 100 万 USDC 给部署者

## 调试技巧

### 查看节点日志

Hardhat 节点会显示所有交易和事件:

```
npx hardhat node
```

观察输出,查看前端发起的交易是否被接收。

### 检查前端请求

在前端代码中添加日志:

```javascript
console.log('[Debug] Wallet address:', walletAddress);
console.log('[Debug] USDC Address:', import.meta.env.VITE_USDC_ADDRESS);
console.log('[Debug] Chain ID:', import.meta.env.VITE_CHAIN_ID);
```

### 验证合约调用

使用 Hardhat console:

```bash
npx hardhat console --network localhost
```

然后:

```javascript
const usdc = await ethers.getContractAt("MockUSDC", "0x5FbDB2315678afecb367f032d93F642f64180aa3");
const balance = await usdc.balanceOf("0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266");
console.log("Balance:", ethers.formatUnits(balance, 6));
```

应该输出: `Balance: 1000000.0`
