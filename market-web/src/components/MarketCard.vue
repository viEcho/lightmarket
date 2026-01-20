<template>
  <div class="market-card" @click="handleCardClick">
    <div class="card-content">
      <div class="card-header">
        <span class="category">{{ market.category }}</span>
        <span class="end-date">{{ formatDate(market.endDate) }}</span>
      </div>

      <h3 class="question">{{ market.question }}</h3>
      <p class="description">{{ market.description }}</p>

      <div class="card-stats">
        <div class="stat-item">
          <span class="stat-label">Volume</span>
          <span class="stat-value">${{ formatVolume(market.volume) }}</span>
        </div>
      </div>

      <div class="probability-section">
        <div class="probability-header">
          <span class="probability-label">Current Probability</span>
          <span class="probability-value">{{ Math.round(market.yesPrice * 100) }}%</span>
        </div>
        <div class="probability-bar">
          <div class="probability-fill" :style="{ width: (market.yesPrice * 100) + '%' }"></div>
        </div>
      </div>
    </div>

    <div class="card-actions">
      <button class="btn-no" @click.stop="handleBet('no')">
        <span class="btn-label">No</span>
        <span class="btn-price">{{ Math.round((1 - market.yesPrice * 100)) }}¢</span>
      </button>
      <button class="btn-yes" @click.stop="handleBet('yes')">
        <span class="btn-label">Yes</span>
        <span class="btn-price">{{ Math.round(market.yesPrice * 100) }}¢</span>
      </button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  market: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['bet', 'click'])

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
  if (volume >= 1000000) {
    return (volume / 1000000).toFixed(1) + 'M'
  }
  if (volume >= 1000) {
    return (volume / 1000).toFixed(0) + 'K'
  }
  return volume.toString()
}

const handleCardClick = () => {
  emit('click')
}

const handleBet = (type) => {
  emit('bet', {
    marketId: props.market.id,
    type,
    price: type === 'yes' ? props.market.yesPrice : 1 - props.market.yesPrice
  })
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
}

.category {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.end-date {
  font-size: 0.875rem;
  color: var(--text-tertiary);
  font-weight: 500;
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
}

.stat-item {
  display: flex;
  gap: 0.5rem;
  align-items: baseline;
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

.btn-no {
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
