<template>
  <header class="header">
    <div class="header-container">
      <div class="logo">
        <a href="/markets" @click.prevent="handleLogoClick" class="logo-link">
          <h1>LightMarket</h1>
        </a>
      </div>
      <nav class="nav">
        <a
          href="/markets"
          @click.prevent="handleMarketsClick"
          class="nav-link"
          :class="{ active: $route.path === '/markets' || $route.path === '/' }"
        >
          Markets
        </a>
        <router-link
          to="/leaderboard"
          class="nav-link"
          :class="{ active: $route.path === '/leaderboard' }"
        >
          Leaderboard
        </router-link>
        <router-link
          to="/activity"
          class="nav-link"
          :class="{ active: $route.path === '/activity' }"
        >
          Activity
        </router-link>
      </nav>
      <div class="header-actions">
        <!-- Make market Button (Visible when connected) -->
        <button
          v-if="isConnected"
          class="btn-secondary"
          @click="$router.push('/create-market')"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 2V14M2 8H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          Make market
        </button>
        <!-- My Markets Button (Visible when connected) -->
        <button
          v-if="isConnected"
          class="btn-secondary"
          @click="handleMyMarkets"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="2" y="2" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
            <path d="M5 8H11M8 5V11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          My markets
        </button>

        <!-- Wallet Connection Button -->
        <button
          v-if="!isConnected"
          class="btn-connect"
          @click="handleConnect"
          :disabled="isLoading"
        >
          <span v-if="isLoading">Connecting...</span>
          <span v-else>Connect Wallet</span>
        </button>

        <!-- Connected Wallet Display -->
        <div v-else class="user-section">
          <button class="avatar-button" @click="toggleUserMenu">
            <div class="user-avatar">
              <span class="avatar-text">{{ getAvatarText() }}</span>
            </div>
          </button>

          <!-- User Menu Popup -->
          <div v-if="showUserMenu" class="user-menu-popup" @click.stop>
            <div class="user-menu-header">
              <div class="user-menu-avatar">
                <span class="avatar-text">{{ getAvatarText() }}</span>
              </div>
              <div v-if="user?.nickname" class="user-nickname">{{ user.nickname }}</div>
            </div>
            <div class="user-menu-content">
              <div class="user-info-item">
                <span class="user-info-label">钱包地址</span>
                <span class="user-info-value user-address-full">{{ walletAddress }}</span>
              </div>
              <div class="user-info-item">
                <span class="user-info-label">余额</span>
                <div class="balance-wrapper">
                  <span class="user-info-value">{{ parseFloat(balance).toFixed(6) }} ETH</span>
                  <button class="refresh-btn" @click="handleRefreshBalance" title="刷新余额">
                    <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
                      <path d="M8 2V6M8 6L6 4M8 6L10 4M14 8C14 11.3137 11.3137 14 8 14C4.68629 14 2 11.3137 2 8C2 4.68629 4.68629 2 8 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>
              <div class="user-info-item">
                <span class="user-info-label">网络</span>
                <span class="user-info-value">{{ getNetworkName(chainId) }}</span>
              </div>
            </div>
            <div class="user-menu-footer">
              <button class="add-wallet-btn" @click="openAddWalletModal">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M8 2V14M2 8H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                添加钱包
              </button>
              <button class="logout-btn" @click="handleDisconnect">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M6 3L2 8L6 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M14 8H2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>

  <!-- 添加钱包弹窗 -->
  <div
    v-if="showAddWalletModal"
    class="add-wallet-modal-overlay"
    @click.self="closeAddWalletModal"
  >
    <div class="add-wallet-modal">
      <!-- 步骤 1: 连接新钱包 -->
      <div v-if="addWalletStep === 1" class="modal-step">
        <div class="modal-header">
          <h3>添加新钱包</h3>
          <button @click="closeAddWalletModal" class="btn-close">✕</button>
        </div>
        <div class="modal-body">
          <p class="step-description">连接要添加的新钱包地址</p>
          <div v-if="addWalletError" class="error-message">
            {{ addWalletError }}
          </div>
          <button
            @click="connectNewWallet"
            :disabled="isAddingWallet"
            class="connect-wallet-btn"
          >
            <span v-if="isAddingWallet">连接中...</span>
            <span v-else>连接新钱包</span>
          </button>
        </div>
      </div>

      <!-- 步骤 2: 签名验证 -->
      <div v-if="addWalletStep === 2" class="modal-step">
        <div class="modal-header">
          <h3>验证钱包所有权</h3>
          <button @click="closeAddWalletModal" class="btn-close">✕</button>
        </div>
        <div class="modal-body">
          <div class="wallet-info">
            <p><strong>钱包地址:</strong></p>
            <p class="address">{{ newWalletAddress }}</p>
          </div>
          <p class="step-description">请签名以验证您是该钱包的所有者</p>
          <div v-if="addWalletError" class="error-message">
            {{ addWalletError }}
          </div>
          <button
            @click="signAndAddWallet"
            :disabled="isAddingWallet"
            class="sign-btn"
          >
            <span v-if="isAddingWallet">签名中...</span>
            <span v-else>签名并添加</span>
          </button>
        </div>
      </div>

      <!-- 步骤 3: 完成 -->
      <div v-if="addWalletStep === 3" class="modal-step">
        <div class="modal-header">
          <h3>钱包添加成功</h3>
          <button @click="closeAddWalletModal" class="btn-close">✕</button>
        </div>
        <div class="modal-body success-body">
          <div class="success-icon">✅</div>
          <p>钱包已成功添加到您的账号</p>
          <p class="success-address">{{ newWalletAddress }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { addWallet, getNonce } from '@/utils/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showUserMenu = ref(false)

// 添加钱包弹窗状态
const showAddWalletModal = ref(false)
const addWalletStep = ref(1) // 1: 选择钱包, 2: 签名验证, 3: 完成
const newWalletAddress = ref('')
const isAddingWallet = ref(false)
const addWalletError = ref('')
const nonceData = ref(null)

// 使用 storeToRefs 保持响应性
const { isConnected, walletAddress, shortAddress, balance, chainId, isLoading, user } = storeToRefs(userStore)

// 获取用户头像显示的文字（昵称首字母或钱包地址前两位）
const getAvatarText = () => {
  if (user.value?.nickname) {
    return user.value.nickname.charAt(0).toUpperCase()
  }
  if (walletAddress.value) {
    return walletAddress.value.slice(2, 4).toUpperCase()
  }
  return '?'
}

const handleConnect = async () => {
  await userStore.connectWallet()
}

const handleDisconnect = async () => {
  await userStore.disconnectWallet()
  showUserMenu.value = false
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleRefreshBalance = async () => {
  await userStore.updateUserBalance()
}

const handleMyMarkets = async () => {
  // 跳转到 markets 页面，并带上 filter=my 参数
  if (route.path === '/markets') {
    // 如果已经在 markets 页面，通过自定义事件触发
    const event = new CustomEvent('load-my-markets')
    window.dispatchEvent(event)
  } else {
    // 跳转到 markets 页面并带上参数
    await router.push({ path: '/markets', query: { filter: 'my' } })
  }
}

const handleLogoClick = () => {
  if (route.path === '/markets') {
    // 已经在 markets 页面，清除 query 参数并触发返回所有市场事件
    if (route.query.filter) {
      router.replace({ path: '/markets', query: {} })
    }
    const event = new CustomEvent('back-to-all-markets')
    window.dispatchEvent(event)
  } else {
    // 跳转到 markets 页面
    router.push('/markets')
  }
}

const handleMarketsClick = () => {
  if (route.path === '/markets') {
    // 已经在 markets 页面，清除 query 参数并触发返回所有市场事件
    if (route.query.filter) {
      router.replace({ path: '/markets', query: {} })
    }
    const event = new CustomEvent('back-to-all-markets')
    window.dispatchEvent(event)
  } else {
    // 跳转到 markets 页面
    router.push('/markets')
  }
}

const getNetworkName = (chainId) => {
  const networks = {
    1: 'Ethereum',
    5: 'Goerli',
    11155111: 'Sepolia',
    137: 'Polygon',
    80001: 'Mumbai',
    56: 'BSC',
    97: 'BSC Testnet',
    31337: 'Hardhat Local'
  }
  return networks[chainId] || `Chain ${chainId}`
}

// 添加钱包相关函数
const openAddWalletModal = () => {
  showAddWalletModal.value = true
  addWalletStep.value = 1
  addWalletError.value = ''
  newWalletAddress.value = ''
  nonceData.value = null
  showUserMenu.value = false
}

const closeAddWalletModal = () => {
  showAddWalletModal.value = false
  addWalletStep.value = 1
  addWalletError.value = ''
  newWalletAddress.value = ''
  nonceData.value = null
}

const connectNewWallet = async () => {
  if (!window.ethereum) {
    addWalletError.value = '请安装 MetaMask 钱包'
    return
  }

  try {
    // 请求连接钱包
    const accounts = await window.ethereum.request({
      method: 'eth_requestAccounts'
    })

    if (accounts.length > 0) {
      newWalletAddress.value = accounts[0]

      // 检查是否和当前钱包相同
      if (newWalletAddress.value.toLowerCase() === walletAddress.value.toLowerCase()) {
        addWalletError.value = '这是当前已连接的钱包地址'
        return
      }

      // 进入下一步：获取 nonce
      await getWalletNonce()
    }
  } catch (error) {
    console.error('连接钱包失败:', error)
    addWalletError.value = `连接失败: ${error.message}`
  }
}

const getWalletNonce = async () => {
  isAddingWallet.value = true
  addWalletError.value = ''

  try {
    const currentChainId = await window.ethereum.request({ method: 'eth_chainId' })
    const chainIdNumber = parseInt(currentChainId, 16)

    const response = await getNonce({
      walletAddress: newWalletAddress.value,
      chainId: chainIdNumber
    })

    if (response.success && response.code === 1000) {
      nonceData.value = response.data
      addWalletStep.value = 2
    } else {
      addWalletError.value = response.message || '获取 Nonce 失败'
    }
  } catch (error) {
    console.error('获取 Nonce 失败:', error)
    addWalletError.value = `获取 Nonce 失败: ${error.message}`
  } finally {
    isAddingWallet.value = false
  }
}

const signAndAddWallet = async () => {
  if (!nonceData.value) {
    addWalletError.value = '请先获取 Nonce'
    return
  }

  isAddingWallet.value = true
  addWalletError.value = ''

  try {
    // 获取签名
    const message = nonceData.value.message
    const { ethers } = await import('ethers')
    const provider = new ethers.BrowserProvider(window.ethereum)
    const signer = await provider.getSigner()
    const signature = await signer.signMessage(message)

    // 获取当前 chainId
    const currentChainId = await window.ethereum.request({ method: 'eth_chainId' })
    const chainIdNumber = parseInt(currentChainId, 16)

    // 调用后端 API 添加钱包
    const response = await addWallet({
      userId: user.value.id,
      walletAddress: newWalletAddress.value,
      chainId: chainIdNumber,
      walletType: 'metamask',
      signature: signature
    })

    if (response.success && response.code === 1000) {
      addWalletStep.value = 3
      // 3秒后关闭弹窗
      setTimeout(() => {
        closeAddWalletModal()
      }, 3000)
    } else {
      addWalletError.value = response.message || '添加钱包失败'
    }
  } catch (error) {
    console.error('添加钱包失败:', error)
    addWalletError.value = `添加失败: ${error.message}`
  } finally {
    isAddingWallet.value = false
  }
}

// Close dropdown when clicking outside
onMounted(() => {
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.user-section')) {
      showUserMenu.value = false
    }
  })

  // Load saved wallet connection
  userStore.loadUserFromStorage()

  // 监听来自其他页面的连接钱包请求
  window.addEventListener('connect-wallet', async () => {
    await handleConnect()
  })
})
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-link {
  text-decoration: none;
  display: inline-block;
  transition: opacity 0.15s ease;
  cursor: pointer;
}

.logo-link:hover {
  opacity: 0.8;
}

.logo h1 {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.025em;
  background: linear-gradient(135deg, var(--accent) 0%, var(--accent-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.nav {
  display: flex;
  gap: 2.5rem;
}

.nav-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  font-size: 0.875rem;
  transition: color 0.15s ease;
  position: relative;
  cursor: pointer;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -1.25rem;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--accent);
  transform: scaleX(0);
  transition: transform 0.15s ease;
}

.nav-link:hover {
  color: var(--text-primary);
}

.nav-link.active {
  color: var(--text-primary);
}

.nav-link.active::after {
  transform: scaleX(1);
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.btn-secondary {
  background: transparent;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  padding: 0.625rem 1rem;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-secondary:hover {
  background: var(--input-bg);
  border-color: var(--text-primary);
}

.btn-connect {
  background: var(--accent);
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.15s ease;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

.btn-connect:hover {
  background: #1a1a1a;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  transform: translateY(-1px);
}

.btn-connect:active {
  transform: translateY(0);
}

.btn-connect:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* User Section */
.user-section {
  position: relative;
  display: flex;
  align-items: center;
}

.avatar-button {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.avatar-button:hover {
  border-color: var(--accent-light);
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.avatar-text {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* User Menu Popup */
.user-menu-popup {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 320px;
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  overflow: hidden;
  animation: slideIn 0.2s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-menu-header {
  padding: 20px 24px;
  background: var(--input-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.user-nickname {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-menu-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-menu-content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user-info-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.user-info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  word-break: break-all;
}

.balance-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.refresh-btn:hover {
  background: var(--accent);
  border-color: var(--accent);
  color: white;
  transform: rotate(180deg);
}

.refresh-btn:active {
  transform: rotate(180deg) scale(0.95);
}

.user-address-full {
  font-size: 12px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  opacity: 0.8;
}

.user-menu-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--input-bg);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: transparent;
  color: #EF4444;
  border: 1px solid #EF4444;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-btn:hover {
  background: #EF4444;
  color: white;
}

.logout-btn:active {
  transform: scale(0.98);
}

/* 添加钱包按钮 */
.add-wallet-btn {
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: transparent;
  color: var(--accent);
  border: 1px solid var(--accent);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.add-wallet-btn:hover {
  background: var(--accent);
  color: white;
}

.add-wallet-btn:active {
  transform: scale(0.98);
}

/* 添加钱包弹窗 */
.add-wallet-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.add-wallet-modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-step .modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-step .modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.modal-step .modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-description {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
  text-align: center;
}

.wallet-info {
  padding: 16px;
  background: var(--input-bg);
  border-radius: 8px;
  margin-bottom: 8px;
}

.wallet-info p {
  margin: 4px 0;
}

.wallet-info .address {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: var(--text-primary);
  word-break: break-all;
}

.connect-wallet-btn,
.sign-btn {
  width: 100%;
  padding: 14px;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.connect-wallet-btn:hover:not(:disabled),
.sign-btn:hover:not(:disabled) {
  background: #1a1a1a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.connect-wallet-btn:active:not(:disabled),
.sign-btn:active:not(:disabled) {
  transform: translateY(0);
}

.connect-wallet-btn:disabled,
.sign-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  padding: 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  color: #dc2626;
  font-size: 14px;
  text-align: center;
}

.success-body {
  text-align: center;
  padding: 32px 24px;
}

.success-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.success-body p {
  margin: 8px 0;
  color: var(--text-primary);
}

.success-address {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: var(--text-secondary);
  word-break: break-all;
}
</style>
