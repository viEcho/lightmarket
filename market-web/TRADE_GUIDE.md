# 前端交易功能使用指南

## 📋 概述

本文档说明如何在前端页面调用合约的买入/卖出功能，以及如何监听事件并记录持仓。

## 🔧 核心文件

### 1. `/src/utils/trade.js` - 交易工具模块

包含以下核心功能：

- **buyYes()** - 买入 YES 代币
- **sellYes()** - 卖出 YES 代币（相当于买入 NO）
- **getUserPosition()** - 查询用户持仓
- **getPoolInfo()** - 查询池子信息
- **listenMarketEvents()** - 监听市场事件
- **recordPosition()** - 记录持仓到后端（待实现）

### 2. `/src/components/TradePanel.vue` - 交易面板组件

完整的交易界面组件，可以直接使用或参考。

## 🚀 快速开始

### 基础用法

```vue
<script setup>
import { buyYes, sellYes, getUserPosition, getPoolInfo } from '@/utils/trade'

// 买入 YES
const handleBuyYes = async () => {
  const result = await buyYes(marketAddress, 100, {
    onTransactionHash: (hash) => {
      console.log('交易已提交:', hash)
    },
    onReceipt: (receipt) => {
      console.log('交易已确认')
    },
    onEvent: (eventData) => {
      console.log('事件数据:', eventData)
      // eventData: { user, usdcSpent, yesReceived, newPrice }
    }
  })

  if (result.success) {
    console.log('交易成功!', result.eventData)
  }
}

// 卖出 YES（买入 NO）
const handleSellYes = async () => {
  const result = await sellYes(marketAddress, 50, {
    onEvent: (eventData) => {
      console.log('事件数据:', eventData)
      // eventData: { user, yesSold, usdcReceived, newPrice }
    }
  })

  if (result.success) {
    console.log('交易成功!', result.eventData)
  }
}

// 查询持仓
const loadPosition = async () => {
  const position = await getUserPosition(marketAddress)
  console.log('持仓:', position)
  // position: { yesAmount, noAmount, lockedAmount, withdrawable }
}

// 查询池子信息
const loadPoolInfo = async () => {
  const info = await getPoolInfo(marketAddress)
  console.log('池子信息:', info)
  // info: { yesPool, noPool, yesPrice, noPrice }
}
</script>
```

## 📡 事件监听

### 自动监听模式

```vue
<script setup>
import { listenMarketEvents } from '@/utils/trade'
import { ref, onUnmounted } from 'vue'

const eventListenerEnabled = ref(true)
let stopEventListener = null

// 启用事件监听
const startEventListener = () => {
  stopEventListener = listenMarketEvents(marketAddress, {
    onBoughtYes: (eventData) => {
      console.log('📢 有人买入 YES:', eventData)
      // eventData: { user, usdcSpent, yesReceived, newPrice }

      // 刷新持仓
      refreshPosition()
    },
    onSoldYes: (eventData) => {
      console.log('📢 有人卖出 YES:', eventData)
      // eventData: { user, yesSold, usdcReceived, newPrice }

      // 刷新持仓
      refreshPosition()
    }
  })
}

// 组件卸载时停止监听
onUnmounted(() => {
  if (stopEventListener) {
    stopEventListener()
  }
})

// 切换监听
const toggleListener = () => {
  if (eventListenerEnabled.value) {
    startEventListener()
  } else {
    if (stopEventListener) {
      stopEventListener()
    }
  }
}
</script>
```

## 🗄️ 后端持仓记录

### 需要后端实现的接口

```javascript
// POST /api/position/record
// 记录用户的交易持仓

{
  "marketAddress": "0x123...",
  "action": "buyYes",           // 或 "sellYes"
  "usdcSpent": "10.5",          // 支付的 USDC（买入时）
  "yesReceived": "20.8",        // 获得的 YES（买入时）
  "yesSold": "50.0",            // 卖出的 YES（卖出时）
  "usdcReceived": "25.3",       // 获得的 USDC（卖出时）
  "price": "65",                // 成交价格（cents）
  "txHash": "0xabc..."          // 交易哈希
}
```

### 后端字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| marketAddress | string | 市场合约地址 |
| action | string | 交易类型：`buyYes` 或 `sellYes` |
| usdcSpent | string | 买入时支付的 USDC 数量 |
| yesReceived | string | 买入时获得的 YES 数量 |
| yesSold | string | 卖出时卖出的 YES 数量 |
| usdcReceived | string | 卖出时获得的 USDC 数量 |
| price | string | 成交价格（0-100 cents） |
| txHash | string | 链上交易哈希 |

### 持仓表设计建议

```sql
CREATE TABLE user_position (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,           -- 用户ID
  market_id BIGINT NOT NULL,         -- 市场ID
  market_address VARCHAR(42),        -- 市场合约地址

  -- 持仓数量
  yes_amount DECIMAL(20, 6) DEFAULT 0,  -- YES 持仓
  no_amount DECIMAL(20, 6) DEFAULT 0,   -- NO 持仓

  -- 成本信息
  total_cost DECIMAL(20, 6) DEFAULT 0,  -- 总成本（USDC）
  avg_price DECIMAL(10, 2) DEFAULT 0,  -- 平均价格

  -- 交易信息
  latest_tx_hash VARCHAR(66),         -- 最新交易哈希
  latest_tx_time TIMESTAMP,           -- 最新交易时间

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_user_market (user_id, market_id)
);

-- 交易历史表
CREATE TABLE trade_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  market_id BIGINT NOT NULL,
  market_address VARCHAR(42),

  -- 交易信息
  action VARCHAR(20),                  -- buyYes 或 sellYes
  usdc_amount DECIMAL(20, 6),          -- USDC 数量
  yes_amount DECIMAL(20, 6),           -- YES 数量
  price DECIMAL(10, 2),                -- 成交价格

  tx_hash VARCHAR(66) UNIQUE,
  block_number BIGINT,
  timestamp TIMESTAMP,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_user_market (user_id, market_id),
  INDEX idx_tx_hash (tx_hash)
);
```

## 🔗 在 MarketDetail.vue 中集成

```vue
<script setup>
import TradePanel from '@/components/TradePanel.vue'
import { ref, onMounted } from 'vue'

const market = ref({
  id: '1',
  title: '测试市场',
  marketAddress: '0x123...', // 从后端获取的市场合约地址
  endTime: '2025-12-31T23:59:59Z'
})

// 组件会自动处理：
// 1. 连接钱包
// 2. 买入/卖出 YES
// 3. 查询持仓
// 4. 监听事件
// 5. 记录持仓到后端
</script>

<template>
  <div class="market-detail">
    <h1>{{ market.title }}</h1>
    <!-- 其他市场信息 -->

    <!-- 交易面板 -->
    <TradePanel :marketAddress="market.marketAddress" />
  </div>
</template>
```

## ⚠️ 注意事项

### 1. 合约调用流程

```
用户点击"买入 YES"
  ↓
1. 检查 USDC 余额
  ↓
2. Approve USDC（如果需要）
  ↓
3. 调用 market.buyYes(usdcAmount)
  ↓
4. 等待交易确认
  ↓
5. 解析 BoughtYes 事件
  ↓
6. 记录持仓到后端
```

### 2. 买入 NO 的方式

合约中没有 `buyNo()` 函数，买入 NO 相当于卖出 YES：

```javascript
// 用户想要买入 NO = 卖出 YES
await sellYes(marketAddress, yesAmount)
```

### 3. 事件监听的两种方式

**方式1：主动监听（推荐）**
```javascript
// 交易时在回调中获取事件
await buyYes(marketAddress, amount, {
  onEvent: (eventData) => {
    // 交易完成后自动触发
    recordPosition(eventData)
  }
})
```

**方式2：被动监听**
```javascript
// 监听链上所有事件
listenMarketEvents(marketAddress, {
  onBoughtYes: (eventData) => {
    // 任何人交易都会触发
    recordPosition(eventData)
  }
})
```

### 4. 环境变量配置

在 `.env` 文件中：

```env
VITE_USDC_ADDRESS=0x...
VITE_FACTORY_ADDRESS=0x...
VITE_CHAIN_ID=31337
```

## 📊 数据流程

```
前端交易
  ↓
合约调用（链上）
  ├→ 事件触发（BoughtYes/SoldYes）
  ├→ 池子更新
  └→ 用户余额更新
  ↓
前端监听事件
  ↓
记录到后端
  ├→ 持仓表（user_position）
  └→ 交易历史（trade_history）
  ↓
前端显示持仓
```

## 🔍 调试技巧

### 查看完整日志

```javascript
// 在 trade.js 中已经添加了详细的 console.log
// 打开浏览器控制台即可看到：
// [Trade] 开始买入 YES...
// [Trade] 市场地址: 0x...
// [Trade] USDC 余额: 1000
// [Trade] 授权 USDC to Market...
// [Trade] ✅ 授权成功
// [Trade] 执行 buyYes 交易...
// [Trade] 交易已提交: 0xabc...
// [Trade] 等待交易确认...
// [Trade] ✅ 交易已确认
// [Trade] 解析 BoughtYes 事件...
// [Trade] ✅ 事件解析成功: { user: '...', ... }
// [Trade] 📡 记录持仓到后端
```

### 错误处理

所有错误都会被捕获并返回用户友好的信息：

```javascript
{
  success: false,
  error: "用户拒绝了交易",  // 用户取消
  code: "ACTION_REJECTED"
}
```

## 📚 相关文档

- [OrderbookMarket.sol](../market-contract/contracts/OrderbookMarket.sol) - 市场合约
- [MarketFactory.sol](../market-contract/contracts/MarketFactory.sol) - 工厂合约
- [openMarket.js](./openMarket.js) - 开放市场工具
