<template>
  <Teleport to="body">
    <div v-if="show" class="modal-overlay" @click="handleClose">
      <div class="modal-container" @click.stop>
        <!-- 关闭按钮 -->
        <button class="close-btn" @click="handleClose">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>

        <!-- 标题 -->
        <div class="modal-header">
          <h3 class="modal-title">交易确认</h3>
          <p class="modal-subtitle">{{ market.question }}</p>
        </div>

        <!-- 未登录提示 -->
        <div v-if="!isConnected" class="wallet-warning">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none" class="warning-icon">
            <circle cx="24" cy="24" r="20" stroke="#F59E0B" stroke-width="2" fill="none"/>
            <path d="M24 16V26M24 32V34" stroke="#F59E0B" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="warning-content">
            <p class="warning-title">请先连接钱包</p>
            <p class="warning-desc">您需要连接钱包才能进行交易</p>
          </div>
          <button @click="handleConnectWallet" class="btn-connect-wallet">
            连接钱包
          </button>
        </div>

        <!-- 已登录：交易表单 -->
        <div v-else class="trade-form">
          <!-- 交易方向 -->
          <div class="trade-direction" :class="`direction-${tradeType}`">
            <svg v-if="tradeType === 'yes'" width="32" height="32" viewBox="0 0 32 32" fill="none">
              <circle cx="16" cy="16" r="14" fill="currentColor"/>
              <path d="M10 16L14 20L22 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else width="32" height="32" viewBox="0 0 32 32" fill="none">
              <circle cx="16" cy="16" r="14" fill="currentColor"/>
              <path d="M10 12L16 20L22 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="direction-text">
              购买 {{ tradeType.toUpperCase() }}
            </span>
          </div>

          <!-- 价格信息 -->
          <div class="price-info">
            <div class="info-row">
              <span class="info-label">当前价格</span>
              <span class="info-value">{{ Math.round(currentPrice * 100) }}¢ / {{ tradeType.toUpperCase() }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">购买数量</span>
              <span class="info-value">1 {{ tradeType.toUpperCase() }} = 1 USDC</span>
            </div>
          </div>

          <!-- 输入框 -->
          <div class="input-section">
            <label class="input-label">购买数量 ({{ tradeType.toUpperCase() }})</label>
            <div class="input-group">
              <input
                v-model="amount"
                type="number"
                class="amount-input"
                placeholder="输入数量"
                min="0"
                step="1"
                @input="calculateCost"
              />
              <span class="input-suffix">{{ tradeType.toUpperCase() }}</span>
            </div>
          </div>

          <!-- 费用说明 -->
          <div class="cost-section">
            <div class="cost-row">
              <span class="cost-label">购买数量</span>
              <span class="cost-value">{{ amount || 0 }} {{ tradeType.toUpperCase() }}</span>
            </div>
            <div class="cost-row total">
              <span class="cost-label">需要支付</span>
              <span class="cost-value highlight">{{ cost || 0 }} USDC</span>
            </div>
          </div>

          <!-- 余额提示 -->
          <div class="balance-info">
            <span class="balance-label">当前余额</span>
            <span class="balance-value" :class="{ insufficient: isInsufficient }">
              {{ balance || '--' }} USDC
            </span>
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <!-- 确认按钮 -->
          <button
            @click="handleConfirm"
            class="btn-confirm"
            :disabled="!amount || loading || isInsufficient"
          >
            <svg v-if="!loading" width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M9 2L2 7V17H16V7L9 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M9 10V14M9 6V10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span v-if="!loading">确认交易</span>
            <span v-else>交易中...</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { buyYes, sellYes } from '../utils/trade'
import { getUSDCBalance } from '../utils/openMarket'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  market: {
    type: Object,
    required: true
  },
  tradeType: {
    type: String, // 'yes' or 'no'
    required: true
  }
})

const emit = defineEmits(['close', 'success'])

const userStore = useUserStore()

// 状态
const amount = ref('')
const balance = ref('') // 改为空字符串，表示未查询
const loading = ref(false)
const error = ref('')
const isBalanceQueried = ref(false) // 标记是否已查询过余额

// 计算属性
const isConnected = computed(() => userStore.isConnected)

const currentPrice = computed(() => {
  return props.tradeType === 'yes' ? props.market.yesPrice : props.market.noPrice
})

const cost = computed(() => {
  return amount.value ? parseFloat(amount.value).toFixed(2) : '0.00'
})

const isInsufficient = computed(() => {
  if (!balance.value || !amount.value) return false
  const balanceNum = parseFloat(balance.value)
  const amountNum = parseFloat(amount.value)
  // 如果余额是 NaN 或 0，不显示为不足（因为还未查询）
  if (isNaN(balanceNum)) return false
  return balanceNum < amountNum
})

// 监听弹窗打开
watch(() => props.show, async (newVal) => {
  if (newVal && isConnected.value) {
    // 重置状态
    amount.value = ''
    error.value = ''
    loading.value = false
    balance.value = ''
    isBalanceQueried.value = false
    // 弹窗打开时不查询余额，改为点击确认交易时查询
  }
})

// 计算费用
const calculateCost = () => {
  // 1:1 比例，数量即为费用
  // 这里只是为了触发响应式更新
}

// 连接钱包
const handleConnectWallet = async () => {
  const success = await userStore.connectWallet()
  if (success) {
    // 连接成功后不立即查询余额，等待点击确认交易时查询
  }
}

// 关闭弹窗
const handleClose = () => {
  emit('close')
}

// 确认交易
const handleConfirm = async () => {
  if (!amount.value || parseFloat(amount.value) <= 0) {
    error.value = '请输入有效的购买数量'
    return
  }

  loading.value = true
  error.value = ''

  try {
    // 实时查询链上USDC余额
    const usdcBalance = await getUSDCBalance(userStore.walletAddress)
    balance.value = usdcBalance
    isBalanceQueried.value = true
    console.log('[TradeModal] 当前USDC余额:', balance.value)

    // 检查余额是否足够
    if (parseFloat(balance.value) < parseFloat(amount.value)) {
      error.value = `USDC 余额不足！需要 ${amount.value} USDC，当前 ${balance.value} USDC`
      return
    }

    // 根据交易类型调用不同函数
    let result
    if (props.tradeType === 'yes') {
      result = await buyYes(props.market.marketAddress, amount.value)
    } else {
      // 买入 NO = 卖出 YES
      result = await sellYes(props.market.marketAddress, amount.value)
    }

    if (result.success) {
      // 交易成功
      emit('success', {
        type: props.tradeType,
        amount: amount.value,
        cost: cost.value,
        txHash: result.txHash
      })

      // 关闭弹窗
      handleClose()
    } else {
      error.value = result.error || '交易失败'
    }
  } catch (err) {
    console.error('[TradeModal] 交易失败:', err)
    error.value = err.message || '交易失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-container {
  background: var(--bg-primary);
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.modal-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid var(--border-color);
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.modal-subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 未登录提示 */
.wallet-warning {
  padding: 40px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
}

.warning-icon {
  opacity: 0.8;
}

.warning-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.warning-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.warning-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
}

.btn-connect-wallet {
  padding: 0.75rem 2rem;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.938rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
}

.btn-connect-wallet:hover {
  background: #7C3AED;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.35);
}

/* 交易表单 */
.trade-form {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.trade-direction {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1.125rem;
}

.direction-yes {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(5, 150, 105, 0.1) 100%);
  color: #10B981;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.direction-no {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(220, 38, 38, 0.1) 100%);
  color: #EF4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.direction-text {
  flex: 1;
}

.price-info {
  background: var(--bg-secondary);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', monospace;
}

.input-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.amount-input {
  width: 100%;
  padding: 0.875rem 80px 0.875rem 1rem;
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: 10px;
  font-size: 1rem;
  color: var(--text-primary);
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', monospace;
  transition: all 0.2s ease;
}

.amount-input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.input-suffix {
  position: absolute;
  right: 16px;
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 600;
  pointer-events: none;
}

.cost-section {
  background: var(--bg-secondary);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cost-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cost-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.cost-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', monospace;
}

.cost-row.total {
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.cost-value.highlight {
  font-size: 1rem;
  color: var(--accent);
}

.balance-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.balance-label {
  font-size: 0.813rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.balance-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', monospace;
}

.balance-value.insufficient {
  color: #EF4444;
}

.error-message {
  padding: 12px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #EF4444;
  border-radius: 8px;
  color: #DC2626;
  font-size: 0.813rem;
  line-height: 1.5;
}

.btn-confirm {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-confirm:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 响应式 */
@media (max-width: 640px) {
  .modal-container {
    max-width: 100%;
  }

  .trade-form {
    padding: 20px;
  }
}
</style>
