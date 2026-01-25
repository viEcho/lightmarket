# 🎉 Web3j 事件监听服务 - 完整实现文档

## ✅ 已完成的工作

### 1. 新增文件清单

```
market-backend/
├── src/main/java/com/market/business/
│   ├── contract/
│   │   └── PredictionMarket.java          # ✅ 智能合约包装类（价格查询）
│   ├── service/
│   │   └── MarketEventListener.java       # ✅ 事件监听服务接口
│   └── service/impl/
│       └── MarketEventListenerImpl.java   # ✅ 事件监听服务实现
│
├── src/main/resources/mapper/
│   └── MarketMapper.xml                   # ✅ 添加了 updatePrice 方法
│
└── EVENT_LISTENER_GUIDE.md               # ✅ 使用文档
```

### 2. 数据库层改动

**MarketMapper.java** - 添加价格更新方法：
```java
int updatePrice(
    @Param("marketAddress") String marketAddress,
    @Param("yesPrice") BigDecimal yesPrice,
    @Param("noPrice") BigDecimal noPrice
);
```

**MarketMapper.xml** - 添加 SQL：
```xml
<update id="updatePrice">
    UPDATE market
    SET yes_price = #{yesPrice},
        no_price = #{noPrice},
        updated_time = NOW()
    WHERE market_address = #{marketAddress}
</update>
```

---

## 🔍 核心功能说明

### 1. 自动监听机制

**触发时机**：应用启动完成后（`@EventListener(ApplicationReadyEvent.class)`）

**监听对象**：所有已部署的市场（`market_status = 5` 且 `market_address` 不为空）

**监听方式**：
- 每10秒检查新区块
- 发现交易事件 → 更新价格
- 每30秒强制刷新所有市场（兜底）

### 2. 价格更新流程

```
1. 监听到交易事件 (BoughtYes/SoldYes)
2. 调用合约 getYesPrice() 和 getNoPrice()
3. 价格转换 (0-100 → 0.00-1.00)
4. 更新数据库 yes_price 和 no_price 字段
5. 记录日志
```

### 3. 价格计算公式

```java
// 合约返回: 0-100 的整数
BigInteger yesPrice = contract.getYesPrice().send();  // 例如: 65

// 转换为小数
BigDecimal yesPriceDecimal = new BigDecimal(yesPrice)
    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    // 结果: 0.6500
```

---

## 🧪 测试步骤

### 前提条件
1. ✅ Hardhat 节点正在运行
2. ✅ 至少有一个市场已部署（`market_status = 5`）
3. ✅ 市场合约有流动性

### 测试流程

#### 1. 启动后端
```bash
cd market-backend
mvn spring-boot:run
```

#### 2. 查看启动日志
应该看到：
```
[MarketEventListener] 应用启动完成，开始初始化事件监听服务
[MarketEventListener] Web3j 客户端初始化成功, 当前区块: 123
[MarketEventListener] 启动事件监听服务
[MarketEventListener] 找到 1 个已部署的市场
[MarketEventListener] 添加市场监听: marketAddress=0x5FbDB...
[MarketEventListener] 定时任务已启动
[MarketEventListener] 价格已更新: marketAddress=0x..., yesPrice=0.5000, noPrice=0.5000
```

#### 3. 前端发起交易
在前端点击"买入YES"或"卖出YES"

#### 4. 观察后端日志
交易后10秒内应该看到：
```
[MarketEventListener] 发现 1 个交易事件: marketAddress=0x...
[MarketEventListener] 价格已更新: marketAddress=0x..., yesPrice=0.5200, noPrice=0.4800
```

#### 5. 验证数据库
```sql
SELECT
    market_address,
    yes_price,
    no_price,
    updated_time
FROM market
WHERE market_address = '0x5FbDB2315678afecb367f032d93F642f64180aa3';
```

---

## 📊 合约价格计算逻辑

### 价格公式
```solidity
// YES 价格 = NO池 / (YES池 + NO池) × 100
function getYesPrice() public view returns (uint256) {
    uint256 totalPool = yesPool + noPool;
    return (noPool * 100) / totalPool;
}

// NO 价格 = YES池 / (YES池 + NO池) × 100
function getNoPrice() public view returns (uint256) {
    uint256 totalPool = yesPool + noPool;
    return (yesPool * 100) / totalPool;
}
```

### 示例计算

假设：
- `yesPool = 500`
- `noPool = 500`

则：
- `YES价格 = 500 / 1000 × 100 = 50`
- `NO价格 = 500 / 1000 × 100 = 50`

如果有人买入 100 YES：
- `yesPool = 600`
- `noPool = 500`
- `YES价格 = 500 / 1100 × 100 = 45.45` （下跌）
- `NO价格 = 600 / 1100 × 100 = 54.54` （上涨）

---

## 🛠️ 配置说明

### application-dev.yaml
```yaml
web3j:
  rpc-url: http://localhost:8545  # 确保与Hardhat节点一致
```

### 检查节点状态
```bash
curl -X POST -H "Content-Type: application/json" \
  --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}' \
  http://localhost:8545
```

---

## 🚨 故障排查

### 问题1: 服务启动但没有监听日志

**检查1**: RPC URL是否正确
```bash
# 查看配置
cat src/main/resources/application-dev.yaml | grep rpc-url
```

**检查2**: 是否有已部署的市场
```sql
SELECT market_id, market_address, market_status
FROM market
WHERE market_status = 5;
```

**解决**: 如果没有，需要先部署市场

### 问题2: 价格一直是 0

**原因**: 合约没有流动性或未注入资金

**检查**:
```bash
cd ../market-contract
npx hardhat run scripts/check_balance.js
```

**解决**: 给市场注入流动性

### 问题3: 更新失败

**检查日志**:
```
ERROR [MarketEventListener] 更新价格失败: marketAddress=0x...
```

**可能原因**:
1. 合约地址错误
2. 节点未同步
3. 合约未部署

---

## 📈 性能指标

### 当前配置
- **事件检查间隔**: 10秒
- **价格刷新间隔**: 30秒
- **最大延迟**: 约40秒（事件+兜底刷新）

### 优化建议
1. 使用 WebSocket 替代 HTTP 轮询
2. 添加事件缓存避免重复处理
3. 批量查询多个市场

---

## 🎯 后续集成

### 前端使用
```javascript
// 从后端获取最新价格
const response = await axios.get('/api/market/findList')
const markets = response.data.data

markets.forEach(market => {
  console.log(market.yesPrice)  // 0.65
  console.log(market.noPrice)   // 0.35
})
```

### API 返回格式
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "marketId": "MKT-xxx",
      "marketAddress": "0x...",
      "yesPrice": 0.65,
      "noPrice": 0.35,
      "updatedTime": "2026-01-25T20:30:45"
    }
  ]
}
```

---

## ✨ 完成清单

- [x] 创建智能合约包装类
- [x] 创建事件监听服务接口
- [x] 实现事件监听服务
- [x] 添加数据库更新方法
- [x] 应用启动自动监听
- [x] 定时任务轮询价格
- [x] 兜底机制（强制刷新）
- [x] 详细日志输出
- [x] 异常处理
- [x] 使用文档

---

## 🎉 总结

现在后端已经完整实现了：
1. ✅ 监听所有已部署市场的交易事件
2. ✅ 实时查询合约最新价格
3. ✅ 自动更新数据库
4. ✅ 应用启动自动开始监听
5. ✅ 定时刷新保证数据一致性

**前端只需要从数据库读取即可！**

无需前端监听合约事件，后端自动同步价格，前端轮询或WebSocket推送即可。
