<template>
  <div class="orderbook-container">
    <div class="orderbook-header">
      <h3 class="title">Order Book</h3>
      <div class="spread-info">
        <span class="spread-label">Spread</span>
        <span class="spread-value">{{ spread.toFixed(2) }}¢</span>
      </div>
    </div>

    <div class="orderbook-content">
      <!-- 卖单 (Asks) -->
      <div class="orders-section asks">
        <div class="orders-header">
          <span class="col-header price">Price (¢)</span>
          <span class="col-header amount">Amount (IMKT)</span>
          <span class="col-header total">Total</span>
        </div>
        <div class="orders-list">
          <div
            v-for="(order, index) in reversedAsks"
            :key="`ask-${index}`"
            class="order-row ask"
          >
            <div class="depth-bar" :style="{ width: order.depth + '%' }"></div>
            <span class="order-price">{{ (order.price * 100).toFixed(1) }}</span>
            <span class="order-amount">{{ formatNumber(order.amount) }}</span>
            <span class="order-total">{{ formatNumber(order.total) }}</span>
          </div>
        </div>
      </div>

      <!-- 当前价格 -->
      <div class="current-price">
        <span class="price-value">{{ (currentPrice * 100).toFixed(1) }}¢</span>
        <span class="price-label">Current Price</span>
      </div>

      <!-- 买单 (Bids) -->
      <div class="orders-section bids">
        <div class="orders-header">
          <span class="col-header price">Price (¢)</span>
          <span class="col-header amount">Amount (IMKT)</span>
          <span class="col-header total">Total</span>
        </div>
        <div class="orders-list">
          <div
            v-for="(order, index) in bids"
            :key="`bid-${index}`"
            class="order-row bid"
          >
            <div class="depth-bar" :style="{ width: order.depth + '%' }"></div>
            <span class="order-price">{{ (order.price * 100).toFixed(1) }}</span>
            <span class="order-amount">{{ formatNumber(order.amount) }}</span>
            <span class="order-total">{{ formatNumber(order.total) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="orderbook-summary">
      <div class="summary-item">
        <span class="label">Max Bid</span>
        <span class="value bid">{{ (maxBid * 100).toFixed(1) }}¢</span>
      </div>
      <div class="summary-item">
        <span class="label">Min Ask</span>
        <span class="value ask">{{ (minAsk * 100).toFixed(1) }}¢</span>
      </div>
      <div class="summary-item">
        <span class="label">Total Volume</span>
        <span class="value">{{ formatNumber(totalVolume) }} IMKT</span>
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

const currentPrice = ref(0.65)

// 生成模拟订单数据
const generateOrders = (basePrice, side) => {
  const orders = []
  let total = 0

  for (let i = 0; i < 8; i++) {
    const priceStep = side === 'ask' ? 0.002 : -0.002
    const price = basePrice + (priceStep * (i + 1))
    const amount = Math.floor(Math.random() * 50000) + 5000
    total += amount

    orders.push({
      price: Number(price.toFixed(3)),
      amount,
      total,
      depth: Math.floor(Math.random() * 40) + 60
    })
  }

  return orders
}

const asks = ref([])
const bids = ref([])

const updateOrders = () => {
  asks.value = generateOrders(currentPrice.value, 'ask')
  bids.value = generateOrders(currentPrice.value, 'bid')
}

onMounted(() => {
  updateOrders()
})

const reversedAsks = computed(() => {
  return [...asks.value].reverse()
})

const maxBid = computed(() => {
  return bids.value[0]?.price || currentPrice.value
})

const minAsk = computed(() => {
  return asks.value[0]?.price || currentPrice.value
})

const spread = computed(() => {
  return (minAsk.value - maxBid.value) * 100
})

const totalVolume = computed(() => {
  const bidTotal = bids.value.reduce((sum, o) => sum + o.amount, 0)
  const askTotal = asks.value.reduce((sum, o) => sum + o.amount, 0)
  return bidTotal + askTotal
})

const formatNumber = (num) => {
  if (!num) return '0'
  return num.toLocaleString('en-US', { maximumFractionDigits: 0 })
}

// 模拟实时更新
let updateInterval = null

onMounted(() => {
  updateInterval = setInterval(() => {
    // 更新当前价格
    const priceChange = (Math.random() - 0.5) * 0.001
    currentPrice.value = Math.max(0.01, Math.min(0.99, currentPrice.value + priceChange))

    // 更新订单
    updateOrders()
  }, 2000)
})

onUnmounted(() => {
  if (updateInterval) {
    clearInterval(updateInterval)
  }
})
</script>

<style scoped>
.orderbook-container {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.orderbook-header {
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

.spread-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--input-bg);
  border-radius: 6px;
}

.spread-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.spread-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--accent-light);
}

.orderbook-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.orders-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.orders-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
  padding: 8px 12px;
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
.col-header.total {
  text-align: right;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-row {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 4px;
  overflow: hidden;
}

.order-row.ask {
  color: #EF4444;
}

.order-row.bid {
  color: #22C55E;
}

.depth-bar {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  opacity: 0.15;
  pointer-events: none;
}

.order-row.ask .depth-bar {
  background: #EF4444;
}

.order-row.bid .depth-bar {
  background: #22C55E;
}

.order-price {
  font-size: 13px;
  font-weight: 600;
  text-align: left;
  position: relative;
  z-index: 1;
}

.order-amount,
.order-total {
  font-size: 13px;
  text-align: right;
  position: relative;
  z-index: 1;
}

.current-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: var(--accent-light);
  border-radius: 8px;
  color: white;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
}

.price-label {
  font-size: 12px;
  opacity: 0.9;
}

.orderbook-summary {
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

.summary-item .value.bid {
  color: #22C55E;
}

.summary-item .value.ask {
  color: #EF4444;
}

@media (max-width: 768px) {
  .orderbook-container {
    padding: 16px;
  }

  .current-price {
    padding: 10px;
  }

  .price-value {
    font-size: 18px;
  }

  .orderbook-summary {
    grid-template-columns: 1fr;
  }
}
</style>
