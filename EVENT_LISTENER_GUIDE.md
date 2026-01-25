# 市场事件监听服务 - 使用文档

## 📋 功能说明

实现了完整的Web3j事件监听服务，用于监听智能合约的交易事件并实时更新数据库中的市场价格。

## 🎯 核心功能

### 1. 自动监听交易事件
- 监听已部署市场的 `BoughtYes` 和 `SoldYes` 事件
- 发现交易时自动查询合约最新价格
- 更新数据库中的 `yes_price` 和 `no_price` 字段

### 2. 定时任务机制
- **任务1**: 每10秒检查新区块的事件
- **任务2**: 每30秒强制刷新所有市场价格（兜底）

### 3. 应用启动自动开始监听
- 应用启动完成后自动初始化
- 查询所有已部署市场（`market_status=5`）
- 为每个市场添加监听

## 📁 新增文件

```
market-backend/src/main/java/com/market/business/
├── contract/
│   └── PredictionMarket.java          # 智能合约包装类
├── service/
│   └── MarketEventListener.java       # 监听服务接口
└── service/impl/
    └── MarketEventListenerImpl.java   # 监听服务实现
```

## 🔄 数据更新流程

```
交易发生 → 合约发出事件 → Web3j监听到 → 调用合约查询价格 → 更新数据库
                                                    ↓
                                            UPDATE market
                                            SET yes_price = ?,
                                                no_price = ?,
                                                updated_time = NOW()
                                            WHERE market_address = ?
```

## ⚙️ 配置要求

确保 `application-dev.yaml` 中配置了RPC URL：

```yaml
web3j:
  rpc-url: http://localhost:8545  # 或其他RPC节点地址
```

## 🧪 测试方法

### 1. 启动后端服务
```bash
mvn spring-boot:run
```

### 2. 查看日志
启动后会看到：
```
[MarketEventListener] 应用启动完成，开始初始化事件监听服务
[MarketEventListener] Web3j 客户端初始化成功, 当前区块: 1234
[MarketEventListener] 启动事件监听服务
[MarketEventListener] 找到 1 个已部署的市场
[MarketEventListener] 添加市场监听: marketAddress=0x...
[MarketEventListener] 初始化监听区块: marketAddress=0x..., startBlock=1134
[MarketEventListener] 定时任务已启动
```

### 3. 前端发起交易
在前端买入/卖出 YES 代币

### 4. 观察后端日志
```
[MarketEventListener] 发现 1 个交易事件: marketAddress=0x...
[MarketEventListener] 价格已更新: marketAddress=0x..., yesPrice=0.6500, noPrice=0.3500
```

### 5. 查询数据库验证
```sql
SELECT market_address, yes_price, no_price, updated_time
FROM market
WHERE market_address = '0x...';
```

## 🔍 关键代码解析

### 1. 价格查询
```java
PredictionMarket contract = PredictionMarket.load(marketAddress, web3j);
BigInteger yesPrice = contract.getYesPrice().send();  // 返回 0-100
BigInteger noPrice = contract.getNoPrice().send();    // 返回 0-100

// 转换为小数（如 65 → 0.65）
BigDecimal yesPriceDecimal = new BigDecimal(yesPrice)
    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
```

### 2. 事件监听
```java
EthFilter filter = new EthFilter(
    DefaultBlockParameter.valueOf(fromBlock),
    DefaultBlockParameter.valueOf(toBlock),
    marketAddress
);

var logResults = web3j.ethGetLogs(filter).send().getLogs();
if (!logResults.isEmpty()) {
    updateMarketPrice(marketAddress);
}
```

### 3. 数据库更新
```java
marketMapper.updatePrice(marketAddress, yesPriceDecimal, noPriceDecimal);
```

## ⚡ 性能优化

### 1. 区间监听
- 只检查从上次处理到当前的区块
- 避免重复处理同一区块

### 2. 兜底机制
- 事件监听失败时，定时刷新保证价格最终会更新

### 3. 异步处理
- 使用 `ScheduledExecutorService` 异步执行
- 不阻塞主线程

## 🛠️ 故障排查

### 问题1: 没有看到监听日志
**检查**: RPC URL 是否配置正确
```yaml
web3j:
  rpc-url: http://localhost:8545  # 确认节点正在运行
```

### 问题2: 价格更新失败
**检查**:
1. 市场合约地址是否正确
2. 合约是否已部署
3. 节点是否同步

### 问题3: 查询到的价格一直是 0
**检查**:
1. 合约中的 `yesPool` 和 `noPool` 是否有值
2. 是否注入了流动性

## 📊 后续优化建议

1. **使用 WebSocket** 替代 HTTP轮询（实时性更好）
2. **添加事件缓存** 避免重复处理
3. **批量查询价格** 减少RPC调用
4. **添加价格历史记录表** 用于K线图
5. **前端监听合约事件** 实现真正的实时更新

## 🎉 完成

现在后端已经可以：
- ✅ 监听所有已部署市场的交易事件
- ✅ 实时更新价格到数据库
- ✅ 定时刷新保证数据一致性
- ✅ 应用启动自动开始监听

前端只需要从数据库读取最新价格即可！
