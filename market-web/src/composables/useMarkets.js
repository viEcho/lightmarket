import { ref } from 'vue'
import { getMarketList, getMyMarkets } from '../utils/api'
import { getMarketStatusLogical } from '../constants/marketStatus'

const markets = ref([])
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)
const isLoading = ref(false)
const hasMore = ref(true)
const currentTag = ref(null)
const currentUserId = ref(null) // 记录当前加载的用户市场ID
const isViewingMyMarkets = ref(false) // 标识当前是否在查看"我的市场"

export function useMarkets() {
  /**
   * 根据 id 查找市场
   * @param {string|number} marketId - 市场ID
   * @returns {Object|null} 找到的市场对象，未找到返回 null
   */
  const findMarketById = (marketId) => {
    return markets.value.find(m => m.id == marketId) || null
  }

  /**
   * 加载市场列表（分页）
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码，如果不传则加载下一页
   * @param {number} params.tag - 标签代码（用于内部逻辑）
   * @param {boolean} params.refresh - 是否刷新（重新加载第一页）
   */
  const loadMarkets = async (params = {}) => {
    if (isLoading.value) {
      return
    }
    if (!params.refresh && !hasMore.value) {
      return
    }

    try {
      isLoading.value = true

      // 如果是刷新，重置为第一页
      if (params.refresh) {
        currentPage.value = 1
        markets.value = []
        hasMore.value = true
      }

      // 如果指定了页码，使用指定页码
      if (params.page) {
        currentPage.value = params.page
      }

      // 如果指定了标签，更新当前标签
      if (params.tag !== undefined) {
        currentTag.value = params.tag
      }

      const requestParams = {
        num: currentPage.value,
        size: pageSize.value
      }

      // 添加标签筛选（如果有）
      if (currentTag.value !== null && currentTag.value !== 'all') {
        requestParams.tagCode = String(currentTag.value)
      }

      // 加载普通市场时清除用户ID标记
      currentUserId.value = null
      isViewingMyMarkets.value = false

      const response = await getMarketList(requestParams)

      if (response && response.data) {
        const { list, total: totalCount, nextPage } = response.data

        // 转换后端字段为前端格式
        const formattedMarkets = list.map(market => ({
          id: market.marketId || market.id,
          title: market.title,
          question: market.title,
          description: market.description,
          category: market.category,
          categoryDesc: market.categoryDesc,
          closeTime: market.closeTime,
          endTime: market.closeTime,
          endDate: formatEndDate(market.closeTime),
          yesPrice: market.yesPrice ? parseFloat(market.yesPrice) : 0.5,
          noPrice: market.noPrice ? parseFloat(market.noPrice) : 0.5,
          currentProbability: market.yesPrice ? parseFloat(market.yesPrice) : 0.5,
          volume: market.totalVolume ? parseFloat(market.totalVolume) : 0,
          liquidity: market.baseLiquidity ? parseFloat(market.baseLiquidity) : 0,
          liquidityProviders: 0,
          status: getMarketStatusLogical(market.marketStatus),
          marketStatus: market.marketStatus,
          creator: market.creator || 'Unknown',
          createTime: market.createdTime ? new Date(market.createdTime).getTime() : Date.now(),
          resolutionSource: market.oracleSource,
          tags: market.tags || [],
          aiModels: market.aiModels || [],
          oracleSource: market.oracleSource,
          baseLiquidity: market.baseLiquidity,
          marketAddress: market.marketAddress
        }))

        // 如果是刷新，直接替换；否则追加
        if (params.refresh || currentPage.value === 1) {
          markets.value = formattedMarkets
        } else {
          markets.value = [...markets.value, ...formattedMarkets]
        }

        // 更新总数和是否还有更多数据
        total.value = totalCount
        hasMore.value = nextPage
      }
    } catch (err) {
      // 降级：加载本地 mock 数据
      if (currentPage.value === 1) {
        loadMockMarkets()
      }
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 加载 mock 数据（降级方案）
   */
  const loadMockMarkets = () => {
    import('../data/markets').then(({ mockMarkets }) => {
      const userCreated = JSON.parse(localStorage.getItem('userCreatedMarkets') || '[]')
      markets.value = [...mockMarkets, ...userCreated.filter(m => m.status === 'approved')]
      hasMore.value = false
    })
  }

  /**
   * 格式化结束日期
   */
  const formatEndDate = (dateString) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toISOString().split('T')[0]
  }

  /**
   * 加载下一页
   */
  const loadNextPage = () => {
    if (!isLoading.value && hasMore.value) {
      currentPage.value++

      // 如果当前正在加载用户市场，继续加载用户市场的下一页
      if (currentUserId.value) {
        loadMyMarkets({
          userId: currentUserId.value,
          page: currentPage.value
        })
      } else {
        loadMarkets({ page: currentPage.value })
      }
    }
  }

  /**
   * 刷新数据（重新加载第一页）
   */
  const refresh = () => {
    // 清除当前用户ID标记和"我的市场"状态
    currentUserId.value = null
    isViewingMyMarkets.value = false
    loadMarkets({ refresh: true })
  }

  /**
   * 返回所有市场视图
   */
  const backToAllMarkets = () => {
    currentUserId.value = null
    isViewingMyMarkets.value = false
    currentTag.value = null
    loadMarkets({ refresh: true })
  }

  /**
   * 加载用户参与的市场列表
   * @param {Object} params - 查询参数
   * @param {number} params.userId - 用户ID（必需）
   * @param {number} params.page - 页码，如果不传则加载下一页
   * @param {boolean} params.refresh - 是否刷新（重新加载第一页）
   */
  const loadMyMarkets = async (params = {}) => {
    if (isLoading.value) {
      return
    }
    if (!params.userId) {
      console.error('[useMarkets] userId is required for loadMyMarkets')
      return
    }
    if (!params.refresh && !hasMore.value) {
      return
    }

    try {
      isLoading.value = true

      // 记录当前加载的用户ID
      currentUserId.value = params.userId
      isViewingMyMarkets.value = true

      // 如果是刷新，重置为第一页
      if (params.refresh) {
        currentPage.value = 1
        markets.value = []
        hasMore.value = true
      }

      // 如果指定了页码，使用指定页码
      if (params.page) {
        currentPage.value = params.page
      }

      const requestParams = {
        userId: params.userId,
        num: currentPage.value,
        size: pageSize.value
      }

      const response = await getMyMarkets(requestParams)

      if (response && response.data) {
        const { list, total: totalCount, nextPage } = response.data

        // 转换后端字段为前端格式
        const formattedMarkets = list.map(market => ({
          id: market.marketId || market.id,
          title: market.title,
          question: market.title,
          description: market.description,
          category: market.category,
          categoryDesc: market.categoryDesc,
          closeTime: market.closeTime,
          endTime: market.closeTime,
          endDate: formatEndDate(market.closeTime),
          yesPrice: market.yesPrice ? parseFloat(market.yesPrice) : 0.5,
          noPrice: market.noPrice ? parseFloat(market.noPrice) : 0.5,
          currentProbability: market.yesPrice ? parseFloat(market.yesPrice) : 0.5,
          volume: market.totalVolume ? parseFloat(market.totalVolume) : 0,
          liquidity: market.baseLiquidity ? parseFloat(market.baseLiquidity) : 0,
          liquidityProviders: 0,
          status: getMarketStatusLogical(market.marketStatus),
          marketStatus: market.marketStatus,
          creator: market.creator || 'Unknown',
          createTime: market.createdTime ? new Date(market.createdTime).getTime() : Date.now(),
          resolutionSource: market.oracleSource,
          tags: market.tags || [],
          aiModels: market.aiModels || [],
          oracleSource: market.oracleSource,
          baseLiquidity: market.baseLiquidity,
          marketAddress: market.marketAddress
        }))

        // 如果是刷新，直接替换；否则追加
        if (params.refresh || currentPage.value === 1) {
          markets.value = formattedMarkets
        } else {
          markets.value = [...markets.value, ...formattedMarkets]
        }

        // 更新总数和是否还有更多数据
        total.value = totalCount
        hasMore.value = nextPage
      }
    } catch (err) {
      console.error('[useMarkets] Failed to load my markets:', err)
    } finally {
      isLoading.value = false
    }
  }

  return {
    markets,
    currentPage,
    pageSize,
    total,
    isLoading,
    hasMore,
    isViewingMyMarkets, // 导出状态
    loadMarkets,
    loadNextPage,
    refresh,
    backToAllMarkets, // 导出方法
    loadMyMarkets,
    findMarketById
  }
}
