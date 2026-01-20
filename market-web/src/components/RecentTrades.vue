<template>
  <div class="recent-trades-container">
    <div class="trades-header">
      <h3 class="title">Recent Trades</h3>
      <div class="filters">
        <button
          :class="['filter-btn', { active: selectedFilter === 'all' }]"
          @click="selectedFilter = 'all'"
        >
          All
        </button>
        <button
          :class="['filter-btn', { active: selectedFilter === 'buy' }]"
          @click="selectedFilter = 'buy'"
        >
          Buys
        </button>
        <button
          :class="['filter-btn', { active: selectedFilter === 'sell' }]"
          @click="selectedFilter = 'sell'"
        >
          Sells
        </button>
      </div>
    </div>

    <div class="trades-table-header">
      <span class="col-header price">Price (¢)</span>
      <span class="col-header amount">Amount (IMKT)</span>
      <span class="col-header time">Time</span>
    </div>

    <div class="trades-list">
      <div
        v-for="trade in filteredTrades"
        :key="trade.id"
        class="trade-row"
        :class="trade.type"
      >
        <span class="trade-price">{{ (trade.price * 100).toFixed(1) }}</span>
        <span class="trade-amount">{{ formatNumber(trade.amount) }}</span>
        <span class="trade-time">{{ trade.time }}</span>
      </div>
    </div>

    <div class="trades-summary">
      <div class="summary-item">
        <span class="label">Total Trades</span>
        <span class="value">{{ trades.length }}</span>
      </div>
      <div class="summary-item">
        <span class="label">Total Volume</span>
        <span class="value">{{ formatNumber(totalVolume) }} IMKT</span>
      </div>
      <div class="summary-item">
        <span class="label">Last Price</span>
        <span class="value">{{ lastPrice }}¢</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  marketId: {
    type: [String, Number],
    required: true
  }
})

const selectedFilter = ref('all')
const trades = ref([])

// 生成模拟交易数据
const generateTrade = () => {
  const type = Math.random() > 0.5 ? 'buy' : 'sell'
  const basePrice = 0.65
  const priceVariation = (Math.random() - 0.5) * 0.01
  const price = Math.max(0.01, Math.min(0.99, basePrice + priceVariation))
  const amount = Math.floor(Math.random() * 10000) + 500

  return {
    id: Date.now() + Math.random(),
    type,
    price: Number(price.toFixed(3)),
    amount,
    timestamp: Date.now()
  }
}

const formatNumber = (num) => {
  if (!num) return '0'
  return num.toLocaleString('en-US', { maximumFractionDigits: 0 })
}

const formatTime = (timestamp) => {
  const now = Date.now()
  const diff = now - timestamp

  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`

  const days = Math.floor(hours / 24)
  return `${days}d ago`
}

// 初始化一些交易数据
for (let i = 0; i < 20; i++) {
  const trade = generateTrade()
  trade.timestamp = Date.now() - i * 30000
  trade.time = formatTime(trade.timestamp)
  trades.value.push(trade)
}

const filteredTrades = computed(() => {
  if (selectedFilter.value === 'all') {
    return trades.value
  }
  return trades.value.filter(t => t.type === selectedFilter.value)
})

const totalVolume = computed(() => {
  return filteredTrades.value.reduce((sum, t) => sum + t.amount, 0)
})

const lastPrice = computed(() => {
  if (trades.value.length === 0) return '0.0'
  return (trades.value[0].price * 100).toFixed(1)
})

// 模拟实时交易更新
let updateInterval = null

onMounted(() => {
  updateInterval = setInterval(() => {
    const newTrade = generateTrade()
    newTrade.time = formatTime(newTrade.timestamp)
    trades.value.unshift(newTrade)

    // 保持最多50条记录
    if (trades.value.length > 50) {
      trades.value.pop()
    }
  }, 2000)
})

onUnmounted(() => {
  if (updateInterval) {
    clearInterval(updateInterval)
  }
})
</script>

<style scoped>
.recent-trades-container {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.trades-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.filters {
  display: flex;
  gap: 4px;
}

.filter-btn {
  padding: 6px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  background: var(--input-bg);
  color: var(--text-primary);
}

.filter-btn.active {
  background: var(--input-bg);
  color: var(--accent-light);
  font-weight: 600;
}

.trades-table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 80px;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--border-color);
}

.col-header {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}

.col-header.price {
  text-align: left;
}

.col-header.amount,
.col-header.time {
  text-align: right;
}

.trades-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 400px;
}

.trade-row {
  display: grid;
  grid-template-columns: 1fr 1fr 80px;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.15s;
}

.trade-row:hover {
  background: var(--input-bg);
}

.trade-row.buy .trade-price {
  color: #22C55E;
}

.trade-row.sell .trade-price {
  color: #EF4444;
}

.trade-price {
  font-size: 13px;
  font-weight: 600;
  text-align: left;
}

.trade-amount,
.trade-time {
  font-size: 13px;
  color: var(--text-secondary);
  text-align: right;
}

.trade-time {
  font-size: 12px;
}

.trades-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid var(--border-color);
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: center;
}

.summary-item .label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.summary-item .value {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

/* 滚动条样式 */
.trades-list::-webkit-scrollbar {
  width: 6px;
}

.trades-list::-webkit-scrollbar-track {
  background: var(--input-bg);
  border-radius: 3px;
}

.trades-list::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.trades-list::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

@media (max-width: 768px) {
  .recent-trades-container {
    padding: 16px;
  }

  .trades-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .filters {
    justify-content: space-between;
  }

  .trades-summary {
    grid-template-columns: 1fr;
  }
}
</style>
