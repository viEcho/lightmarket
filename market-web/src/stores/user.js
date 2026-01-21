import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ethers } from 'ethers'
import { post, postWithAuth } from '../utils/api'

export const useUserStore = defineStore('user', () => {
  // State
  const walletAddress = ref(null)
  const isConnected = ref(false)
  const balance = ref('0')
  const chainId = ref(null)
  const user = ref(null)
  const isLoading = ref(false)
  const error = ref(null)

  // Computed
  const shortAddress = computed(() => {
    if (!walletAddress.value) return ''
    return `${walletAddress.value.slice(0, 6)}...${walletAddress.value.slice(-4)}`
  })

  const isLoggedIn = computed(() => {
    return isConnected.value && user.value !== null
  })

  // Actions
  const connectWallet = async () => {
    try {
      isLoading.value = true
      error.value = null

      // Check if MetaMask is installed
      if (!window.ethereum) {
        error.value = 'Please install MetaMask to use this feature'
        alert('Please install MetaMask wallet to continue')
        window.open('https://metamask.io/download/', '_blank')
        return false
      }

      // Request account access
      const provider = new ethers.BrowserProvider(window.ethereum)

      let accounts
      try {
        accounts = await provider.send('eth_requestAccounts', [])
      } catch (rpcError) {
        // Handle RPC errors
        if (rpcError.code === -32002 || rpcError.message?.includes('too many errors')) {
          error.value = 'RPC endpoint is busy. Please wait a moment and try again, or switch to a different network in MetaMask.'
          alert('MetaMask RPC endpoint is experiencing issues. Please:\n1. Wait 30 seconds and try again\n2. Or switch to a different network in MetaMask\n3. Or add a custom RPC endpoint in MetaMask settings')
          return false
        }
        throw rpcError
      }

      if (accounts.length === 0) {
        error.value = 'No accounts found'
        return false
      }

      // Get wallet address
      const address = accounts[0]
      walletAddress.value = address
      // 注意：不在这里设置 isConnected.value = true
      // 要等到登录成功后再设置，确保右上角按钮在登录完成后才切换

      // Get network
      let network
      try {
        network = await provider.getNetwork()
        // 保持为数字类型（后端需要 Integer），不转换为字符串
        chainId.value = Number(network.chainId)
      } catch (err) {
        console.warn('Could not get network:', err)
        chainId.value = null
      }

      // Get balance (may fail if RPC is busy, don't block connection)
      try {
        const balanceBigNumber = await provider.getBalance(walletAddress.value)
        balance.value = ethers.formatEther(balanceBigNumber)
      } catch (balanceErr) {
        console.warn('Could not get balance:', balanceErr)
        balance.value = '0'

        // Check if it's an RPC error and show user-friendly message
        if (balanceErr.code === -32002 || balanceErr.message?.includes('too many errors')) {
          console.warn('⚠️ MetaMask RPC 端点繁忙')
          console.warn('💡 解决方法：')
          console.warn('   1. 在 MetaMask 中切换到你刚添加的 "Sepolia Infura" 网络')
          console.warn('   2. 或者等待 30 秒后重试')
          console.warn('   3. 或者切换到其他网络（如 Polygon）')
        }
      }

      // Perform signature login
      await performSignatureLogin(provider, address)

      // 登录成功后才设置 isConnected = true
      console.log('🔄 准备设置 isConnected = true, 当前值:', isConnected.value)
      isConnected.value = true
      console.log('✅ 已设置 isConnected = true')

      return true
    } catch (err) {
      console.error('Wallet connection error:', err)
      error.value = err.message || 'Failed to connect wallet'
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 签名登录流程
  const performSignatureLogin = async (provider, address) => {
    try {
      // 1. 请求 nonce（添加 chainId 参数）
      console.log('📝 请求 nonce...')
      const nonceData = await post('/user/nonce', {
        walletAddress: address,
        chainId: chainId.value
      })

      if (!nonceData.data || !nonceData.data.nonce) {
        throw new Error('Invalid nonce response')
      }

      const nonce = nonceData.data.nonce
      const message = nonceData.data.message // 使用后端返回的消息
      console.log('✅ 获取到 nonce:', nonce)
      console.log('📋 待签名消息:', message)

      // 2. 请求签名
      const signer = await provider.getSigner()
      console.log('🔐 请求签名...')

      const signature = await signer.signMessage(message)
      console.log('✅ 签名完成:', signature)

      // 3. 提交签名登录（添加 chainId 参数）
      console.log('🚀 提交签名登录...')
      const loginData = await post('/user/login', {
        walletAddress: address,
        chainId: chainId.value,
        signature: signature
      })

      // 4. 保存用户信息（包含 accessToken）
      user.value = loginData.data
      localStorage.setItem('userInfo', JSON.stringify(loginData.data))
      localStorage.setItem('walletConnected', 'true')
      localStorage.setItem('walletAddress', address)

      console.log('✅ 登录成功!')
      console.log('👤 用户信息:', {
        uid: loginData.data.uid,
        nickname: loginData.data.nickname,
        walletAddress: loginData.data.walletAddress,
        accessToken: loginData.data.accessToken,
        avatar: loginData.data.avatar
      })
      console.log('📦 store.state:', {
        isConnected: isConnected.value,
        user: user.value
      })
    } catch (err) {
      console.warn('⚠️ 签名登录失败:', err.message)
      console.warn('💡 后台服务可能未启动，使用离线模式')

      // 后台服务不可用时的降级处理
      user.value = {
        walletAddress: address,
        nickname: `${address.slice(0, 6)}...${address.slice(-4)}`,
        avatar: null,
        createdAt: new Date().toISOString()
      }
      localStorage.setItem('userInfo', JSON.stringify(user.value))
      localStorage.setItem('walletConnected', 'true')
      localStorage.setItem('walletAddress', address)

      // 即使降级也要设置为已连接状态
      isConnected.value = true
      console.log('📦 降级模式 store.state:', {
        isConnected: isConnected.value,
        user: user.value
      })
    }
  }

  const disconnectWallet = async () => {
    try {
      // 调用后台退出登录接口（预留）
      if (walletAddress.value) {
        try {
          await postWithAuth('/user/logout', {
            walletAddress: walletAddress.value
          })
        } catch (err) {
          console.warn('退出登录接口调用失败:', err.message)
          // 即使接口调用失败，也继续执行本地退出逻辑
        }
      }
    } catch (err) {
      console.warn('退出登录时发生错误:', err)
    } finally {
      // 无论接口是否成功，都清除本地状态
      walletAddress.value = null
      isConnected.value = false
      balance.value = '0'
      chainId.value = null
      user.value = null
      error.value = null

      localStorage.removeItem('walletConnected')
      localStorage.removeItem('walletAddress')
      localStorage.removeItem('userInfo')
    }
  }

  const loadUserFromStorage = () => {
    const savedWallet = localStorage.getItem('walletAddress')
    const savedUser = localStorage.getItem('userInfo')
    const wasConnected = localStorage.getItem('walletConnected')

    if (wasConnected && savedWallet && savedUser) {
      walletAddress.value = savedWallet
      user.value = JSON.parse(savedUser)
      isConnected.value = true

      // Try to reconnect to wallet
      if (window.ethereum) {
        const provider = new ethers.BrowserProvider(window.ethereum)
        provider.getBalance(walletAddress.value).then((balanceBigNumber) => {
          balance.value = ethers.formatEther(balanceBigNumber)
        }).catch(err => console.error('Error getting balance:', err))

        provider.getNetwork().then(network => {
          // 保持为数字类型
          chainId.value = Number(network.chainId)
        }).catch(err => console.error('Error getting network:', err))
      }
    }
  }

  const updateUserBalance = async () => {
    if (!isConnected.value || !walletAddress.value) return

    try {
      const provider = new ethers.BrowserProvider(window.ethereum)
      const balanceBigNumber = await provider.getBalance(walletAddress.value)
      balance.value = ethers.formatEther(balanceBigNumber)
    } catch (err) {
      console.error('Error updating balance:', err)
    }
  }

  return {
    // State
    walletAddress,
    isConnected,
    balance,
    chainId,
    user,
    isLoading,
    error,

    // Computed
    shortAddress,
    isLoggedIn,

    // Actions
    connectWallet,
    disconnectWallet,
    loadUserFromStorage,
    updateUserBalance
  }
})
