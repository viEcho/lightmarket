# LightMarket API 接口总结

## 快速参考

### 1. 用户认证
- `POST /api/admin/login` - 管理员登录
  - **数据表**: `admins`

### 2. 市场管理
- `POST /api/markets` - 创建市场
  - **数据表**: `markets`, `market_tags`, `market_ai_models`
- `GET /api/markets` - 获取市场列表
  - **数据表**: `markets`
- `GET /api/markets/:marketId` - 获取市场详情
  - **数据表**: `markets`, `market_tags`, `market_ai_models`
- `GET /api/markets/check-duplicate` - 检查市场重复
  - **数据表**: `markets`

### 3. 交易相关
- `POST /api/markets/:marketId/trades` - 创建交易
  - **数据表**: `transactions`, `markets` (更新)
- `GET /api/markets/:marketId/transactions` - 获取交易记录
  - **数据表**: `transactions`
- `GET /api/markets/:marketId/orderbook` - 获取订单簿
  - **数据表**: `orders` 或从 `transactions` 计算
- `GET /api/markets/:marketId/recent-trades` - 获取最近交易
  - **数据表**: `transactions`
- `GET /api/markets/:marketId/price-history` - 获取价格历史
  - **数据表**: `price_history` 或从 `transactions` 聚合

### 4. 管理员审核
- `GET /api/admin/markets/review` - 获取待审核市场
  - **数据表**: `markets`, `market_tags`
- `POST /api/admin/markets/:marketId/promote-to-final-review` - 进入终审
  - **数据表**: `markets`
- `POST /api/admin/markets/:marketId/approve` - 审核通过
  - **数据表**: `markets`
- `POST /api/admin/markets/:marketId/reject` - 拒绝市场
  - **数据表**: `markets`

### 5. 管理员仪表板
- `GET /api/admin/dashboard/stats` - 获取统计数据
  - **数据表**: `markets` (聚合)
- `GET /api/admin/dashboard/managed-markets` - 获取管理的市场
  - **数据表**: `markets`
- `POST /api/admin/markets/:marketId/settle` - 结算市场
  - **数据表**: `markets`, `rewards`
- `GET /api/admin/dashboard/rewards` - 获取奖励历史
  - **数据表**: `rewards`
- `POST /api/admin/rewards/:rewardId/claim` - 领取奖励
  - **数据表**: `rewards`
- `GET /api/admin/dashboard/settled-markets` - 获取已结算市场
  - **数据表**: `markets`, `transactions`

### 6. 排行榜
- `GET /api/leaderboard` - 获取交易者排行榜
  - **数据表**: `trader_stats` 或从 `transactions` 计算

### 7. 活动流
- `GET /api/activity` - 获取活动流
  - **数据表**: `transactions`, `markets`

---

## 数据表清单

### 必需数据表

1. **admins** - 管理员表
2. **markets** - 市场表（核心表）
3. **market_tags** - 市场标签表
4. **market_ai_models** - 市场AI模型表
5. **transactions** - 交易表（核心表）
6. **rewards** - 奖励表

### 可选数据表（用于性能优化）

7. **orders** - 订单表（如果使用订单簿系统）
8. **price_history** - 价格历史表（用于图表性能优化）
9. **trader_stats** - 交易者统计表（用于排行榜性能优化）

---

## 数据表字段概览

### markets 表（核心）
- 基本信息: id, title, question, description, category
- 时间: end_time, create_time, update_time, review_time, settled_at
- 状态: status, stage, outcome
- 价格: yes_price, no_price, current_probability
- 统计: volume, liquidity, liquidity_providers, stake_amount
- 其他: creator, resolution_source, review_note

### transactions 表（核心）
- id, market_id, type, option, amount, price, total
- trader, timestamp, status

### rewards 表
- id, market_id, creator, amount, status
- settled_at, claimed_at
