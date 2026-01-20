<template>
  <div class="activity-page">
    <div class="page-header">
      <h1 class="page-title">Recent Activity</h1>
      <p class="page-subtitle">Live trading activity across all markets</p>
    </div>

    <div class="activity-filters">
      <button
        v-for="filter in filters"
        :key="filter"
        class="filter-btn"
        :class="{ active: activeFilter === filter }"
        @click="activeFilter = filter"
      >
        {{ filter }}
      </button>
    </div>

    <div class="activity-list">
      <div
        v-for="activity in filteredActivities"
        :key="activity.id"
        class="activity-item"
      >
        <div class="activity-icon" :class="activity.type">
          <svg v-if="activity.type === 'buy'" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 3L13 8L8 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3 8H13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 13L3 8L8 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M13 8H3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="activity-content">
          <div class="activity-header">
            <span class="activity-user">{{ activity.user }}</span>
            <span class="activity-action">{{ activity.type === 'buy' ? 'bought' : 'sold' }}</span>
            <span class="activity-amount">${{ activity.amount.toLocaleString() }}</span>
            <span class="activity-position">{{ activity.position }}</span>
            <span class="activity-price">@ {{ activity.price }}¢</span>
          </div>
          <div class="activity-market">{{ activity.market }}</div>
        </div>
        <div class="activity-time">{{ activity.time }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeFilter = ref('All')

const filters = ['All', 'Buys', 'Sells']

const activities = ref([
  {
    id: 1,
    user: 'CryptoWhale',
    type: 'buy',
    amount: 5000,
    position: 'YES',
    price: 65,
    market: 'Will Bitcoin exceed $150,000 by end of 2025?',
    time: '2m'
  },
  {
    id: 2,
    user: 'DiamondHands',
    type: 'sell',
    amount: 2500,
    position: 'NO',
    price: 35,
    market: 'Will Ethereum reach $10,000 in 2025?',
    time: '5m'
  },
  {
    id: 3,
    user: 'PredictionPro',
    type: 'buy',
    amount: 10000,
    position: 'YES',
    price: 42,
    market: 'Will Ethereum reach $10,000 in 2025?',
    time: '8m'
  },
  {
    id: 4,
    user: 'MarketMaster',
    type: 'buy',
    amount: 3200,
    position: 'NO',
    price: 72,
    market: 'Will AI pass the Turing test convincingly by 2026?',
    time: '12m'
  },
  {
    id: 5,
    user: 'AlphaSeeker',
    type: 'sell',
    amount: 8000,
    position: 'YES',
    price: 58,
    market: 'Will Tesla deliver more than 2 million vehicles in 2025?',
    time: '15m'
  },
  {
    id: 6,
    user: 'WhaleTrader',
    type: 'buy',
    amount: 15000,
    position: 'YES',
    price: 52,
    market: 'Will a Democrat win the 2028 US Presidential Election?',
    time: '18m'
  },
  {
    id: 7,
    user: 'RiskTaker',
    type: 'buy',
    amount: 2000,
    position: 'YES',
    price: 28,
    market: 'Will SpaceX successfully land humans on Mars by 2030?',
    time: '22m'
  },
  {
    id: 8,
    user: 'SteadyTrader',
    type: 'sell',
    amount: 4500,
    position: 'NO',
    price: 58,
    market: 'Will Tesla deliver more than 2 million vehicles in 2025?',
    time: '25m'
  }
])

const filteredActivities = computed(() => {
  if (activeFilter.value === 'All') {
    return activities.value
  }
  return activities.value.filter(a =>
    activeFilter.value === 'Buys' ? a.type === 'buy' : a.type === 'sell'
  )
})
</script>

<style scoped>
.activity-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 3rem 2rem;
  width: 100%;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  letter-spacing: -0.025em;
  margin-bottom: 0.75rem;
  color: var(--text-primary);
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  font-weight: 400;
}

.activity-filters {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 2rem;
}

.filter-btn {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 0.625rem 1.25rem;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.15s ease;
}

.filter-btn:hover {
  background: var(--bg-secondary);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.filter-btn.active {
  background: var(--accent);
  border-color: var(--accent);
  color: white;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  transition: all 0.15s ease;
}

.activity-item:hover {
  background: var(--bg-secondary);
  border-color: var(--border-hover);
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-icon.buy {
  background: var(--success-bg);
  color: var(--success);
}

.activity-icon.sell {
  background: var(--danger-bg);
  color: var(--danger);
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-header {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  flex-wrap: wrap;
  margin-bottom: 0.375rem;
}

.activity-user {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.875rem;
}

.activity-action {
  color: var(--text-tertiary);
  font-size: 0.875rem;
}

.activity-amount {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.875rem;
}

.activity-position {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  padding: 0.125rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.activity-price {
  color: var(--text-tertiary);
  font-size: 0.875rem;
}

.activity-market {
  font-size: 0.875rem;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  font-weight: 500;
  flex-shrink: 0;
}
</style>
