# 本地测试网部署完成 ✅

## 已部署合约

### 1. MockUSDC
- **地址**: `0x5FbDB2315678afecb367f032d93F642f64180aa3`
- **功能**: ERC20 代币,6位小数
- **初始供应**: 已铸造 1,000,000 USDC 给部署者账户

### 2. MarketFactory
- **地址**: `0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0`
- **USDC 地址**: `0x5FbDB2315678afecb367f032d93F642f64180aa3`
- **功能**: 创建市场合约的工厂合约

## 配置文件

### market-web/.env
```env
VITE_FACTORY_ADDRESS=0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0
VITE_USDC_ADDRESS=0x5FbDB2315678afecb367f032d93F642f64180aa3
VITE_CHAIN_ID=31337
```

## 测试账户信息

### 默认 Hardhat 账户 #0
- **地址**: `0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266`
- **私钥**: `0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80`
- **ETH 余额**: 10,000 ETH
- **USDC 余额**: 1,000,000 USDC

### MetaMask 配置
1. **网络名称**: Hardhat Local
2. **RPC URL**: http://localhost:8545
3. **Chain ID**: 31337
4. **货币符号**: ETH

## 运行环境

### 当前运行的进程

1. **Hardhat 节点** (终端1)
   ```bash
   cd market-contract
   npx hardhat node
   ```
   - 监听: http://localhost:8545
   - 必须保持运行才能与合约交互

2. **前端开发服务器** (终端2)
   ```bash
   cd market-web
   npm run dev
   ```
   - 访问: http://localhost:5173

## 验证部署

### 验证 USDC 合约
```bash
cd market-contract
npx hardhat console --network localhost
```

在控制台中:
```javascript
const usdc = await ethers.getContractAt("MockUSDC", "0x5FbDB2315678afecb367f032d93F642f64180aa3");
const balance = await usdc.balanceOf("0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266");
console.log("USDC Balance:", ethers.formatUnits(balance, 6));
// 输出: USDC Balance: 1000000.0
```

### 验证 Factory 合约
```javascript
const factory = await ethers.getContractAt("MarketFactory", "0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0");
const usdc = await factory.usdc();
console.log("USDC Address:", usdc);
// 输出: USDC Address: 0x5FbDB2315678afecb367f032d93F642f64180aa3
```

## 功能测试流程

### 1. 连接钱包
- 打开前端应用
- 点击连接钱包
- 选择 MetaMask
- 导入测试账户私钥

### 2. 查看余额
前端应显示:
- **需要质押**: 显示市场的基础流动性金额
- **当前余额**: 显示钱包的 USDC 余额 (1,000,000 USDC)

### 3. 发布市场
1. 在"我的市场"页面找到终审通过的市场
2. 点击"发布市场"按钮
3. 确认交易
4. 等待交易完成

### 4. 验证结果
- 市场状态变为"发布中" (status=4)
- 后端监听链上事件
- 约30秒后状态变为"已发布上链" (status=5)
- marketAddress 字段已填充

## 重置环境

如果需要重新开始:

### 停止 Hardhat 节点
- 在终端1 按 `Ctrl+C`

### 重新部署
```bash
# 终端1: 启动节点
cd market-contract
npx hardhat node

# 终端2: 部署合约
cd market-contract

# 部署 USDC
npx hardhat run scripts/deploy_usdc.js --network localhost

# 部署 Factory
USDC_ADDRESS=<新的USDC地址> npx hardhat run scripts/deploy_factory.js --network localhost

# 更新前端 .env 文件
# VITE_FACTORY_ADDRESS=<新的Factory地址>
# VITE_USDC_ADDRESS=<新的USDC地址>

# 重启前端
cd ../market-web
npm run dev
```

## 注意事项

1. **Hardhat 节点必须运行**: 关闭节点后所有部署都会丢失
2. **合约地址会变化**: 每次重新部署,地址都会改变
3. **环境变量需同步**: 前端 .env 必须与实际部署地址一致
4. **钱包连接**: 确保 MetaMask 连接到 http://localhost:8545
5. **交易确认**: Hardhat 节点默认自动确认交易,无需等待

## 快速命令参考

```bash
# 启动 Hardhat 节点
cd market-contract && npx hardhat node

# 部署 USDC
cd market-contract && npx hardhat run scripts/deploy_usdc.js --network localhost

# 部署 Factory (需要先设置 USDC_ADDRESS)
cd market-contract && USDC_ADDRESS=0x5FbDB2315678afecb367f032d93F642f64180aa3 npx hardhat run scripts/deploy_factory.js --network localhost

# 启动前端
cd market-web && npm run dev

# 查看节点日志
tail -f /tmp/hardhat-node.log

# 停止 Hardhat 节点
pkill -f "hardhat node"
```

## 测试数据

### 市场创建测试数据
- **基础流动性**: 1000 USDC
- **市场状态**: 3 (终审通过)
- **创建者**: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266

### 预期结果
- ✅ 可以查询到 USDC 余额: 1,000,000 USDC
- ✅ 余额足够发布市场
- ✅ 可以成功调用 Factory.createMarket
- ✅ 市场合约创建成功
- ✅ USDC 正确质押到市场合约

## 故障排查

### 问题: 无法查询 USDC 余额
**解决**:
1. 确认 Hardhat 节点正在运行
2. 确认 MetaMask 连接到 localhost:8545
3. 检查 .env 文件中的 USDC_ADDRESS
4. 刷新前端页面

### 问题: Factory 合约地址未配置
**解决**:
1. 确认 Factory 合约已部署
2. 更新 .env 文件中的 VITE_FACTORY_ADDRESS
3. 重启前端开发服务器

### 问题: 交易失败
**解决**:
1. 检查 Hardhat 节点日志
2. 确认账户有足够的 ETH (gas费)
3. 确认账户有足够的 USDC 余额
4. 检查合约是否正确授权

## 下一步

现在你可以:
1. ✅ 连接钱包并查看余额
2. ✅ 测试发布市场功能
3. ⏭️ 实现后端 opening 接口
4. ⏭️ 实现后端 deploying 接口
5. ⏭️ 实现链上事件监听

详细的功能文档请参考:
- [OPEN_MARKET_GUIDE.md](./OPEN_MARKET_GUIDE.md)
- [USDC_SETUP_GUIDE.md](./USDC_SETUP_GUIDE.md)
