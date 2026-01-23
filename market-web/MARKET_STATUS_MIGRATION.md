# 市场状态映射统一化

## 问题

之前市场状态映射分散在多个组件中,导致状态显示不一致:
- MarketCard.vue: 状态码映射不完整,缺少状态 4-10,99
- AdminDashboard.vue: 使用本地 getStatusText 和 getStatusClass 方法
- useMarkets.js: 使用本地 getMarketStatus 方法
- 状态映射规则不一致

## 解决方案

创建了全局统一的状态映射文件: `src/constants/marketStatus.js`

### 状态码对照表

```
0  - 待审核
1  - 已拒绝
2  - 初审通过
3  - 终审通过
4  - 发布中
5  - 已发布
6  - 已关闭
7  - 裁决中
8  - 挑战中
9  - 已终裁
10 - 结算中
99 - 已结算
```

### 修改的文件

1. **新增文件**: `src/constants/marketStatus.js`
   - MARKET_STATUS_TEXT: 状态码 -> 中文文本
   - MARKET_STATUS_CLASS: 状态码 -> CSS类名
   - MARKET_STATUS_LOGICAL: 状态码 -> 逻辑状态
   - MARKET_STAGE_MAP: 状态码 -> 审核阶段
   - 辅助函数: getMarketStatusText(), getMarketStatusClass(), getMarketStatusLogical()

2. **修改**: `src/components/MarketCard.vue`
   - 导入全局映射函数
   - 删除本地 getStatusText 和 getStatusClass 方法
   - 使用 getMarketStatusText() 和 getMarketStatusClass()
   - 添加缺失状态的CSS类

3. **修改**: `src/components/AdminDashboard.vue`
   - 导入全局映射函数
   - 删除本地 getStatusText 和 getStatusClass 方法
   - 使用 getMarketStatusText() 和 getMarketStatusClass()
   - 添加缺失状态的CSS类

4. **修改**: `src/composables/useMarkets.js`
   - 导入 getMarketStatusLogical
   - 删除本地 getMarketStatus 方法
   - 使用 getMarketStatusLogical() 转换状态

5. **修改**: `src/components/MarketDetail.vue`
   - 导入全局映射函数(为未来使用)

## 使用方法

### 在组件中使用

```javascript
import { getMarketStatusText, getMarketStatusClass } from '@/constants/marketStatus'

// 在模板中
<template>
  <span :class="getMarketStatusClass(market.marketStatus)">
    {{ getMarketStatusText(market.marketStatus) }}
  </span>
</template>
```

### 获取简短类名(用于某些场景)

```javascript
// 返回 'pending' 而不是 'status-pending'
getMarketStatusClass(status, true)
```

### 其他辅助函数

```javascript
import {
  getMarketStatusLogical,  // 获取逻辑状态字符串
  getMarketStage,          // 获取审核阶段
  isMarketTradeable,       // 是否可交易
  isMarketSettled,         // 是否已结算
  isMarketClosed           // 是否已关闭
} from '@/constants/marketStatus'
```

## CSS类名规范

所有市场状态相关的CSS类名统一使用 `status-*` 前缀:

- `.status-pending` - 待审核
- `.status-rejected` - 已拒绝
- `.status-preliminary` - 初审通过
- `.status-final` - 终审通过
- `.status-deploying` - 发布中
- `.status-active` - 已发布
- `.status-closed` - 已关闭
- `.status-arbitrating` - 裁决中
- `.status-challenging` - 挑战中
- `.status-final-arbitrated` - 已终裁
- `.status-settling` - 结算中
- `.status-settled` - 已结算

## 迁移完成情况

- ✅ MarketCard.vue - 使用全局映射
- ✅ AdminDashboard.vue - 使用全局映射
- ✅ useMarkets.js - 使用全局映射
- ✅ MarketDetail.vue - 导入全局映射
- ⏸️ ReviewMarket.vue - 使用独立的审核状态系统,暂不修改

## 注意事项

1. 所有市场状态都应该使用 `marketStatus` 数字字段,不要使用逻辑状态 `status` 字段进行判断
2. 新增状态时,只需更新 `marketStatus.js` 文件即可
3. CSS类名需要同步更新以确保样式正确显示
