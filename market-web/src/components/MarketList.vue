<template>
  <div class="market-list">
    <div class="filters">
      <button
        v-for="filter in filters"
        :key="filter.key"
        class="filter-btn"
        :class="{ active: selectedFilter === filter.key }"
        @click="selectedFilter = filter.key"
      >
        {{ filter.label }}
      </button>
    </div>
    <div class="markets-grid">
      <MarketCard
        v-for="market in filteredMarkets"
        :key="market.id"
        :market="market"
        @click="handleMarketClick(market.id)"
      />
    </div>
    <div v-if="filteredMarkets.length === 0" class="no-results">
      <p>No markets found for this category</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import MarketCard from './MarketCard.vue'

const props = defineProps({
  markets: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['navigate'])

const selectedFilter = ref('all')

const filters = [
  { key: 'all', label: 'All Markets' },
  { key: 'crypto', label: 'Crypto' },
  { key: 'technology', label: 'Technology' },
  { key: 'politics', label: 'Politics' },
  { key: 'sports', label: 'Sports' },
  { key: 'finance', label: 'Finance' },
  { key: 'entertainment', label: 'Entertainment' },
  { key: 'other', label: 'Other' }
]

const filteredMarkets = computed(() => {
  if (selectedFilter.value === 'all') {
    return props.markets
  }
  return props.markets.filter(market => {
    const category = market.category?.toLowerCase() || ''
    return category === selectedFilter.value.toLowerCase()
  })
})

const handleMarketClick = (marketId) => {
  emit('navigate', 'market-detail', marketId)
}
</script>

<style scoped>
.market-list {
  width: 100%;
}

.filters {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-btn {
  background: var(--bg-secondary);
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
  background: var(--bg-tertiary);
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.filter-btn.active {
  background: var(--accent);
  border-color: var(--accent);
  color: white;
}

.markets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 1.5rem;
}

.no-results {
  text-align: center;
  padding: 3rem;
  color: var(--text-secondary);
}

.no-results p {
  font-size: 1.125rem;
  margin: 0;
}
</style>
