<template>
  <div v-if="market.marketStatus === 3 && !isDeployed" class="open-market-container">
    <!-- 需要支付的流动性 -->
    <div class="liquidity-info">
      <div class="info-row">
        <span class="label">需要质押 (基础流动性)</span>
        <span class="value">{{ market.baseLiquidity }} USDC</span>
      </div>
      <div v-if="balance !== null" class="info-row">
        <span class="label">当前余额</span>
        <span class="value" :class="{ insufficient: parseFloat(balance) < parseFloat(market.baseLiquidity) }">
          {{ balance }} USDC
        </span>
      </div>
    </div>

    <!-- 发布按钮 -->
    <button
      @click="handleOpenMarket"
      class="btn-publish"
      :disabled="loading"
    >
      <svg v-if="!loading" width="18" height="18" viewBox="0 0 18 18" fill="none" class="btn-icon">
        <path d="M9 2L2 7V17H16V7L9 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
        <path d="M9 10V14M9 6V10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <svg v-else width="18" height="18" viewBox="0 0 18 18" fill="none" class="btn-icon spin">
        <circle cx="9" cy="9" r="7" stroke="currentColor" stroke-width="2" stroke-dasharray="4 2"/>
      </svg>
      <span v-if="!loading">发布市场</span>
      <span v-else>发布中...</span>
    </button>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- 成功提示 -->
    <div v-if="success" class="success-message">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <circle cx="10" cy="10" r="8" stroke="#10B981" stroke-width="2"/>
        <path d="M6 10L8 12L14 6" stroke="#10B981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <div class="message-content">
        <div class="title">发布成功!</div>
        <div class="desc">后端正在监听链上事件,约30秒后完成部署</div>
        <div class="tx-info">
          交易: <a :href="getExplorerTxUrl(success.txHash)" target="_blank">{{ shortHash(success.txHash) }}</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { openMarket, isMarketDeployed, getUSDCBalance } from '../utils/openMarket'
import { notifyMarketDeploying, openMarket as openMarketAPI } from '../utils/api'
import { useUserStore } from '../stores/user'
import { useMarkets } from '../composables/useMarkets'

const props = defineProps({
  market: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['deployed'])

const userStore = useUserStore()
const { loadMarkets, loadMyMarkets, isViewingMyMarkets } = useMarkets()

// 状态
const loading = ref(false)
const isDeployed = ref(false)
const contractAddress = ref('')
const balance = ref(null)
const error = ref('')
const success = ref(null)

// 查询 USDC 余额
const checkBalance = async () => {
  try {
    if (!userStore.isConnected) {
      balance.value = null
      return
    }

    const usdcBalance = await getUSDCBalance(userStore.walletAddress)
    balance.value = usdcBalance
  } catch (err) {
    console.error('[OpenMarketButton] 查询余额失败:', err)
    balance.value = null
  }
}

// 监听钱包连接状态,自动更新余额
watch(() => userStore.isConnected, () => {
  checkBalance()
})

// 短地址显示
const shortAddress = (addr) => {
  if (!addr) return ''
  return `${addr.slice(0, 6)}...${addr.slice(-4)}`
}

// 短哈希显示
const shortHash = (hash) => {
  if (!hash) return ''
  return `${hash.slice(0, 10)}...${hash.slice(-8)}`
}

// 获取浏览器URL
const getExplorerUrl = (address) => {
  const chainId = import.meta.env.VITE_CHAIN_ID || '31337'
  if (chainId === '31337') {
    return `https://localhost:8545/address/${address}` // 本地网络
  } else if (chainId === '11155111') {
    return `https://sepolia.etherscan.io/address/${address}` // Sepolia 测试网
  } else {
    return `https://etherscan.io/address/${address}` // 主网
  }
}

// 获取交易浏览器URL
const getExplorerTxUrl = (txHash) => {
  const chainId = import.meta.env.VITE_CHAIN_ID || '31337'
  if (chainId === '31337') {
    return `https://localhost:8545/tx/${txHash}` // 本地网络
  } else if (chainId === '11155111') {
    return `https://sepolia.etherscan.io/tx/${txHash}` // Sepolia 测试网
  } else {
    return `https://etherscan.io/tx/${txHash}` // 主网
  }
}

// 检查市场是否已部署
const checkDeployment = async () => {
  try {
    const onChainMarketId = props.market.onChainMarketId
    if (!onChainMarketId) {
      return
    }

    const deployed = await isMarketDeployed(onChainMarketId)
    isDeployed.value = deployed

    if (deployed) {
      const { getMarketAddress } = await import('../utils/openMarket')
      const address = await getMarketAddress(onChainMarketId)
      contractAddress.value = address
    }
  } catch (err) {
    // 静默失败,不影响主流程
  }
}

// 处理开放市场
const handleOpenMarket = async () => {
  if (loading.value) return

  loading.value = true
  error.value = ''
  success.value = null

  try {
    // 步骤1: 检查钱包连接
    if (!userStore.isConnected) {
      const shouldConnect = confirm('请先连接钱包')
      if (shouldConnect) {
        const success = await userStore.connectWallet()
        if (!success) {
          throw new Error('钱包连接失败')
        }
      } else {
        return
      }
    }

    // 步骤2: 检查 USDC 余额
    const usdcBalance = await getUSDCBalance(userStore.walletAddress)
    const requiredAmount = props.market.baseLiquidity || 0

    if (parseFloat(usdcBalance) < parseFloat(requiredAmount)) {
      throw new Error(`USDC 余额不足! 需要: ${requiredAmount} USDC, 当前: ${usdcBalance} USDC`)
    }

    // 确认对话框
    const confirmMsg = `确认发布市场到区块链?\n\n` +
      `标题: ${props.market.title}\n` +
      `需要质押: ${requiredAmount} USDC\n` +
      `当前余额: ${usdcBalance} USDC\n\n` +
      `点击确认后,将进行以下操作:\n` +
      `1. 后端校验市场信息\n` +
      `2. 授权 USDC 给工厂合约\n` +
      `3. 调用合约创建市场并质押流动性`

    const confirmed = confirm(confirmMsg)
    if (!confirmed) {
      return
    }

    // 步骤3: 调用后端 opening 接口
    const userId = userStore.user?.userId
    if (!userId) {
      throw new Error('无法获取用户ID,请重新登录')
    }

    const openResponse = await openMarketAPI(userId, props.market.id)

    if (!openResponse.success) {
      throw new Error(openResponse.message || '后端校验失败')
    }

    // 步骤4: 调用合约创建市场
    console.log('[OpenMarket] 开始调用合约创建市场...')

    const contractResult = await openMarket(props.market, {
      onTransactionHash: (hash, onChainMarketId) => {
        console.log('[OpenMarket] ✅ 交易已提交, hash:', hash)
        console.log('[OpenMarket] onChainMarketId:', onChainMarketId)
        console.log('[OpenMarket] 准备通知后端 /market/deploying, marketId:', props.market.id)

        // 立即通知后端(发送 txHash、onChainMarketId 和 marketAddress)
        notifyMarketDeploying(props.market.id, {
          txHash: hash,
          onChainMarketId: onChainMarketId,
          marketAddress: contractResult.marketAddress // ✅ 前端解析的地址
        }).then(response => {
          console.log('[OpenMarket] ✅ 后端通知成功:', response)
        }).catch(err => {
          console.error('[OpenMarket] ❌ 后端通知失败:', err)
          console.error('[OpenMarket] 错误详情:', err.response || err.message)
        })
      },
      onReceipt: (receipt) => {
        console.log('[OpenMarket] ✅ 交易已确认, Gas Used:', receipt.gasUsed?.toString())
      }
    })

    if (!contractResult.success) {
      throw new Error(contractResult.error || '合约部署失败')
    }

    // ===== 步骤 5: 成功处理 =====
    success.value = contractResult

    // 触发事件，通知父组件（传递 marketAddress 以便立即更新 UI）
    emit('deployed', {
      marketId: props.market.id,
      txHash: contractResult.txHash,
      onChainMarketId: contractResult.onChainMarketId,
      marketAddress: contractResult.marketAddress // ✅ 立即可用的地址
    })

    // 刷新市场列表以获取最新状态
    setTimeout(() => {
      if (isViewingMyMarkets.value) {
        const userId = userStore.user?.userId
        if (userId) {
          loadMyMarkets({ userId, refresh: true })
        }
      } else {
        loadMarkets({ refresh: true })
      }
    }, 2000)

    // 10秒后自动清除提示
    setTimeout(() => {
      success.value = null
    }, 10000)

  } catch (err) {
    error.value = err.message || '未知错误'

    // 刷新列表以获取最新状态
    setTimeout(() => {
      if (isViewingMyMarkets.value) {
        const userId = userStore.user?.userId
        if (userId) {
          loadMyMarkets({ userId, refresh: true })
        }
      } else {
        loadMarkets({ refresh: true })
      }
    }, 2000)

    setTimeout(() => {
      error.value = ''
    }, 5000)
  } finally {
    loading.value = false
  }
}

// 组件挂载时检查状态
onMounted(async () => {
  await checkDeployment()
  if (!isDeployed.value) {
    await checkBalance()
  }
})
</script>

<style scoped>
.open-market-container {
  margin-top: 1rem;
  padding: 1.25rem;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.liquidity-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1rem;
  background: var(--input-bg);
  border-radius: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-row .label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-row .value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', monospace;
}

.info-row .value.insufficient {
  color: #EF4444;
}

.btn-publish {
  width: 100%;
  padding: 0.875rem 1.5rem;
  background: var(--accent-light);
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
  gap: 0.5rem;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}

.btn-publish:hover:not(:disabled) {
  background: #7C3AED;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);
}

.btn-publish:active:not(:disabled) {
  transform: translateY(0);
}

.btn-publish:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon {
  flex-shrink: 0;
}

.btn-icon.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.error-message {
  padding: 0.875rem 1rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #EF4444;
  border-radius: 8px;
  color: #DC2626;
  font-size: 0.875rem;
  line-height: 1.5;
  animation: slideIn 0.3s ease;
}

.success-message {
  padding: 1rem;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid #10B981;
  border-radius: 8px;
  display: flex;
  gap: 0.75rem;
  animation: slideIn 0.3s ease;
}

.success-message svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.message-content .title {
  font-size: 0.938rem;
  font-weight: 600;
  color: #065F46;
}

.message-content .desc {
  font-size: 0.813rem;
  color: #047857;
  line-height: 1.5;
}

.message-content .tx-info {
  font-size: 0.75rem;
  color: #059669;
}

.message-content .tx-info a {
  color: #059669;
  text-decoration: none;
  font-weight: 500;
  font-family: 'SF Mono', 'Monaco', monospace;
}

.message-content .tx-info a:hover {
  text-decoration: underline;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .open-market-container {
    padding: 1rem;
  }

  .btn-publish {
    font-size: 0.875rem;
    padding: 0.75rem 1.25rem;
  }

  .info-row {
    font-size: 0.813rem;
  }
}
</style>
