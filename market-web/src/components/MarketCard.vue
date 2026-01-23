<template>
  <div class="market-card" @click="handleCardClick">
    <div class="card-content">
      <div class="card-header">
        <div class="tags-list">
          <span
            v-for="tag in displayTags"
            :key="tag.code"
            class="tag-item"
          >
            {{ tag.desc }}
          </span>
          <span v-if="market.marketStatus !== undefined" class="status-badge" :class="getMarketStatusClass(market.marketStatus)">
            {{ getMarketStatusText(market.marketStatus) }}
          </span>
        </div>
        <span class="end-date">{{ formatDate(market.endTime) }}</span>
      </div>

      <h3 class="question">{{ market.question }}</h3>
      <p class="description">{{ market.description }}</p>

      <!-- 发布市场按钮 (仅当终审通过时显示) -->
      <OpenMarketButton
        v-if="market.marketStatus === 3"
        :market="market"
        @deployed="handleDeployed"
        @click.stop
      />

      <div class="card-stats" v-show="market.marketStatus !== 3">
        <div class="stat-item" v-if="market.creator">
          <span class="stat-label">Creator</span>
          <span class="stat-value creator-address">{{ formatAddress(market.creator) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Volume</span>
          <span class="stat-value">${{ formatVolume(market.volume) }}</span>
        </div>
      </div>

      <div class="probability-section" v-show="market.marketStatus !== 3">
        <div class="probability-header">
          <span class="probability-label">Current Probability</span>
          <span class="probability-value">{{ Math.round(yesProbability * 100) }}%</span>
        </div>
        <div class="probability-bar">
          <div class="probability-fill" :style="{ width: (yesProbability * 100) + '%' }"></div>
        </div>
      </div>
    </div>

    <div class="card-actions" v-show="market.marketStatus !== 3 && market.marketStatus >= 5">
      <button class="btn-yes" @click.stop="handleBet('yes')">
        <span class="btn-label">Yes</span>
        <span class="btn-price">{{ Math.round(market.yesPrice * 100) }}¢</span>
      </button>
      <button class="btn-no" @click.stop="handleBet('no')">
        <span class="btn-label">No</span>
        <span class="btn-price">{{ Math.round(market.noPrice * 100) }}¢</span>
      </button>
    </div>

    <!-- 交易弹窗 -->
    <TradeModal
      :show="showTradeModal"
      :market="market"
      :tradeType="tradeType"
      @close="showTradeModal = false"
      @success="handleTradeSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getMarketStatusText, getMarketStatusClass } from '../constants/marketStatus'
import OpenMarketButton from './OpenMarketButton.vue'
import TradeModal from './TradeModal.vue'

const props = defineProps({
  market: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['bet', 'click'])

// 交易弹窗状态
const showTradeModal = ref(false)
const tradeType = ref('yes')

// 计算Yes的概率 = yesPrice / (yesPrice + noPrice)，向下取整保留两位小数
const yesProbability = computed(() => {
  const yesPrice = props.market.yesPrice || 0
  const noPrice = props.market.noPrice || 0
  const total = yesPrice + noPrice

  if (total === 0) return 0

  // 计算概率，向下取整保留两位小数
  return Math.floor((yesPrice / total) * 100) / 100
})

// 显示标签列表
const displayTags = computed(() => {
  // 如果 market 已经有 tags 数组（包含 {code, desc}），直接返回
  if (Array.isArray(props.market.tags) && props.market.tags.length > 0) {
    // 检查第一个元素是否是对象（有 desc 属性）
    if (props.market.tags[0] && typeof props.market.tags[0] === 'object' && props.market.tags[0].desc) {
      return props.market.tags
    }
    // 如果是 code 数组，需要从 availableTags 查找（这个先留空，因为MarketList会传入转换后的数据）
    return []
  }

  // 降级：如果 market.categoryDesc 存在，使用它
  if (props.market.categoryDesc) {
    return [{ code: props.market.category, desc: props.market.categoryDesc }]
  }

  return []
})

const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = date - now
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays < 30) {
    return `${diffDays}d left`
  }
  const diffMonths = Math.ceil(diffDays / 30)
  if (diffMonths < 12) {
    return `${diffMonths}mo left`
  }
  const diffYears = Math.ceil(diffDays / 365)
  return `${diffYears}y left`
}

const formatVolume = (volume) => {
  if (!volume) return '0'
  if (volume >= 1000000) {
    return (volume / 1000000).toFixed(1) + 'M'
  }
  if (volume >= 1000) {
    return (volume / 1000).toFixed(0) + 'K'
  }
  return volume.toString()
}

const formatAddress = (address) => {
  if (!address) return ''
  if (address.startsWith('0x')) {
    return `${address.slice(0, 6)}...${address.slice(-4)}`
  }
  // 如果不是以太坊地址，只显示前10个字符
  if (address.length > 14) {
    return `${address.slice(0, 10)}...`
  }
  return address
}

const handleCardClick = () => {
  emit('click')
}

const handleDeployed = (data) => {
  console.log('[MarketCard] 市场已部署:', data)

  // ✅ 如果前端解析出了 marketAddress，立即更新到市场对象
  // 这样用户可以立即开始交易，无需等待后端更新数据库
  if (data.marketAddress && props.market.id === data.marketId) {
    props.market.marketAddress = data.marketAddress
    props.market.onChainMarketId = data.onChainMarketId
    console.log('[MarketCard] ✅ 已更新 marketAddress, 用户可以立即交易:', data.marketAddress)
  }
}

const handleBet = (type) => {
  // 检查市场是否已部署
  if (!props.market.marketAddress) {
    alert('该市场尚未部署到区块链，无法交易。请等待市场部署完成。')
    return
  }

  // 打开交易弹窗
  tradeType.value = type
  showTradeModal.value = true
}

const handleTradeSuccess = (data) => {
  console.log('[MarketCard] 交易成功:', data)

  // 通知父组件
  emit('bet', {
    marketId: props.market.id,
    type: data.type,
    amount: data.amount,
    cost: data.cost,
    txHash: data.txHash
  })

  // 这里可以添加成功提示
  // 例如刷新余额、更新价格等
}
</script>

<style scoped>
.market-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.market-card:hover {
  border-color: var(--border-hover);
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
}

.card-content {
  padding: 1.5rem;
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  gap: 0.5rem;
}

.tags-list {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  flex: 1;
}

.tag-item {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.75rem;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.75rem;
  white-space: nowrap;
}

.status-pending {
  background: #FEF3C7;
  color: #92400E;
}

.status-rejected {
  background: var(--danger-bg);
  color: var(--danger);
}

.status-preliminary {
  background: #DBEAFE;
  color: #1E40AF;
}

.status-final {
  background: #E0E7FF;
  color: #4338CA;
}

.status-active {
  background: var(--success-bg);
  color: var(--success);
}

.status-closed {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.status-arbitrating {
  background: #E0E7FF;
  color: #4338CA;
}

.status-challenging {
  background: #FEE2E2;
  color: #DC2626;
}

.status-resolved {
  background: #D1FAE5;
  color: #065F46;
}

.status-unknown {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.status-deploying {
  background: #E0E7FF;
  color: #4338CA;
}

.status-final-arbitrated {
  background: #FEE2E2;
  color: #DC2626;
}

.status-settling {
  background: #FEF3C7;
  color: #92400E;
}

.end-date {
  font-size: 0.875rem;
  color: var(--text-tertiary);
  font-weight: 500;
  flex-shrink: 0;
}

.question {
  font-size: 1.125rem;
  font-weight: 600;
  letter-spacing: -0.025em;
  margin-bottom: 0.75rem;
  line-height: 1.5;
  color: var(--text-primary);
}

.description {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-stats {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stat-item {
  display: flex;
  gap: 0.5rem;
  align-items: baseline;
}

.creator-address {
  font-family: 'SF Mono', 'Monaco', 'Consolas', 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.75rem;
  opacity: 0.8;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

.stat-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 600;
}

.probability-section {
  margin-top: auto;
}

.probability-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.probability-label {
  font-size: 0.875rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

.probability-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 600;
}

.probability-bar {
  height: 6px;
  background: var(--bg-secondary);
  border-radius: 3px;
  overflow: hidden;
}

.probability-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent-light) 0%, var(--accent) 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.card-actions {
  display: flex;
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.btn-no,
.btn-yes {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  border: none;
  cursor: pointer;
  transition: background 0.15s ease;
  background: transparent;
  gap: 0.25rem;
}

.btn-yes {
  border-right: 1px solid var(--border-color);
}

.btn-no:hover {
  background: var(--danger-bg);
}

.btn-no .btn-price {
  color: var(--danger);
}

.btn-yes:hover {
  background: var(--success-bg);
}

.btn-yes .btn-price {
  color: var(--success);
}

.btn-label {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.btn-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}
</style>
