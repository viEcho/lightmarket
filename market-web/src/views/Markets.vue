<template>
  <div class="markets-page">
    <div class="page-header">
      <h1 class="page-title">Prediction Markets</h1>
      <p class="page-subtitle">Trade on the outcomes of real-world events</p>
    </div>
    <MarketList :markets="markets" @navigate="navigateTo" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMarkets } from '../composables/useMarkets'
import MarketList from '../components/MarketList.vue'

const router = useRouter()
const { markets } = useMarkets()

onMounted(() => {
  // 组件挂载时加载数据
})

const navigateTo = (page, param) => {
  if (page === 'market-detail') {
    router.push({ name: 'market-detail', params: { marketId: param } })
  } else if (page === 'create-market') {
    router.push({ name: 'create-market' })
  }
}
</script>

<style scoped>
.markets-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 3rem 2rem;
  width: 100%;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 3rem;
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
  margin: 0;
}
</style>
