<template>
  <div class="trade-panel">
    <div class="trade-header">
      <h3>交易面板</h3>
      <div class="price-info">
        <span class="price">YES 价格: {{ poolInfo?.yesPrice }}¢</span>
        <span class="price">NO 价格: {{ poolInfo?.noPrice }}¢</span>
      </div>
    </div>

    <!-- 买入 YES -->
    <div class="trade-section">
      <h4>买入 YES</h4>
      <div class="input-group">
        <label>支付 USDC 数量:</label>
        <input
          v-model.number="buyYesAmount"
          type="number"
          placeholder="输入 USDC 数量"
          min="0"
          step="0.01"
        />
      </div>
      <button
        @click="handleBuyYes"
        :disabled="loading || !buyYesAmount"
        class="btn btn-buy"
      >
        {{ loading ? '交易中...' : '买入 YES' }}
      </button>
    </div>

    <!-- 卖出 YES (买入 NO) -->
    <div class="trade-section">
      <h4>卖出 YES (买入 NO)</h4>
      <div class="input-group">
        <label>卖出 YES 数量:</label>
        <input
          v-model.number="sellYesAmount"
          type="number"
          placeholder="输入 YES 数量"
          min="0"
          step="0.01"
        />
      </div>
      <button
        @click="handleSellYes"
        :disabled="loading || !sellYesAmount"
        class="btn btn-sell"
      >
        {{ loading ? '交易中...' : '卖出 YES' }}
      </button>
    </div>

    <!-- 我的持仓 -->
    <div class="position-section">
      <h4>我的持仓</h4>
      <div v-if="position" class="position-info">
        <p>YES: {{ position.yesAmount }}</p>
        <p>NO: {{ position.noAmount }}</p>
        <p>锁定: {{ position.lockedAmount }}</p>
        <p>可提取: {{ position.withdrawable }}</p>
      </div>
      <div v-else>
        <p>请先连接钱包查看持仓</p>
      </div>
      <button @click="refreshPosition" class="btn btn-refresh">
        刷新持仓
      </button>
    </div>

    <!-- 交易历史 -->
    <div class="history-section">
      <h4>交易历史</h4>
      <div v-if="tradeHistory.length === 0" class="no-history">
        暂无交易记录
      </div>
      <div v-else class="history-list">
        <div
          v-for="(trade, index) in tradeHistory"
          :key="index"
          class="history-item"
          :class="trade.action === 'buyYes' ? 'buy' : 'sell'"
        >
          <div class="trade-action">
            {{ trade.action === 'buyYes' ? '买入' : '卖出' }} YES
          </div>
          <div class="trade-details">
            <span v-if="trade.action === 'buyYes'">
              支付: {{ trade.usdcSpent }} USDC, 获得: {{ trade.yesReceived }} YES
            </span>
            <span v-else>
              卖出: {{ trade.yesSold }} YES, 获得: {{ trade.usdcReceived }} USDC
            </span>
          </div>
          <div class="trade-price">
            价格: {{ trade.price }}¢
          </div>
          <div class="trade-hash">
            <a :href="getTxHashUrl(trade.txHash)" target="_blank">
              查看交易
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- 事件监听开关 -->
    <div class="event-listener-section">
      <label>
        <input type="checkbox" v-model="eventListenerEnabled" @change="toggleEventListener" />
        启用事件监听（实时更新）
      </label>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { buyYes, sellYes, getUserPosition, getPoolInfo, listenMarketEvents } from '@/utils/trade'

// Props
const props = defineProps({
  marketAddress: {
    type: String,
    required: true
  }
})

// 状态
const loading = ref(false)
const buyYesAmount = ref('')
const sellYesAmount = ref('')
const poolInfo = ref(null)
const position = ref(null)
const tradeHistory = ref([])
const eventListenerEnabled = ref(false)
let stopEventListener = null

// 初始化
onMounted(async () => {
  await loadPoolInfo()
  await refreshPosition()
})

onUnmounted(() => {
  if (stopEventListener) {
    stopEventListener()
  }
})

// 加载池子信息
const loadPoolInfo = async () => {
  try {
    const info = await getPoolInfo(props.marketAddress)
    poolInfo.value = info
  } catch (error) {
    console.error('加载池子信息失败:', error)
  }
}

// 刷新持仓
const refreshPosition = async () => {
  try {
    const pos = await getUserPosition(props.marketAddress)
    position.value = pos
  } catch (error) {
    console.error('刷新持仓失败:', error)
  }
}

// 买入 YES
const handleBuyYes = async () => {
  if (!buyYesAmount.value || buyYesAmount.value <= 0) {
    alert('请输入有效的 USDC 数量')
    return
  }

  loading.value = true

  try {
    const result = await buyYes(props.marketAddress, buyYesAmount.value, {
      onTransactionHash: (hash) => {
        console.log('交易已提交:', hash)
      },
      onReceipt: (receipt) => {
        console.log('交易已确认:', receipt)
      },
      onEvent: (eventData) => {
        console.log('事件触发:', eventData)

        // 添加到交易历史
        tradeHistory.value.unshift({
          action: 'buyYes',
          ...eventData,
          timestamp: new Date().toISOString()
        })

        // 刷新持仓和池子信息
        refreshPosition()
        loadPoolInfo()
      }
    })

    if (result.success) {
      alert(`✅ 买入成功! 获得 ${result.eventData.yesReceived} YES`)
      buyYesAmount.value = ''
    } else {
      alert(`❌ 交易失败: ${result.error}`)
    }
  } catch (error) {
    console.error('买入失败:', error)
    alert(`❌ 交易失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 卖出 YES
const handleSellYes = async () => {
  if (!sellYesAmount.value || sellYesAmount.value <= 0) {
    alert('请输入有效的 YES 数量')
    return
  }

  loading.value = true

  try {
    const result = await sellYes(props.marketAddress, sellYesAmount.value, {
      onTransactionHash: (hash) => {
        console.log('交易已提交:', hash)
      },
      onReceipt: (receipt) => {
        console.log('交易已确认:', receipt)
      },
      onEvent: (eventData) => {
        console.log('事件触发:', eventData)

        // 添加到交易历史
        tradeHistory.value.unshift({
          action: 'sellYes',
          ...eventData,
          timestamp: new Date().toISOString()
        })

        // 刷新持仓和池子信息
        refreshPosition()
        loadPoolInfo()
      }
    })

    if (result.success) {
      alert(`✅ 卖出成功! 获得 ${result.eventData.usdcReceived} USDC`)
      sellYesAmount.value = ''
    } else {
      alert(`❌ 交易失败: ${result.error}`)
    }
  } catch (error) {
    console.error('卖出失败:', error)
    alert(`❌ 交易失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 切换事件监听
const toggleEventListener = () => {
  if (eventListenerEnabled.value) {
    // 启用监听
    stopEventListener = listenMarketEvents(props.marketAddress, {
      onBoughtYes: (eventData) => {
        console.log('📢 监听到 BoughtYes 事件:', eventData)

        // 添加到交易历史
        tradeHistory.value.unshift({
          action: 'buyYes',
          ...eventData,
          timestamp: new Date().toISOString(),
          isListener: true
        })

        // 刷新持仓和池子信息
        refreshPosition()
        loadPoolInfo()
      },
      onSoldYes: (eventData) => {
        console.log('📢 监听到 SoldYes 事件:', eventData)

        // 添加到交易历史
        tradeHistory.value.unshift({
          action: 'sellYes',
          ...eventData,
          timestamp: new Date().toISOString(),
          isListener: true
        })

        // 刷新持仓和池子信息
        refreshPosition()
        loadPoolInfo()
      }
    })

    console.log('✅ 事件监听已启用')
  } else {
    // 停止监听
    if (stopEventListener) {
      stopEventListener()
      stopEventListener = null
    }
    console.log('⏹️ 事件监听已停止')
  }
}

// 获取交易浏览器链接
const getTxHashUrl = (txHash) => {
  // 根据网络返回不同的浏览器链接
  const chainId = import.meta.env.VITE_CHAIN_ID || '31337'
  if (chainId === '1') {
    return `https://etherscan.io/tx/${txHash}`
  } else if (chainId === '31337') {
    return `#` // 本地网络没有浏览器
  }
  return `#`
}
</script>

<style scoped>
.trade-panel {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
}

.trade-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.price-info {
  display: flex;
  gap: 20px;
}

.price {
  font-weight: bold;
  color: #333;
}

.trade-section {
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 6px;
}

.input-group {
  margin-bottom: 10px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.input-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-buy {
  background: #4caf50;
  color: white;
}

.btn-buy:hover:not(:disabled) {
  background: #45a049;
}

.btn-sell {
  background: #f44336;
  color: white;
}

.btn-sell:hover:not(:disabled) {
  background: #da190b;
}

.btn-refresh {
  background: #2196f3;
  color: white;
  margin-top: 10px;
}

.position-section {
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 6px;
}

.position-info p {
  margin: 5px 0;
}

.history-section {
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 6px;
}

.no-history {
  color: #999;
  text-align: center;
  padding: 20px;
}

.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
  border-left: 4px solid #999;
}

.history-item.buy {
  background: #e8f5e9;
  border-left-color: #4caf50;
}

.history-item.sell {
  background: #ffebee;
  border-left-color: #f44336;
}

.trade-action {
  font-weight: bold;
  margin-bottom: 5px;
}

.trade-details {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.trade-price {
  font-size: 14px;
  color: #333;
}

.trade-hash a {
  font-size: 12px;
  color: #2196f3;
  text-decoration: none;
}

.trade-hash a:hover {
  text-decoration: underline;
}

.event-listener-section {
  padding: 15px;
  background: white;
  border-radius: 6px;
}

.event-listener-section label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
</style>
