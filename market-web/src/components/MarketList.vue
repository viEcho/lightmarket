<template>
  <div class="market-list">
    <div class="markets-grid">
      <MarketCard
        v-for="market in markets"
        :key="market.id"
        :market="market"
        @click="handleMarketClick(market.id)"
      />
    </div>
    <div v-if="isLoading" class="loading-more">
      <div class="spinner"></div>
      <p>Loading more markets...</p>
    </div>
    <div v-else-if="!hasMore && markets.length > 0" class="no-more">
      <p>No more markets to load</p>
    </div>
    <div v-if="markets.length === 0 && !isLoading" class="no-results">
      <p>No markets found</p>
    </div>
  </div>
</template>

<script setup>
import MarketCard from './MarketCard.vue'

const props = defineProps({
  markets: {
    type: Array,
    required: true
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  hasMore: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['navigate'])

const handleMarketClick = (marketId) => {
  emit('navigate', 'market-detail', marketId)
}
</script>

<style scoped>
.market-list {
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.markets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 1.5rem;
}

.loading-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  gap: 1rem;
  color: var(--text-secondary);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-color);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-more p {
  margin: 0;
  font-size: 0.875rem;
}

.no-more {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.no-more p {
  margin: 0;
  font-size: 0.875rem;
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
