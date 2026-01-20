import { ref } from 'vue'
import { mockMarkets } from '../data/markets'

const markets = ref([])

export function useMarkets() {
  const loadMarkets = () => {
    // 加载mock数据和用户创建的市场
    const userCreated = JSON.parse(localStorage.getItem('userCreatedMarkets') || '[]')
    markets.value = [...mockMarkets, ...userCreated.filter(m => m.status === 'approved')]
  }

  // 初始化加载
  if (markets.value.length === 0) {
    loadMarkets()
  }

  return {
    markets,
    loadMarkets
  }
}
