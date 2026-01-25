<template>
  <div class="markets-page">
    <div class="page-header">
      <h1 class="page-title">{{ isViewingMyMarkets ? 'Personal Markets' : 'Markets' }}</h1>
      <p class="page-subtitle">{{ isViewingMyMarkets ? 'Manage the markets you create and participate in' : 'Trade on the outcomes of real-world events' }}</p>
    </div>

    <!-- 标签筛选区（固定，不滚动） -->
    <div class="tags-section" v-if="!isViewingMyMarkets">
      <button
        v-for="filter in filters"
        :key="filter.key"
        class="filter-btn"
        :class="{ active: selectedFilter === filter.key }"
        @click="handleFilterChange(filter.key)"
      >
        {{ filter.label }}
      </button>
    </div>

    <!-- 返回按钮区（查看我的市场时显示） -->
    <div class="tags-section" v-if="isViewingMyMarkets">
      <button class="filter-btn back-btn" @click="handleBackToAll">
        ← All Markets
      </button>
    </div>

    <!-- 市场卡片滚动区 -->
    <div class="markets-scroll-container" ref="scrollContainer">
      <MarketList
        :markets="markets"
        :isLoading="isLoading"
        :hasMore="hasMore"
        @navigate="navigateTo"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMarkets } from '../composables/useMarkets'
import MarketList from '../components/MarketList.vue'
import { getOptions } from '../utils/api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 使用 markets composable
const {
  markets,
  isLoading,
  hasMore,
  isViewingMyMarkets,
  loadMarkets,
  loadNextPage,
  loadMyMarkets,
  backToAllMarkets
} = useMarkets()

// 滚动容器引用
const scrollContainer = ref(null)

// 节流变量
let scrollTimeout = null
let isLoadingMore = false // 防止重复加载

// 标签相关
const selectedFilter = ref('all')
const availableTags = ref([])

// 动态生成过滤器列表
const filters = computed(() => {
  const baseFilters = [{ key: 'all', label: 'All Markets' }]
  const tagFilters = availableTags.value.map(tag => ({
    key: tag.code,
    label: tag.desc
  }))
  return [...baseFilters, ...tagFilters]
})

// 加载标签选项
const loadTags = async () => {
  try {
    const response = await getOptions('tag')
    if (response && response.data && response.data.tag) {
      availableTags.value = response.data.tag
    }
  } catch (err) {
    console.error('Failed to load tags:', err)
  }
}

// 处理标签变化
const handleFilterChange = (filter) => {
  if (selectedFilter.value === filter) return

  selectedFilter.value = filter

  // 重置滚动位置
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = 0
  }

  // 加载对应标签的市场数据
  loadMarkets({ tag: filter, refresh: true })
}

// 处理返回所有市场
const handleBackToAll = () => {
  selectedFilter.value = 'all'
  backToAllMarkets()
}

// 滚动事件处理
const handleScroll = () => {
  // 使用节流避免频繁触发
  if (scrollTimeout) {
    return
  }

  scrollTimeout = setTimeout(() => {
    scrollTimeout = null

    // 如果正在加载或没有更多数据，直接返回
    if (isLoading.value || !hasMore.value || isLoadingMore) {
      return
    }

    if (!scrollContainer.value) {
      return
    }

    const container = scrollContainer.value
    const scrollTop = container.scrollTop
    const scrollHeight = container.scrollHeight
    const clientHeight = container.clientHeight
    const distanceToBottom = scrollHeight - scrollTop - clientHeight

    // 距离底部 200px 时触发加载
    if (distanceToBottom < 200) {
      console.log('[Markets] 📜 触发加载下一页, distanceToBottom:', distanceToBottom)
      isLoadingMore = true
      loadNextPage()

      // 加载完成后重置标志
      setTimeout(() => {
        isLoadingMore = false
      }, 1000)
    }
  }, 200) // 200ms 节流
}

onMounted(async () => {
  // 加载标签选项
  loadTags()

  // 检查URL参数，如果是filter=my，加载我的市场
  if (route.query.filter === 'my') {
    const userId = userStore.user?.userId
    if (userId) {
      loadMyMarkets({ userId, refresh: true })
    } else {
      // 如果未登录，跳转到首页
      router.push('/markets')
    }
  } else {
    // 初始化加载市场数据
    loadMarkets({ refresh: true })
  }

  // 监听返回所有市场事件
  window.addEventListener('back-to-all-markets', () => {
    backToAllMarkets()
    selectedFilter.value = 'all'
  })

  // 监听加载我的市场事件
  const handleLoadMyMarkets = () => {
    const userId = userStore.user?.userId
    if (!userId) {
      alert('无法获取用户ID，请重新连接钱包')
      return
    }
    loadMyMarkets({ userId, refresh: true })
  }
  window.addEventListener('load-my-markets', handleLoadMyMarkets)

  // 等待 DOM 渲染完成后添加滚动监听
  await nextTick()

  // 多次尝试确保 DOM 已完全渲染
  setTimeout(() => {
    console.log('[Markets] === 布局调试信息 ===')
    console.log('[Markets] scrollContainer.value:', scrollContainer.value)

    if (scrollContainer.value) {
      const rect = scrollContainer.value.getBoundingClientRect()
      console.log('[Markets] 滚动容器 getBoundingClientRect:', {
        top: rect.top,
        left: rect.left,
        width: rect.width,
        height: rect.height
      })
      console.log('[Markets] 滚动容器 scrollHeight:', scrollContainer.value.scrollHeight)
      console.log('[Markets] 滚动容器 clientHeight:', scrollContainer.value.clientHeight)
      console.log('[Markets] 滚动容器 offsetHeight:', scrollContainer.value.offsetHeight)
      console.log('[Markets] 是否显示滚动条:', scrollContainer.value.scrollHeight > scrollContainer.value.clientHeight)
      console.log('[Markets] overflow-y 值:', getComputedStyle(scrollContainer.value).overflowY)

      scrollContainer.value.addEventListener('scroll', handleScroll, { passive: true })
      console.log('[Markets] ✅ 滚动监听已添加')
    } else {
      console.error('[Markets] ❌ scrollContainer.value 为 null')
    }

    console.log('[Markets] 市场数量:', markets.value.length)
    console.log('[Markets] hasMore:', hasMore.value)
    console.log('[Markets] isLoading:', isLoading.value)
    console.log('[Markets] === 调试信息结束 ===')
  }, 500)
})

// 监听路由query参数变化
watch(() => route.query.filter, (newFilter) => {
  if (newFilter === 'my') {
    const userId = userStore.user?.userId
    if (userId) {
      loadMyMarkets({ userId, refresh: true })
    } else {
      router.push('/markets')
    }
  } else {
    backToAllMarkets()
  }
})

onUnmounted(() => {
  if (scrollTimeout) {
    clearTimeout(scrollTimeout)
    scrollTimeout = null
  }

  if (scrollContainer.value) {
    scrollContainer.value.removeEventListener('scroll', handleScroll)
  }
})

const navigateTo = (page, param) => {
  if (page === 'market-detail') {
    router.push({ name: 'market-detail', params: { marketId: param } })
  } else if (page === 'create-market') {
    router.push({ name: 'create-market' })
  } else if (page === 'back-to-all') {
    handleBackToAll()
  }
}
</script>

<style scoped>
.markets-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0rem 2rem;
  width: 100%;
  box-sizing: border-box;
  /* 使用 absolute 定位和固定高度 */
  position: absolute;
  top: 72px; /* Header 高度 */
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 0.75rem;
  flex-shrink: 0;
  padding-top: 1rem; /* 顶部间距 */
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.025em;
  color: var(--text-primary);
}

.page-subtitle {
  font-size: 1rem;
  color: var(--text-secondary);
  font-weight: 400;
  margin: 0;
}

.tags-section {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  flex-shrink: 0; /* 防止标签区被压缩 */
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

.filter-btn.back-btn {
  background: var(--accent-light);
  border-color: var(--accent-light);
  color: white;
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

.markets-scroll-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 10px;
  min-height: 0; /* 重要：允许 flex 子项缩小 */
  /* 确保滚动条始终可见，即使内容不足 */
  display: block; /* 改为 block，避免嵌套 flex 的问题 */
}

/* 自定义滚动条样式 */
.markets-scroll-container::-webkit-scrollbar {
  width: 8px;
}

.markets-scroll-container::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 4px;
}

.markets-scroll-container::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.markets-scroll-container::-webkit-scrollbar-thumb:hover {
  background: var(--border-hover);
}
</style>
