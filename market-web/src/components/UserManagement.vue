<template>
  <div class="user-management">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <p class="subtitle">查询用户并 Mock USDC</p>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <!-- 网络状态提示 -->
      <div class="network-status" :class="{ connected: isCorrectNetwork, disconnected: !isCorrectNetwork }">
        <span class="status-icon">{{ isCorrectNetwork ? '✅' : '⚠️' }}</span>
        <span class="status-text">
          {{ networkStatusText }}
        </span>
        <button
          v-if="!isConnected && !isConnecting"
          @click="connectWallet"
          class="btn btn-connect"
        >
          连接钱包
        </button>
        <button
          v-if="isConnected && !isCorrectNetwork"
          @click="switchNetwork"
          class="btn btn-switch"
        >
          切换网络
        </button>
      </div>

      <div class="search-box">
        <input
          v-model="searchAddress"
          type="text"
          placeholder="输入用户钱包地址 (0x...)"
          @keyup.enter="handleSearch"
        />
        <button
          @click="handleSearch"
          :disabled="loading || !searchAddress"
          class="btn btn-search"
        >
          {{ loading ? '搜索中...' : '🔍 搜索' }}
        </button>
      </div>
      <div class="search-tips">
        <p>💡 输入完整的钱包地址进行搜索（例如: 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266）</p>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0" class="results-section">
      <div class="results-header">
        <h3>搜索结果 ({{ searchResults.length }})</h3>
        <button @click="clearSearch" class="btn btn-clear">清除</button>
      </div>

      <div class="user-list">
        <div
          v-for="(user, index) in searchResults"
          :key="index"
          class="user-card"
        >
          <div class="user-info">
            <div class="user-avatar">
              {{ user.address.slice(0, 2).toUpperCase() }}
            </div>
            <div class="user-details">
              <div class="user-address">{{ formatAddress(user.address) }}</div>
              <div class="user-full">{{ user.address }}</div>
              <div class="user-balance">
                USDC 余额: {{ user.balance }} USDC
              </div>
            </div>
          </div>
          <div class="user-actions">
            <button
              @click="openMockDialog(user)"
              class="btn btn-mock"
            >
              🪙 Mock USDC
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 无结果提示 -->
    <div v-else-if="searched && searchResults.length === 0" class="no-results">
      <div class="no-results-icon">🔍</div>
      <p>未找到该地址的用户</p>
      <p class="hint">请检查地址是否正确</p>
    </div>

    <!-- Mock USDC 弹窗 -->
    <div
      v-if="showMockDialog"
      class="modal-overlay"
      @click.self="closeMockDialog"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h3>🪙 Mock USDC</h3>
          <button @click="closeMockDialog" class="btn-close">✕</button>
        </div>

        <div class="modal-body">
          <!-- 用户信息 -->
          <div class="target-user">
            <p><strong>目标地址:</strong></p>
            <p class="address">{{ selectedUser?.address }}</p>
          </div>

          <!-- 当前余额 -->
          <div class="current-balance">
            <p><strong>当前余额:</strong> {{ selectedUser?.balance }} USDC</p>
          </div>

          <!-- 快速选项 -->
          <div class="quick-amounts">
            <p><strong>快速选择:</strong></p>
            <div class="quick-buttons">
              <button
                v-for="option in quickOptions"
                :key="option.amount"
                @click="mockAmount = option.amount"
                :class="['btn-quick', { active: mockAmount === option.amount }]"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <!-- 自定义数量 -->
          <div class="custom-amount">
            <label>或输入自定义数量:</label>
            <input
              v-model.number="mockAmount"
              type="number"
              placeholder="输入 USDC 数量"
              min="1"
              step="1"
            />
            <span class="unit">USDC</span>
          </div>

          <!-- 预计余额 -->
          <div v-if="mockAmount > 0" class="preview-balance">
            <p><strong>预计余额:</strong></p>
            <p class="new-balance">
              {{ parseFloat(selectedUser?.balance || 0) + mockAmount }} USDC
            </p>
          </div>
        </div>

        <div class="modal-footer">
          <button
            @click="handleMock"
            :disabled="mocking || !mockAmount || mockAmount <= 0"
            class="btn btn-confirm"
          >
            {{ mocking ? '铸造中...' : '✅ 确认铸造' }}
          </button>
          <button @click="closeMockDialog" class="btn btn-cancel">
            取消
          </button>
        </div>
      </div>
    </div>

    <!-- 交易状态提示 -->
    <div
      v-if="transactionStatus"
      class="transaction-toast"
      :class="transactionStatus.type"
    >
      <div class="toast-icon">
        {{ transactionStatus.type === 'success' ? '✅' : transactionStatus.type === 'error' ? '❌' : '⏳' }}
      </div>
      <div class="toast-message">
        {{ transactionStatus.message }}
      </div>
      <button
        v-if="transactionStatus.type !== 'pending'"
        @click="transactionStatus = null"
        class="toast-close"
      >
        ✕
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { mintTestUSDC, getUSDCBalance } from '@/utils/faucet'
import { getWithAuth, post } from '@/utils/api'

// 状态
const searchAddress = ref('')
const searchResults = ref([])
const loading = ref(false)
const searched = ref(false)

// 钱包连接状态
const isConnected = ref(false)
const isConnecting = ref(false)
const currentChainId = ref(null)
const walletAddress = ref('')

// 计算属性
const isCorrectNetwork = computed(() => {
  return currentChainId.value === 31337 || currentChainId.value === 31337
})

const networkStatusText = computed(() => {
  if (!isConnected.value) {
    return '未连接钱包'
  }
  if (!isCorrectNetwork.value) {
    return `网络错误 (当前: Chain ID ${currentChainId.value}，需要: 31337)`
  }
  return `✅ 已连接 Hardhat Local (${walletAddress.value.slice(0, 6)}...${walletAddress.value.slice(-4)})`
})

// 连接钱包
const connectWallet = async () => {
  if (!window.ethereum) {
    showStatus('error', '请安装 MetaMask 钱包')
    return
  }

  isConnecting.value = true
  try {
    const accounts = await window.ethereum.request({
      method: 'eth_requestAccounts'
    })

    if (accounts.length > 0) {
      walletAddress.value = accounts[0]
      isConnected.value = true

      // 获取网络 ID
      const { ethers } = await import('ethers')
      const provider = new ethers.BrowserProvider(window.ethereum)
      const network = await provider.getNetwork()
      currentChainId.value = Number(network.chainId)

      if (currentChainId.value === 31337) {
        showStatus('success', '钱包已连接到 Hardhat Local')
      } else {
        showStatus('error', `请切换到 Hardhat Local 网络 (当前: ${currentChainId.value})`)
      }
    }
  } catch (error) {
    console.error('连接钱包失败:', error)
    showStatus('error', `连接失败: ${error.message}`)
  } finally {
    isConnecting.value = false
  }
}

// 切换网络
const switchNetwork = async () => {
  try {
    // MetaMask 已经有 localhostHadhat 网络，直接切换
    // Chain ID 31337 = 0xc35
    await window.ethereum.request({
      method: 'wallet_switchEthereumChain',
      params: [{ chainId: '0xc35' }]
    })

    showStatus('success', '已切换到 Hardhat Local 网络')
  } catch (error) {
    console.error('切换网络失败:', error)

    // 如果切换失败，提示用户手动切换
    if (error.code === 4902 || error.code === -32603) {
      showStatus('error', '请在 MetaMask 中手动选择 "localhostHadhat" 网络')
    } else {
      showStatus('error', `切换失败: ${error.message}`)
    }
  }
}

// 监听账户和网络变化
onMounted(async () => {
  if (window.ethereum) {
    // 检查是否已连接
    const accounts = await window.ethereum.request({ method: 'eth_accounts' })
    if (accounts.length > 0) {
      walletAddress.value = accounts[0]
      isConnected.value = true

      // 获取当前网络 ID
      const { ethers } = await import('ethers')
      const provider = new ethers.BrowserProvider(window.ethereum)
      const network = await provider.getNetwork()
      currentChainId.value = Number(network.chainId)
    }

    // 监听账户变化
    window.ethereum.on('accountsChanged', (accounts) => {
      if (accounts.length > 0) {
        walletAddress.value = accounts[0]
        isConnected.value = true
      } else {
        isConnected.value = false
        walletAddress.value = ''
      }
    })

    // 监听网络变化
    window.ethereum.on('chainChanged', async (chainId) => {
      currentChainId.value = parseInt(chainId, 16)
    })
  }
})

// Mock 弹窗
const showMockDialog = ref(false)
const selectedUser = ref(null)
const mockAmount = ref('')
const mocking = ref(false)

// 交易状态
const transactionStatus = ref(null)

// 快速选项
const quickOptions = [
  { amount: 100, label: '100' },
  { amount: 1000, label: '1,000' },
  { amount: 10000, label: '10,000' },
  { amount: 100000, label: '100,000' }
]

// 搜索用户
const handleSearch = async () => {
  if (!searchAddress.value) {
    showStatus('error', '请输入钱包地址')
    return
  }

  // 验证地址格式
  if (!isValidAddress(searchAddress.value)) {
    showStatus('error', '请输入有效的钱包地址')
    return
  }

  loading.value = true
  searched.value = false

  try {
    // 调用后端 API 查询钱包信息
    const response = await fetch('http://localhost:9999/api/admin/wallet/query', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}` // 如果需要 token
      },
      body: JSON.stringify({
        walletAddress: searchAddress.value
      })
    })

    const data = await response.json()

    if (data.success && data.code === 1000) {
      const walletData = data.data

      // 查询链上余额
      let balance = '0'
      try {
        balance = await getUSDCBalance(walletData.walletAddress)
      } catch (balanceError) {
        console.error('查询余额失败:', balanceError)
      }

      // 构造用户信息
      const user = {
        address: walletData.walletAddress,
        balance: balance,
        userId: walletData.userId,
        nickname: walletData.nickname,
        avatar: walletData.avatar,
        chainId: walletData.chainId,
        walletType: walletData.walletType,
        isPrimary: walletData.isPrimary,
        createdTime: walletData.createdTime
      }

      searchResults.value = [user]
      searched.value = true

      showStatus('success', `找到用户，余额: ${balance} USDC`)
    } else {
      showStatus('error', data.message || '查询失败')
      searchResults.value = []
    }
  } catch (error) {
    console.error('搜索失败:', error)
    showStatus('error', `查询失败: ${error.message}`)
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

// 清除搜索
const clearSearch = () => {
  searchAddress.value = ''
  searchResults.value = []
  searched.value = false
}

// 打开 Mock 弹窗
const openMockDialog = (user) => {
  selectedUser.value = user
  mockAmount.value = ''
  showMockDialog.value = true
}

// 关闭 Mock 弹窗
const closeMockDialog = () => {
  showMockDialog.value = false
  selectedUser.value = null
  mockAmount.value = ''
}

// 执行 Mock
const handleMock = async () => {
  if (!selectedUser.value) return
  if (!mockAmount.value || mockAmount.value <= 0) {
    showStatus('error', '请输入有效的数量')
    return
  }

  mocking.value = true

  try {
    const { ethers } = await import('ethers')
    const CONFIG = {
      USDC_ADDRESS: import.meta.env.VITE_USDC_ADDRESS || ''
    }

    if (!window.ethereum) {
      throw new Error('请安装 MetaMask 钱包')
    }

    const provider = new ethers.BrowserProvider(window.ethereum)

    // 检查网络
    const network = await provider.getNetwork()
    const currentChainId = Number(network.chainId)
    const targetChainId = 31337

    if (currentChainId !== targetChainId) {
      showStatus('error', `网络错误：当前 Chain ID ${currentChainId}，需要 Chain ID ${targetChainId}`)
      mocking.value = false
      return
    }

    // 检查 USDC 地址配置
    if (!CONFIG.USDC_ADDRESS) {
      throw new Error('USDC 合约地址未配置，请检查 .env 文件')
    }

    showStatus('pending', `正在铸造 ${mockAmount.value} USDC...`)

    const signer = await provider.getSigner()

    const MOCK_USDC_ABI = [
      "function mint(address to, uint256 amount) external"
    ]

    const usdcContract = new ethers.Contract(
      CONFIG.USDC_ADDRESS,
      MOCK_USDC_ABI,
      signer
    )

    // 铸造到指定地址
    const amountWei = ethers.parseUnits(mockAmount.value.toString(), 6)

    try {
      const tx = await usdcContract.mint(selectedUser.value.address, amountWei)
      console.log('[UserManagement] 交易已提交:', tx.hash)

      showStatus('pending', `交易已提交: ${tx.hash.slice(0, 10)}...`)

      // 等待交易确认，设置超时
      const receipt = await Promise.race([
        tx.wait(),
        new Promise((_, reject) =>
          setTimeout(() => reject(new Error('交易超时 (30秒)')), 30000)
        )
      ])

      console.log('[UserManagement] 交易已确认, Gas Used:', receipt?.gasUsed?.toString())

      // 重新查询链上余额
      const newBalance = await getUSDCBalance(selectedUser.value.address)

      // 更新用户余额
      selectedUser.value.balance = newBalance

      // 更新搜索结果
      const userIndex = searchResults.value.findIndex(
        u => u.address === selectedUser.value.address
      )
      if (userIndex !== -1) {
        searchResults.value[userIndex].balance = newBalance
      }

      showStatus('success', `✅ 成功铸造 ${mockAmount.value} USDC! 新余额: ${newBalance} USDC`)
    } catch (txError) {
      console.error('Transaction error:', txError)
      throw new Error(`交易失败: ${txError.message}`)
    }

    closeMockDialog()
  } catch (error) {
    console.error('Mock 失败:', error)
    showStatus('error', `铸造失败: ${error.message}`)
  } finally {
    mocking.value = false
  }
}

// 显示状态
const showStatus = (type, message) => {
  transactionStatus.value = { type, message }

  // 3秒后自动关闭成功/错误提示
  if (type !== 'pending') {
    setTimeout(() => {
      if (transactionStatus.value?.type === type) {
        transactionStatus.value = null
      }
    }, 3000)
  }
}

// 验证地址格式
const isValidAddress = (address) => {
  return /^0x[a-fA-F0-9]{40}$/.test(address)
}

// 格式化地址显示
const formatAddress = (address) => {
  if (!address) return ''
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}
</script>

<style scoped>
.user-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 5px 0;
  color: #333;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 搜索区域 */
.search-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* 网络状态 */
.network-status {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 15px;
  font-size: 14px;
}

.network-status.connected {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.network-status.disconnected {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}

.status-icon {
  font-size: 18px;
}

.status-text {
  flex: 1;
  font-weight: 500;
}

.btn-connect,
.btn-switch {
  padding: 6px 12px;
  font-size: 12px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-connect:hover,
.btn-switch:hover {
  background: #2980b9;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-box input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: 'Courier New', monospace;
}

.search-box input:focus {
  outline: none;
  border-color: #3498db;
}

.search-tips {
  margin-top: 10px;
}

.search-tips p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
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

.btn-search {
  background: #3498db;
  color: white;
}

.btn-search:hover:not(:disabled) {
  background: #2980b9;
}

.btn-clear {
  background: #95a5a6;
  color: white;
  padding: 6px 12px;
  font-size: 12px;
}

.btn-clear:hover {
  background: #7f8c8d;
}

.btn-mock {
  background: #f39c12;
  color: white;
}

.btn-mock:hover {
  background: #e67e22;
}

/* 搜索结果 */
.results-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.results-header h3 {
  margin: 0;
  color: #333;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
  transition: all 0.3s;
}

.user-card:hover {
  background: #f0f0f0;
  border-color: #3498db;
}

.user-info {
  display: flex;
  gap: 15px;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.user-details {
  flex: 1;
}

.user-address {
  font-weight: bold;
  font-size: 16px;
  color: #333;
  margin-bottom: 5px;
  font-family: 'Courier New', monospace;
}

.user-full {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
  font-family: 'Courier New', monospace;
}

.user-balance {
  font-size: 14px;
  color: #27ae60;
  font-weight: 500;
}

.user-actions {
  display: flex;
  gap: 10px;
}

/* 无结果 */
.no-results {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
}

.no-results-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.no-results p {
  margin: 10px 0;
  color: #666;
}

.no-results .hint {
  font-size: 14px;
  color: #999;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.btn-close:hover {
  background: #f0f0f0;
}

.modal-body {
  padding: 20px;
}

.target-user,
.current-balance {
  margin-bottom: 20px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 6px;
}

.target-user p,
.current-balance p {
  margin: 5px 0;
}

.address {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

.quick-amounts {
  margin-bottom: 20px;
}

.quick-amounts p {
  margin: 0 0 10px 0;
  color: #333;
}

.quick-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.btn-quick {
  padding: 10px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-quick:hover {
  border-color: #3498db;
  background: #f0f0f0;
}

.btn-quick.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.custom-amount {
  margin-bottom: 20px;
}

.custom-amount label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.custom-amount {
  display: flex;
  gap: 10px;
  align-items: center;
}

.custom-amount input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.unit {
  color: #666;
  font-weight: 500;
}

.preview-balance {
  padding: 15px;
  background: #e8f5e9;
  border-radius: 6px;
  border-left: 4px solid #4caf50;
}

.preview-balance p {
  margin: 5px 0;
}

.new-balance {
  font-size: 18px;
  font-weight: bold;
  color: #4caf50;
}

.modal-footer {
  display: flex;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.btn-confirm {
  flex: 1;
  background: #27ae60;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: #229954;
}

.btn-cancel {
  padding: 10px 20px;
  background: #95a5a6;
  color: white;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

/* 交易提示 */
.transaction-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  min-width: 300px;
  padding: 15px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 2000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.transaction-toast.success {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.transaction-toast.error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.transaction-toast.pending {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}

.toast-icon {
  font-size: 24px;
}

.toast-message {
  flex: 1;
  font-weight: 500;
}

.toast-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: inherit;
  padding: 0;
  width: 24px;
  height: 24px;
}
</style>
