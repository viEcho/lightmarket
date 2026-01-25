<template>
  <div class="price-chart-container">
    <div class="chart-header">
      <div class="chart-tabs">
        <button
          :class="['tab-btn', { active: chartType === 'line' }]"
          @click="switchChartType('line')"
        >
          Line
        </button>
        <button
          :class="['tab-btn', { active: chartType === 'area' }]"
          @click="switchChartType('area')"
        >
          Area
        </button>
        <button
          :class="['tab-btn', { active: chartType === 'bar' }]"
          @click="switchChartType('bar')"
        >
          Bar
        </button>
      </div>
      <div class="time-filters">
        <button
          v-for="period in timePeriods"
          :key="period.value"
          :class="['period-btn', { active: selectedPeriod === period.value }]"
          @click="changeTimePeriod(period.value)"
        >
          {{ period.label }}
        </button>
      </div>
    </div>

    <div class="chart-wrapper">
      <div ref="chartContainer" class="chart-container"></div>
    </div>

    <div class="chart-info">
      <div class="info-item">
        <span class="label">Current Price</span>
        <span class="value current-price">{{ (currentPrice * 100).toFixed(1) }}¢</span>
      </div>
      <div class="info-item">
        <span class="label">24h Change</span>
        <span :class="['value', priceChange >= 0 ? 'positive' : 'negative']">
          {{ priceChange >= 0 ? '+' : '' }}{{ priceChange.toFixed(2) }}%
        </span>
      </div>
      <div class="info-item">
        <span class="label">24h High</span>
        <span class="value">{{ (highPrice * 100).toFixed(1) }}¢</span>
      </div>
      <div class="info-item">
        <span class="label">24h Low</span>
        <span class="value">{{ (lowPrice * 100).toFixed(1) }}¢</span>
      </div>
      <div class="info-item">
        <span class="label">24h Volume</span>
        <span class="value">{{ formatNumber(volume) }} IMKT</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  marketId: {
    type: [String, Number],
    required: true
  }
})

const chartContainer = ref(null)
const chartInstance = ref(null)
const isChartInitialized = ref(false)

const chartType = ref('area')
const selectedPeriod = ref('1D')
const currentPrice = ref(0.65)

const timePeriods = [
  { label: '30m', value: '30m' },
  { label: '1H', value: '1H' },
  { label: '4H', value: '4H' },
  { label: '1D', value: '1D' },
  { label: '1W', value: '1W' },
  { label: '1M', value: '1M' },
  { label: 'All', value: 'All' }
]

// 时间周期配置（毫秒）
const periodInterval = {
  '30m': 30 * 60 * 1000,      // 30分钟
  '1H': 60 * 60 * 1000,        // 1小时
  '4H': 4 * 60 * 60 * 1000,    // 4小时
  '1D': 24 * 60 * 60 * 1000,   // 1天
  '1W': 7 * 24 * 60 * 60 * 1000, // 1周
  '1M': 30 * 24 * 60 * 60 * 1000, // 1月（30天）
  'All': 30 * 24 * 60 * 60 * 1000 // All（按月）
}

// 每个周期显示的数据点数量
const periodDataCount = {
  '30m': 48,    // 24小时，每30分钟一个点
  '1H': 48,     // 48小时
  '4H': 42,     // 7天
  '1D': 30,     // 30天
  '1W': 12,     // 12周
  '1M': 3,      // 3个月
  'All': 12     // 12个月（一年）
}

// 根据时间间隔格式化日期标签
const formatDateLabel = (timestamp, interval) => {
  const date = new Date(timestamp)

  // 30分钟和1小时：显示时间（HH:MM）
  if (interval < 4 * 60 * 60 * 1000) {
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    })
  }
  // 4小时：显示日期+时间
  else if (interval < 24 * 60 * 60 * 1000) {
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      hour12: false
    })
  }
  // 1天：显示日期
  else if (interval < 7 * 24 * 60 * 60 * 1000) {
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric'
    })
  }
  // 1周：显示月/日
  else if (interval < 30 * 24 * 60 * 60 * 1000) {
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric'
    })
  }
  // 1月、3月、All：显示月份
  else {
    return date.toLocaleDateString('en-US', {
      month: 'short',
      year: '2-digit'
    })
  }
}

// 生成模拟价格数据（使用固定的种子确保所有市场显示相同数据）
const generatePriceData = (basePrice, count, interval = 3600000) => {
  const data = []
  let price = basePrice
  const now = Date.now()

  // 使用固定的种子生成可重复的数据
  let seed = 12345

  for (let i = count; i >= 0; i--) {
    // 简单的伪随机数生成器
    seed = (seed * 1103515245 + 12345) & 0x7fffffff
    const randomValue = (seed % 1000) / 1000 // 0-1之间的随机数

    const change = (randomValue - 0.5) * 0.02
    price = Math.max(0.01, Math.min(0.99, price + change))
    const timestamp = now - i * interval
    data.push({
      timestamp: timestamp,
      date: formatDateLabel(timestamp, interval),
      price: Number(price.toFixed(3)),
      volume: Math.floor(randomValue * 100000) + 10000
    })
  }

  return data
}

// 初始化价格数据
const priceData = ref([])

// 初始化价格数据的函数
const initializePriceData = () => {
  priceData.value = generatePriceData(0.65, periodDataCount['1D'], periodInterval['1D'])
  console.log('[PriceChart] 初始化价格数据，数据点数量:', priceData.value.length)
  console.log('[PriceChart] 当前价格:', priceData.value[priceData.value.length - 1]?.price)
}

const priceChange = computed(() => {
  if (priceData.value.length < 2) return 0
  const firstPrice = priceData.value[0].price
  const lastPrice = priceData.value[priceData.value.length - 1].price
  return ((lastPrice - firstPrice) / firstPrice) * 100
})

const highPrice = computed(() => {
  return Math.max(...priceData.value.map(d => d.price))
})

const lowPrice = computed(() => {
  return Math.min(...priceData.value.map(d => d.price))
})

const volume = computed(() => {
  return priceData.value.reduce((sum, d) => sum + d.volume, 0)
})

const formatNumber = (num) => {
  if (!num) return '0'
  return num.toLocaleString('en-US', { maximumFractionDigits: 0 })
}

const getChartOption = () => {
  const dates = priceData.value.map(d => d.date)
  const prices = priceData.value.map(d => d.price)

  const baseOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'var(--border-color)',
      textStyle: {
        color: 'var(--text-primary)'
      },
      formatter: (params) => {
        const data = params[0]
        return `
          <div style="padding: 8px;">
            <div style="margin-bottom: 4px; font-weight: 600;">${data.name}</div>
            <div>Price: ${(data.data * 100).toFixed(1)}¢</div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '3%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: {
        lineStyle: { color: 'var(--border-color)' }
      },
      axisLabel: {
        color: 'var(--text-secondary)',
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      scale: true,
      min: (value) => Math.max(0, value - 0.1),
      axisLine: {
        lineStyle: { color: 'var(--border-color)' }
      },
      axisLabel: {
        color: 'var(--text-secondary)',
        formatter: (value) => (value * 100).toFixed(0) + '¢'
      },
      splitLine: {
        lineStyle: { color: 'var(--border-color)', opacity: 0.3 }
      }
    }
  }

  if (chartType.value === 'line') {
    return {
      ...baseOption,
      series: [
        {
          name: 'Price',
          type: 'line',
          smooth: true,
          data: prices,
          lineStyle: {
            color: '#8B5CF6',
            width: 2
          },
          showSymbol: false,
          areaStyle: undefined
        }
      ]
    }
  } else if (chartType.value === 'area') {
    return {
      ...baseOption,
      series: [
        {
          name: 'Price',
          type: 'line',
          smooth: true,
          data: prices,
          lineStyle: {
            color: '#8B5CF6',
            width: 2
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(139, 92, 246, 0.4)' },
                { offset: 1, color: 'rgba(139, 92, 246, 0)' }
              ]
            }
          },
          showSymbol: false
        }
      ]
    }
  } else if (chartType.value === 'bar') {
    return {
      ...baseOption,
      series: [
        {
          name: 'Price',
          type: 'bar',
          data: prices,
          itemStyle: {
            color: (params) => {
              const price = params.data
              const prevPrice = prices[params.dataIndex - 1] || price
              return price >= prevPrice ? '#22C55E' : '#EF4444'
            }
          }
        }
      ]
    }
  }
}

const initChart = () => {
  if (!chartContainer.value) {
    console.error('[PriceChart] chartContainer 为 null，无法初始化图表')
    return
  }

  try {
    // 如果已有实例，先销毁
    if (chartInstance.value) {
      console.log('[PriceChart] 销毁旧的 ECharts 实例')
      chartInstance.value.dispose()
      chartInstance.value = null
    }

    console.log('[PriceChart] 开始初始化 ECharts 实例...')

    // 检查数据
    if (!priceData.value || priceData.value.length === 0) {
      console.error('[PriceChart] 价格数据为空，无法初始化图表')
      return
    }
    console.log('[PriceChart] 价格数据点数量:', priceData.value.length)

    // 初始化 ECharts
    chartInstance.value = echarts.init(chartContainer.value)
    console.log('[PriceChart] ECharts 实例已创建')

    // 生成配置
    const option = getChartOption()
    console.log('[PriceChart] 图表配置已生成, series数量:', option.series?.length)

    // 设置配置
    chartInstance.value.setOption(option, true) // true 表示不合并，完全重置
    console.log('[PriceChart] 图表已渲染')

    isChartInitialized.value = true
  } catch (error) {
    console.error('[PriceChart] 初始化图表失败:', error)
    console.error('[PriceChart] 错误堆栈:', error.stack)

    // 清理失败的实例
    if (chartInstance.value) {
      try {
        chartInstance.value.dispose()
      } catch (e) {
        // ignore
      }
      chartInstance.value = null
    }
  }
}

const updateChartData = () => {
  if (!chartInstance.value) {
    return
  }

  try {
    chartInstance.value.setOption(getChartOption(), true)
    currentPrice.value = priceData.value[priceData.value.length - 1].price
  } catch (error) {
    // Error handling
  }
}

const switchChartType = (type) => {
  chartType.value = type
  updateChartData()
}

const changeTimePeriod = (period) => {
  selectedPeriod.value = period
  const interval = periodInterval[period]
  const count = periodDataCount[period]
  priceData.value = generatePriceData(0.65, count, interval)
  updateChartData()
}

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

// 模拟实时价格更新
let updateInterval = null

const startRealtimeUpdates = () => {
  updateInterval = setInterval(() => {
    const lastPrice = priceData.value[priceData.value.length - 1].price
    const change = (Math.random() - 0.5) * 0.01
    const newPrice = Math.max(0.01, Math.min(0.99, lastPrice + change))
    const interval = periodInterval[selectedPeriod.value]

    priceData.value.push({
      timestamp: Date.now(),
      date: formatDateLabel(Date.now(), interval),
      price: Number(newPrice.toFixed(3)),
      volume: Math.floor(Math.random() * 50000) + 5000
    })

    // 保持数据长度
    const maxCount = periodDataCount[selectedPeriod.value]
    if (priceData.value.length > maxCount) {
      priceData.value.shift()
    }

    // 更新图表
    updateChartData()
  }, 3000)
}

onMounted(() => {
  console.log('[PriceChart] 组件已挂载, marketId:', props.marketId)

  // 初始化价格数据
  initializePriceData()

  // 使用 nextTick 确保 DOM 已渲染，然后多次尝试初始化
  nextTick(() => {
    const tryInitChart = (attempts = 0) => {
      console.log(`[PriceChart] 尝试初始化图表 (第 ${attempts + 1} 次)...`)
      console.log('[PriceChart] chartContainer.value:', chartContainer.value)

      if (chartContainer.value) {
        // 检查容器是否有尺寸
        const rect = chartContainer.value.getBoundingClientRect()
        console.log('[PriceChart] 容器尺寸:', { width: rect.width, height: rect.height })

        if (rect.width > 0 && rect.height > 0) {
          initChart()
          startRealtimeUpdates()
          window.addEventListener('resize', handleResize)
          console.log('[PriceChart] ✅ 图表初始化成功')
        } else {
          console.warn('[PriceChart] ⚠️ 容器尺寸为 0，延迟初始化')
          if (attempts < 5) {
            setTimeout(() => tryInitChart(attempts + 1), 200)
          } else {
            console.error('[PriceChart] ❌ 多次尝试后仍无法初始化图表')
          }
        }
      } else {
        console.error('[PriceChart] ❌ chartContainer.value 为 null')
        if (attempts < 5) {
          setTimeout(() => tryInitChart(attempts + 1), 200)
        }
      }
    }

    // 首次尝试延迟执行，确保父组件布局完成
    setTimeout(() => tryInitChart(0), 300)
  })
})

onUnmounted(() => {
  if (updateInterval) {
    clearInterval(updateInterval)
  }
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})
</script>

<style scoped>
.price-chart-container {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.chart-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: var(--input-bg);
  color: var(--text-primary);
}

.tab-btn.active {
  background: var(--accent-light);
  color: white;
  border-color: var(--accent-light);
}

.time-filters {
  display: flex;
  gap: 4px;
}

.period-btn {
  padding: 6px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.period-btn:hover {
  background: var(--input-bg);
  color: var(--text-primary);
}

.period-btn.active {
  background: var(--input-bg);
  color: var(--accent-light);
  font-weight: 600;
}

.chart-wrapper {
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 350px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  min-height: 350px;
}

.chart-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: var(--text-secondary);
}

.info-item .value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.info-item .value.current-price {
  color: var(--accent-light);
  font-size: 24px;
}

.info-item .value.positive {
  color: #22C55E;
}

.info-item .value.negative {
  color: #EF4444;
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: stretch;
  }

  .time-filters {
    justify-content: space-between;
  }

  .chart-container {
    height: 250px;
  }

  .chart-info {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
