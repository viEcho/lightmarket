<template>
  <div class="test-faucet">
    <div class="faucet-header">
      <h3>🪙 测试 USDC 水龙头</h3>
      <p class="warning">⚠️ 仅用于测试网络！</p>
    </div>

    <!-- 余额显示 -->
    <div class="balance-section">
      <div class="balance-card">
        <span class="label">我的 USDC 余额:</span>
        <span class="balance">{{ loadingBalance ? '查询中...' : balance }} USDC</span>
      </div>
      <button @click="refreshBalance" class="btn btn-refresh" :disabled="loadingBalance">
        🔄 刷新余额
      </button>
    </div>

    <!-- 快速选项 -->
    <div class="quick-options">
      <h4>快速铸造:</h4>
      <div class="option-buttons">
        <button
          v-for="option in quickOptions"
          :key="option.amount"
          @click="handleMint(option.amount)"
          :disabled="loading"
          class="btn btn-quick"
        >
          {{ option.label }}
        </button>
      </div>
    </div>

    <!-- 自定义数量 -->
    <div class="custom-amount">
      <h4>自定义数量:</h4>
      <div class="input-group">
        <input
          v-model.number="customAmount"
          type="number"
          placeholder="输入 USDC 数量"
          min="1"
          step="1"
        />
        <button
          @click="handleMint(customAmount)"
          :disabled="loading || !customAmount"
          class="btn btn-mint"
        >
          {{ loading ? '铸造中...' : '铸造 USDC' }}
        </button>
      </div>
    </div>

    <!-- 交易状态 -->
    <div v-if="transactionStatus" class="transaction-status" :class="transactionStatus.type">
      <div class="status-icon">{{ transactionStatus.type === 'success' ? '✅' : '❌' }}</div>
      <div class="status-message">{{ transactionStatus.message }}</div>
      <div v-if="transactionStatus.txHash" class="tx-hash">
        <a :href="getTxHashUrl(transactionStatus.txHash)" target="_blank">
          查看交易 →
        </a>
      </div>
    </div>

    <!-- 使用说明 -->
    <div class="instructions">
      <h4>📖 使用说明</h4>
      <ol>
        <li>确保已连接 MetaMask 钱包</li>
        <li>确保钱包已连接到测试网络（Hardhat Local / Sepolia）</li>
        <li>选择或输入要铸造的 USDC 数量</li>
        <li>点击"铸造 USDC"并确认交易</li>
        <li>等待交易确认后，USDC 将转入你的钱包</li>
      </ol>

      <div class="network-info">
        <p><strong>配置的网络:</strong></p>
        <p>Chain ID: {{ chainId }}</p>
        <p v-if="usdcAddress">USDC 合约: {{ usdcAddress }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { mintTestUSDC, getUSDCBalance } from '@/utils/faucet'

// 状态
const loading = ref(false)
const loadingBalance = ref(false)
const balance = ref('0')
const customAmount = ref('')
const transactionStatus = ref(null)

// 配置
const chainId = import.meta.env.VITE_CHAIN_ID || '31337'
const usdcAddress = import.meta.env.VITE_USDC_ADDRESS || ''

// 快速选项
const quickOptions = [
  { amount: 100, label: '100 USDC' },
  { amount: 1000, label: '1,000 USDC' },
  { amount: 10000, label: '10,000 USDC' },
  { amount: 100000, label: '100,000 USDC' }
]

// 初始化
onMounted(async () => {
  await refreshBalance()
})

// 刷新余额
const refreshBalance = async () => {
  loadingBalance.value = true
  try {
    const bal = await getUSDCBalance()
    balance.value = bal
    transactionStatus.value = null
  } catch (error) {
    console.error('查询余额失败:', error)
    showStatus('error', `查询余额失败: ${error.message}`)
  } finally {
    loadingBalance.value = false
  }
}

// 铸造 USDC
const handleMint = async (amount) => {
  if (!amount || amount <= 0) {
    showStatus('error', '请输入有效的数量')
    return
  }

  loading.value = true
  transactionStatus.value = null

  try {
    console.log('[Faucet] 开始铸造:', amount, 'USDC')

    const result = await mintTestUSDC(amount, {
      onTransactionHash: (hash) => {
        showStatus('pending', `⏳ 交易已提交: ${hash.slice(0, 10)}...`)
      },
      onReceipt: (receipt) => {
        console.log('[Faucet] 交易已确认')
      }
    })

    if (result.success) {
      balance.value = result.balanceAfter
      showStatus('success', `🎉 成功铸造 ${result.minted} USDC!`, result.txHash)
      customAmount.value = ''
    } else {
      showStatus('error', `❌ 铸造失败: ${result.error}`)
    }
  } catch (error) {
    console.error('铸造失败:', error)
    showStatus('error', `❌ 铸造失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 显示状态
const showStatus = (type, message, txHash = null) => {
  transactionStatus.value = { type, message, txHash }
}

// 获取交易浏览器链接
const getTxHashUrl = (txHash) => {
  if (chainId === '1') {
    return `https://etherscan.io/tx/${txHash}`
  } else if (chainId === '5') {
    return `https://goerli.etherscan.io/tx/${txHash}`
  } else if (chainId === '11155111') {
    return `https://sepolia.etherscan.io/tx/${txHash}`
  } else if (chainId === '31337') {
    return `#` // 本地网络
  }
  return `#`
}
</script>

<style scoped>
.test-faucet {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  border: 2px solid #ffd700;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff 100%);
}

.faucet-header {
  text-align: center;
  margin-bottom: 20px;
}

.faucet-header h3 {
  margin: 0 0 10px 0;
  color: #f39c12;
}

.warning {
  color: #e74c3c;
  font-size: 14px;
  margin: 0;
}

.balance-section {
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-card {
  flex: 1;
}

.balance-card .label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.balance {
  font-size: 24px;
  font-weight: bold;
  color: #27ae60;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-refresh {
  background: #3498db;
  color: white;
}

.btn-refresh:hover:not(:disabled) {
  background: #2980b9;
}

.quick-options {
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.quick-options h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.option-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.btn-quick {
  background: #f39c12;
  color: white;
  padding: 12px;
}

.btn-quick:hover:not(:disabled) {
  background: #e67e22;
  transform: translateY(-2px);
}

.custom-amount {
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.custom-amount h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.input-group {
  display: flex;
  gap: 10px;
}

.input-group input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.btn-mint {
  background: #27ae60;
  color: white;
  padding: 10px 24px;
}

.btn-mint:hover:not(:disabled) {
  background: #229954;
}

.transaction-status {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.transaction-status.success {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.transaction-status.error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.transaction-status.pending {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}

.status-icon {
  font-size: 24px;
}

.status-message {
  flex: 1;
  font-weight: 500;
}

.tx-hash a {
  color: #3498db;
  text-decoration: none;
  font-size: 14px;
}

.tx-hash a:hover {
  text-decoration: underline;
}

.instructions {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  font-size: 14px;
}

.instructions h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.instructions ol {
  margin: 0;
  padding-left: 20px;
}

.instructions li {
  margin-bottom: 5px;
  color: #555;
}

.network-info {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #dee2e6;
  font-size: 12px;
  color: #666;
}

.network-info p {
  margin: 5px 0;
}
</style>
