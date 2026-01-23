# 发布市场功能文档

## 功能概述

在"我的市场"页面中,当市场状态为"终审通过"(status=3)时,会显示一个醒目的"发布市场到区块链"按钮,允许用户将市场部署到区块链上。

## 完整流程

### 1. 前端流程

```
用户点击"发布市场到区块链"按钮
    ↓
检查钱包连接状态
    ↓
检查 USDC 余额是否充足
    ↓
确认对话框(显示市场信息和所需USDC)
    ↓
调用后端 opening 接口
    ↓
后端校验(市场创建人 + 市场状态)
    ↓
后端将状态改为"发布中"(status=4)
    ↓
前端授权 USDC 给工厂合约
    ↓
前端调用 Factory.createMarket 合约
    ↓
前端立即调用 notifyMarketDeploying 接口(发送 txHash)
    ↓
合约质押 USDC 作为初始流动性
    ↓
部署完成,刷新市场列表
```

### 2. 后端流程

#### 接口1: `GET /api/market/opening`

**参数:**
- `userId`: 用户ID
- `marketId`: 市场ID

**后端校验:**
1. 市场是否由该用户创建
2. 市场状态是否为"终审通过"(status=3)
3. 如果校验通过,将市场状态改为"发布中"(status=4)

**响应:**
```json
{
  "success": true,
  "code": 1000,
  "message": "校验通过",
  "data": null
}
```

#### 接口2: `POST /api/market/deploying`

**参数:**
- `marketId`: 市场ID (query参数)
- `txHash`: 交易哈希 (form参数)
- `onChainMarketId`: 链上市场ID (form参数,可选)

**后端处理:**
1. 接收前端发送的 txHash
2. 开始监听工厂合约的 MarketCreated 事件
3. 监听到事件后:
   - 验证合约管理员
   - 验证创建事件
   - 提取市场合约地址
   - 更新数据库 market 表:
     - 设置 `marketAddress` 为合约地址
     - 将状态改为"已发布上链"(status=5)
     - 设置 `onChainMarketId`

**响应:**
```json
{
  "success": true,
  "code": 1000,
  "message": "后端已开始监听链上事件",
  "data": null
}
```

## 前端组件

### OpenMarketButton.vue

**显示条件:**
- 只在 `market.marketStatus === 3` 时显示

**按钮状态:**
1. **默认状态**: 显示 "🚀 发布市场到区块链 (需质押 XXX USDC)"
2. **处理中状态**: 显示进度步骤:
   - "连接钱包..."
   - "检查余额..."
   - "后端校验..."
   - "授权 USDC..."
   - "部署合约..."
   - "质押流动性..."
   - "完成部署..."

3. **已部署状态**: 显示绿色的"已部署"徽章和合约地址链接

4. **错误状态**: 显示错误消息(5秒后自动消失)

5. **成功状态**: 显示成功消息和交易哈希(10秒后自动消失)

### MarketCard.vue 集成

**修改内容:**
1. 导入 `OpenMarketButton` 组件
2. 在 `description` 后添加按钮
3. 当 `marketStatus === 3` 时:
   - 隐藏 `card-stats` (Creator, Volume)
   - 隐藏 `probability-section` (Current Probability)
   - 隐藏 `card-actions` (Yes/No 按钮)
4. 添加 `handleDeployed` 方法处理部署完成事件

## 合约交互

### Factory.createMarket

```solidity
function createMarket(
    bytes32 marketId,      // 链上市场ID
    uint256 endTime,       // 结束时间戳
    uint256 initialLiquidity  // 初始流动性(USDC)
) external returns (address)
```

**事件:**
```solidity
event MarketCreated(
    bytes32 indexed marketId,
    address indexed market,
    address indexed creator,
    uint256 endTime,
    uint256 initialLiquidity
)
```

### 执行步骤

1. **Approve USDC**
   - 调用 `USDC.approve(FACTORY_ADDRESS, amount)`

2. **Create Market**
   - 调用 `Factory.createMarket(marketId, endTime, liquidity)`
   - 工厂合约会:
     - 创建新的 PredictionMarket 合约
     - 转账 USDC 到新合约
     - 初始化流动性池
     - 触发 `MarketCreated` 事件

## API 文件更新

### 新增接口函数

```javascript
// src/utils/api.js

/**
 * GET 请求开始发布市场
 */
export const openMarket = (userId, marketId) => {
  return request(`/market/opening?userId=${userId}&marketId=${marketId}`, {
    method: 'GET'
  })
}
```

### 已有接口函数

```javascript
/**
 * POST 请求通知后端：开始创建市场合约
 */
export const notifyMarketDeploying = (marketId, data) => {
  // data: { txHash, onChainMarketId }
  return request(`/market/deploying?marketId=${marketId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData.toString()
  })
}
```

## 状态映射

### 市场状态码

```
0 - 待审核
1 - 已拒绝
2 - 初审通过
3 - 终审通过 ← 显示"发布市场"按钮
4 - 发布中 ← 后端 opening 接口返回后
5 - 已发布上链 ← 后端监听到事件后
6 - 已关闭
7 - 裁决中
8 - 挑战中
9 - 已终裁
10 - 结算中
99 - 已结算
```

## 环境变量

```bash
# .env
VITE_FACTORY_ADDRESS=0x...  # 工厂合约地址
VITE_USDC_ADDRESS=0x...      # USDC 合约地址
VITE_CHAIN_ID=31337          # 链ID
```

## 错误处理

### 常见错误

1. **钱包未连接**
   - 提示: "请先连接钱包"

2. **USDC 余额不足**
   - 提示: "USDC 余额不足! 需要: XXX USDC, 当前: XXX USDC"

3. **后端校验失败**
   - 可能原因:
     - 市场不是由该用户创建
     - 市场状态不是终审通过
   - 提示: "后端校验失败"

4. **用户拒绝交易**
   - 提示: "用户拒绝了交易"

5. **合约部署失败**
   - 可能原因:
     - Gas 不足
     - 网络错误
     - 合约执行失败
   - 提示: "合约部署失败"

## 后端实现指南

### 1. Opening 接口

```java
@GetMapping("/opening")
public Result openMarket(
    @RequestParam String userId,
    @RequestParam String marketId
) {
    // 1. 查询市场
    Market market = marketService.getById(marketId);

    // 2. 校验市场创建人
    if (!market.getCreatorId().equals(userId)) {
        return Result.error("无权发布此市场");
    }

    // 3. 校验市场状态
    if (market.getMarketStatus() != 3) {
        return Result.error("市场状态不正确");
    }

    // 4. 更新状态为"发布中"
    market.setMarketStatus(4);
    marketService.updateById(market);

    return Result.success();
}
```

### 2. Deploying 接口

```java
@PostMapping("/deploying")
public Result notifyDeploying(
    @RequestParam String marketId,
    @RequestParam String txHash,
    @RequestParam(required = false) String onChainMarketId
) {
    // 1. 查询市场
    Market market = marketService.getById(marketId);

    // 2. 保存交易信息
    market.setDeployTxHash(txHash);
    if (onChainMarketId != null) {
        market.setOnChainMarketId(onChainMarketId);
    }
    marketService.updateById(market);

    // 3. 启动异步任务监听链上事件
    asyncTaskService.listenMarketCreated(marketId, txHash);

    return Result.success("后端已开始监听链上事件");
}
```

### 3. 链上事件监听

```java
@Async
public void listenMarketCreated(String marketId, String txHash) {
    // 1. 查询交易收据
    TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();

    // 2. 解析 MarketCreated 事件
    Event event = factoryContract.getEvent("MarketCreated");
    EventValues eventValues = contract.staticExtractEventParameters(
        event,
        receipt.getLogs().get(0)
    );

    // 3. 提取数据
    String marketAddress = (String) eventValues.getIndexedValues().get(1).getValue();
    String creator = (String) eventValues.getIndexedValues().get(2).getValue();

    // 4. 验证管理员和创建者
    // ...

    // 5. 更新数据库
    Market market = marketService.getById(marketId);
    market.setMarketAddress(marketAddress);
    market.setMarketStatus(5); // 已发布上链
    marketService.updateById(market);
}
```

## 测试步骤

1. **准备测试环境**
   - 部署工厂合约
   - 部署 USDC 合约
   - 配置环境变量

2. **创建测试数据**
   - 用户创建市场
   - 市场通过终审 (status=3)

3. **测试发布流程**
   - 连接钱包
   - 确保 USDC 余额充足
   - 点击"发布市场到区块链"
   - 确认对话框
   - 等待交易完成
   - 验证后端数据库更新
   - 刷新页面查看状态

4. **验证结果**
   - 市场状态变为"已发布上链"(status=5)
   - marketAddress 字段已填充
   - 可以正常交易

## 注意事项

1. **安全**
   - 前端不解析事件数据,只发送 txHash
   - 后端通过 txHash 查询链上真实数据
   - 验证合约管理员和创建者

2. **用户体验**
   - 清晰的进度提示
   - 详细的错误信息
   - 自动刷新列表

3. **性能**
   - 使用异步任务监听链上事件
   - 定时任务每30秒查询一次
   - 超时时间设置为5分钟

4. **容错**
   - 即使后端通知失败,交易已确认
   - 后端可以通过定时任务补偿
   - 用户可以手动刷新查看状态
