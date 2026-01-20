# LightMarket 后端 API 接口文档

## 概述
本文档列出了 LightMarket 前端应用所需的所有后端 API 接口以及每个接口对应的数据表。

---

## 1. 用户认证相关

### 1.1 管理员登录
**接口**: `POST /api/admin/login`  
**描述**: 管理员登录验证

**请求参数**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应**:
```json
{
  "success": true,
  "token": "string",
  "admin": {
    "id": "number",
    "username": "string"
  }
}
```

**数据表**: `admins`
- id (主键)
- username (唯一)
- password_hash
- created_at
- updated_at

---

## 2. 市场管理相关

### 2.1 创建市场
**接口**: `POST /api/markets`  
**描述**: 用户创建新的预测市场

**请求参数**:
```json
{
  "title": "string",
  "question": "string",
  "description": "string",
  "category": "crypto|technology|politics|sports|finance|entertainment|other",
  "endTime": "datetime",
  "stakeAmount": "number",
  "resolutionSource": "string",
  "tags": ["string"],
  "selectedAI": ["string"],
  "creator": "string" // 钱包地址或用户ID
}
```

**响应**:
```json
{
  "success": true,
  "market": {
    "id": "string",
    "title": "string",
    "status": "pending",
    "stage": "pre-review",
    "createTime": "timestamp"
  }
}
```

**数据表**: `markets`
- id (主键)
- title
- question
- description
- category
- end_time
- stake_amount
- resolution_source
- creator (用户地址或ID)
- status (pending|approved|rejected|settled)
- stage (pre-review|final-review|published)
- current_probability (默认0.5)
- yes_price (默认0.5)
- no_price (默认0.5)
- volume (默认0)
- liquidity (初始等于stake_amount)
- liquidity_providers (默认1)
- create_time
- update_time
- review_time
- review_note
- settled_at
- outcome (yes|no|null)

**数据表**: `market_tags`
- id (主键)
- market_id (外键 -> markets.id)
- tag (字符串)

**数据表**: `market_ai_models`
- id (主键)
- market_id (外键 -> markets.id)
- ai_model (chatgpt|claude|gemini|perplexity|grok|wenxin|tongyi|glm|kimi|xunfei)

---

### 2.2 获取市场列表
**接口**: `GET /api/markets`  
**描述**: 获取市场列表，支持筛选和分页

**查询参数**:
- `category`: 分类筛选
- `status`: 状态筛选 (approved|pending|rejected|settled)
- `page`: 页码
- `limit`: 每页数量
- `sort`: 排序方式 (volume|createTime|liquidity)

**响应**:
```json
{
  "success": true,
  "markets": [
    {
      "id": "string",
      "title": "string",
      "question": "string",
      "description": "string",
      "category": "string",
      "yesPrice": "number",
      "noPrice": "number",
      "currentProbability": "number",
      "volume": "number",
      "liquidity": "number",
      "liquidityProviders": "number",
      "status": "string",
      "endTime": "timestamp",
      "endDate": "string",
      "creator": "string",
      "createTime": "timestamp",
      "tags": ["string"],
      "imageUrl": "string"
    }
  ],
  "pagination": {
    "page": "number",
    "limit": "number",
    "total": "number",
    "totalPages": "number"
  }
}
```

**数据表**: `markets` (同上)

---

### 2.3 获取市场详情
**接口**: `GET /api/markets/:marketId`  
**描述**: 获取单个市场的详细信息

**响应**:
```json
{
  "success": true,
  "market": {
    "id": "string",
    "title": "string",
    "question": "string",
    "description": "string",
    "category": "string",
    "yesPrice": "number",
    "noPrice": "number",
    "currentProbability": "number",
    "volume": "number",
    "liquidity": "number",
    "liquidityProviders": "number",
    "status": "string",
    "stage": "string",
    "endTime": "timestamp",
    "endDate": "string",
    "creator": "string",
    "createTime": "timestamp",
    "resolutionSource": "string",
    "tags": ["string"],
    "selectedAI": ["string"],
    "stakeAmount": "number",
    "imageUrl": "string",
    "reviewTime": "timestamp",
    "reviewNote": "string",
    "settledAt": "timestamp",
    "outcome": "yes|no|null"
  }
}
```

**数据表**: `markets`, `market_tags`, `market_ai_models`

---

### 2.4 检查市场重复
**接口**: `GET /api/markets/check-duplicate`  
**描述**: 检查市场标题是否重复

**查询参数**:
- `title`: 市场标题

**响应**:
```json
{
  "hasDuplicate": true,
  "duplicates": [
    {
      "id": "string",
      "title": "string",
      "status": "string"
    }
  ]
}
```

**数据表**: `markets`

---

## 3. 交易相关

### 3.1 创建交易订单
**接口**: `POST /api/markets/:marketId/trades`  
**描述**: 用户买入或卖出市场代币

**请求参数**:
```json
{
  "type": "buy|sell",
  "option": "yes|no",
  "amount": "number", // IMKT数量
  "trader": "string" // 钱包地址或用户ID
}
```

**响应**:
```json
{
  "success": true,
  "transaction": {
    "id": "string",
    "marketId": "string",
    "type": "string",
    "option": "string",
    "amount": "number",
    "price": "number",
    "total": "number",
    "trader": "string",
    "timestamp": "timestamp"
  },
  "market": {
    "yesPrice": "number",
    "noPrice": "number",
    "currentProbability": "number",
    "volume": "number",
    "liquidity": "number"
  }
}
```

**数据表**: `transactions`
- id (主键)
- market_id (外键 -> markets.id)
- type (buy|sell)
- option (yes|no)
- amount
- price (交易时的价格)
- total (amount * price)
- trader (用户地址或ID)
- timestamp
- status (pending|completed|failed)

**数据表**: `markets` (更新 volume, liquidity, yes_price, no_price, current_probability)

---

### 3.2 获取市场交易记录
**接口**: `GET /api/markets/:marketId/transactions`  
**描述**: 获取指定市场的所有交易记录

**查询参数**:
- `type`: 交易类型筛选 (buy|sell|all)
- `page`: 页码
- `limit`: 每页数量
- `sort`: 排序方式 (time|amount|price)

**响应**:
```json
{
  "success": true,
  "transactions": [
    {
      "id": "string",
      "type": "string",
      "option": "string",
      "amount": "number",
      "price": "number",
      "total": "number",
      "trader": "string",
      "timestamp": "timestamp"
    }
  ],
  "pagination": {
    "page": "number",
    "limit": "number",
    "total": "number",
    "totalPages": "number"
  },
  "summary": {
    "totalTransactions": "number",
    "totalVolume": "number",
    "uniqueTraders": "number",
    "currentPrice": "number"
  }
}
```

**数据表**: `transactions`

---

### 3.3 获取订单簿
**接口**: `GET /api/markets/:marketId/orderbook`  
**描述**: 获取市场的订单簿数据（买单和卖单）

**响应**:
```json
{
  "success": true,
  "bids": [
    {
      "price": "number",
      "amount": "number",
      "total": "number",
      "depth": "number"
    }
  ],
  "asks": [
    {
      "price": "number",
      "amount": "number",
      "total": "number",
      "depth": "number"
    }
  ],
  "currentPrice": "number",
  "spread": "number",
  "maxBid": "number",
  "minAsk": "number",
  "totalVolume": "number"
}
```

**数据表**: `orders` (如果使用订单簿系统)
- id (主键)
- market_id (外键 -> markets.id)
- type (bid|ask)
- price
- amount
- trader
- status (open|filled|cancelled)
- create_time
- update_time

**或者从 `transactions` 表实时计算**

---

### 3.4 获取最近交易
**接口**: `GET /api/markets/:marketId/recent-trades`  
**描述**: 获取市场最近的交易记录

**查询参数**:
- `limit`: 返回数量 (默认20)
- `filter`: 筛选类型 (buy|sell|all)

**响应**:
```json
{
  "success": true,
  "trades": [
    {
      "id": "string",
      "type": "string",
      "price": "number",
      "amount": "number",
      "timestamp": "timestamp",
      "time": "string" // 格式化后的时间字符串
    }
  ],
  "summary": {
    "totalTrades": "number",
    "totalVolume": "number",
    "lastPrice": "number"
  }
}
```

**数据表**: `transactions`

---

### 3.5 获取价格历史数据
**接口**: `GET /api/markets/:marketId/price-history`  
**描述**: 获取市场价格历史数据，用于图表显示

**查询参数**:
- `period`: 时间周期 (30m|1H|4H|1D|1W|1M|All)
- `startTime`: 开始时间戳
- `endTime`: 结束时间戳

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "timestamp": "timestamp",
      "date": "string",
      "price": "number",
      "volume": "number"
    }
  ],
  "currentPrice": "number",
  "priceChange": "number",
  "highPrice": "number",
  "lowPrice": "number",
  "volume": "number"
}
```

**数据表**: `price_history` (价格历史快照表)
- id (主键)
- market_id (外键 -> markets.id)
- timestamp
- price
- volume
- yes_price
- no_price

**或者从 `transactions` 表按时间聚合计算**

---

## 4. 管理员审核相关

### 4.1 获取待审核市场列表
**接口**: `GET /api/admin/markets/review`  
**描述**: 管理员获取待审核的市场列表

**查询参数**:
- `stage`: 审核阶段 (pre-review|final-review)
- `status`: 状态 (pending|approved|rejected)
- `page`: 页码
- `limit`: 每页数量

**响应**:
```json
{
  "success": true,
  "markets": [
    {
      "id": "string",
      "title": "string",
      "description": "string",
      "category": "string",
      "creator": "string",
      "stakeAmount": "number",
      "createTime": "timestamp",
      "status": "string",
      "stage": "string",
      "duplicateCheck": {
        "hasDuplicate": "boolean",
        "duplicates": ["array"]
      },
      "competingMarkets": ["array"],
      "criteria": {
        "topic": "boolean",
        "resolvable": "boolean",
        "source": "boolean",
        "timeline": "boolean"
      }
    }
  ],
  "pagination": {
    "page": "number",
    "limit": "number",
    "total": "number"
  }
}
```

**数据表**: `markets`, `market_tags`

---

### 4.2 预审通过（进入终审）
**接口**: `POST /api/admin/markets/:marketId/promote-to-final-review`  
**描述**: 将市场从预审阶段提升到终审阶段

**响应**:
```json
{
  "success": true,
  "market": {
    "id": "string",
    "stage": "final-review"
  }
}
```

**数据表**: `markets` (更新 stage 字段)

---

### 4.3 审核通过市场
**接口**: `POST /api/admin/markets/:marketId/approve`  
**描述**: 管理员审核通过市场

**请求参数**:
```json
{
  "reviewNote": "string",
  "criteria": {
    "topic": "boolean",
    "resolvable": "boolean",
    "source": "boolean",
    "timeline": "boolean"
  }
}
```

**响应**:
```json
{
  "success": true,
  "market": {
    "id": "string",
    "status": "approved",
    "stage": "published",
    "reviewTime": "timestamp"
  }
}
```

**数据表**: `markets` (更新 status, stage, review_time, review_note)

---

### 4.4 拒绝市场
**接口**: `POST /api/admin/markets/:marketId/reject`  
**描述**: 管理员拒绝市场申请

**请求参数**:
```json
{
  "reason": "string",
  "reviewNote": "string"
}
```

**响应**:
```json
{
  "success": true,
  "market": {
    "id": "string",
    "status": "rejected",
    "reviewTime": "timestamp",
    "reviewNote": "string"
  }
}
```

**数据表**: `markets` (更新 status, review_time, review_note)

---

## 5. 管理员仪表板相关

### 5.1 获取仪表板统计数据
**接口**: `GET /api/admin/dashboard/stats`  
**描述**: 获取管理员仪表板的统计数据

**响应**:
```json
{
  "success": true,
  "stats": {
    "totalMarkets": "number",
    "activeMarkets": "number",
    "pendingMarkets": "number",
    "totalLiquidity": "number"
  }
}
```

**数据表**: `markets` (聚合查询)

---

### 5.2 获取管理的市场列表
**接口**: `GET /api/admin/dashboard/managed-markets`  
**描述**: 获取当前用户创建的所有市场

**查询参数**:
- `creator`: 创建者地址或ID
- `status`: 状态筛选

**响应**:
```json
{
  "success": true,
  "markets": [
    {
      "id": "string",
      "title": "string",
      "category": "string",
      "status": "string",
      "liquidity": "number",
      "volume": "number",
      "endTime": "timestamp",
      "stakeAmount": "number"
    }
  ]
}
```

**数据表**: `markets`

---

### 5.3 结算市场
**接口**: `POST /api/admin/markets/:marketId/settle`  
**描述**: 市场创建者结算市场

**请求参数**:
```json
{
  "outcome": "yes|no"
}
```

**响应**:
```json
{
  "success": true,
  "market": {
    "id": "string",
    "status": "settled",
    "outcome": "yes|no",
    "settledAt": "timestamp"
  },
  "reward": {
    "amount": "number",
    "status": "pending"
  }
}
```

**数据表**: `markets` (更新 status, outcome, settled_at)  
**数据表**: `rewards` (创建奖励记录)
- id (主键)
- market_id (外键 -> markets.id)
- creator (用户地址或ID)
- amount
- status (pending|claimed)
- settled_at
- claimed_at

---

### 5.4 获取奖励历史
**接口**: `GET /api/admin/dashboard/rewards`  
**描述**: 获取用户的流动性奖励历史

**查询参数**:
- `creator`: 创建者地址或ID

**响应**:
```json
{
  "success": true,
  "rewards": [
    {
      "id": "string",
      "marketId": "string",
      "marketTitle": "string",
      "amount": "number",
      "status": "pending|claimed",
      "settledAt": "timestamp",
      "claimedAt": "timestamp"
    }
  ],
  "summary": {
    "totalRewards": "number",
    "pendingRewards": "number",
    "claimedRewards": "number"
  }
}
```

**数据表**: `rewards`

---

### 5.5 领取奖励
**接口**: `POST /api/admin/rewards/:rewardId/claim`  
**描述**: 用户领取流动性奖励

**响应**:
```json
{
  "success": true,
  "reward": {
    "id": "string",
    "status": "claimed",
    "claimedAt": "timestamp"
  }
}
```

**数据表**: `rewards` (更新 status, claimed_at)

---

### 5.6 获取已结算市场列表
**接口**: `GET /api/admin/dashboard/settled-markets`  
**描述**: 获取用户已结算的市场列表

**查询参数**:
- `creator`: 创建者地址或ID

**响应**:
```json
{
  "success": true,
  "markets": [
    {
      "id": "string",
      "title": "string",
      "category": "string",
      "outcome": "yes|no",
      "volume": "number",
      "participants": "number",
      "settledAt": "timestamp"
    }
  ]
}
```

**数据表**: `markets`, `transactions` (计算 participants)

---

## 6. 排行榜相关

### 6.1 获取交易者排行榜
**接口**: `GET /api/leaderboard`  
**描述**: 获取交易者排行榜

**查询参数**:
- `limit`: 返回数量 (默认5)
- `sort`: 排序方式 (profit|winRate|trades)

**响应**:
```json
{
  "success": true,
  "traders": [
    {
      "address": "string",
      "name": "string",
      "avatar": "string",
      "profit": "number",
      "winRate": "number",
      "trades": "number"
    }
  ]
}
```

**数据表**: `trader_stats` (交易者统计表)
- id (主键)
- trader (用户地址或ID)
- name
- avatar
- total_profit
- win_rate
- total_trades
- update_time

**或者从 `transactions` 和 `markets` 表实时计算**

---

## 7. 活动流相关

### 7.1 获取活动流
**接口**: `GET /api/activity`  
**描述**: 获取所有市场的交易活动流

**查询参数**:
- `filter`: 筛选类型 (All|Buys|Sells)
- `limit`: 返回数量
- `page`: 页码

**响应**:
```json
{
  "success": true,
  "activities": [
    {
      "id": "string",
      "user": "string",
      "type": "buy|sell",
      "amount": "number",
      "position": "yes|no",
      "price": "number",
      "market": "string",
      "marketId": "string",
      "time": "string",
      "timestamp": "timestamp"
    }
  ],
  "pagination": {
    "page": "number",
    "limit": "number",
    "total": "number"
  }
}
```

**数据表**: `transactions`, `markets` (关联查询)

---

## 数据表总结

### 核心数据表

1. **admins** - 管理员表
   - id, username, password_hash, created_at, updated_at

2. **markets** - 市场表
   - id, title, question, description, category, end_time, stake_amount, resolution_source, creator, status, stage, current_probability, yes_price, no_price, volume, liquidity, liquidity_providers, create_time, update_time, review_time, review_note, settled_at, outcome

3. **market_tags** - 市场标签表
   - id, market_id, tag

4. **market_ai_models** - 市场AI模型表
   - id, market_id, ai_model

5. **transactions** - 交易表
   - id, market_id, type, option, amount, price, total, trader, timestamp, status

6. **orders** - 订单表（可选，如果使用订单簿系统）
   - id, market_id, type, price, amount, trader, status, create_time, update_time

7. **price_history** - 价格历史表（可选，用于图表）
   - id, market_id, timestamp, price, volume, yes_price, no_price

8. **rewards** - 奖励表
   - id, market_id, creator, amount, status, settled_at, claimed_at

9. **trader_stats** - 交易者统计表（可选，用于排行榜）
   - id, trader, name, avatar, total_profit, win_rate, total_trades, update_time


---

## 注意事项

1. 所有涉及金额的字段建议使用 Decimal 类型，避免浮点数精度问题
2. 时间戳字段统一使用 Unix timestamp 或 ISO 8601 格式
3. 用户标识统一使用钱包地址
4. 价格计算需要考虑 AMM (自动做市商) 算法，确保 yes_price + no_price = 1
5. 交易完成后需要更新市场的 volume, liquidity, yes_price, no_price 等字段
6. 建议使用事务确保数据一致性
7. 对于高频查询的接口（如价格历史、订单簿），建议使用缓存机制
