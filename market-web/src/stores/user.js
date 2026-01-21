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
        chainId.value = null
      }

      try {
        const balanceBigNumber = await provider.getBalance(walletAddress.value)
        balance.value = ethers.formatEther(balanceBigNumber)
      } catch (balanceErr) {
        balance.value = '0'
      }

      // Perform signature login
      await performSignatureLogin(provider, address)

      return true
    } catch (err) {
      error.value = err.message || 'Failed to connect wallet'
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 签名登录流程
  const performSignatureLogin = async (provider, address) => {
    try {
      const nonceData = await post('/user/nonce', {
        walletAddress: address,
        chainId: chainId.value
      })

      if (!nonceData.data || !nonceData.data.nonce) {
        throw new Error('Invalid nonce response')
      }

      const nonce = nonceData.data.nonce
      const message = nonceData.data.message

      const signer = await provider.getSigner()
      const signature = await signer.signMessage(message)

      const loginData = await post('/user/login', {
        walletAddress: address,
        chainId: chainId.value,
        signature: signature
      })

      // 检查登录是否成功
      if (!loginData.success || loginData.code !== 1000) {
        throw new Error(loginData.message || 'Login failed')
      }

      // 登录成功，保存用户信息
      user.value = loginData.data
      // 确保 userId 字段存在（从后端返回的 id 字段）
      if (loginData.data.id && !user.value.userId) {
        user.value.userId = loginData.data.id
      }
      localStorage.setItem('userInfo', JSON.stringify(user.value))
      localStorage.setItem('walletConnected', 'true')
      localStorage.setItem('walletAddress', address)

      isConnected.value = true
    } catch (err) {
      console.error('[UserStore] Login failed:', err)
      // 登录失败，清除用户信息
      user.value = null
      isConnected.value = false
      throw err
    }
  }

  const disconnectWallet = async () => {
    try {
      if (walletAddress.value) {
        try {
          await postWithAuth('/user/logout', {
            walletAddress: walletAddress.value
          })
        } catch (err) {
          // 即使接口调用失败，也继续执行本地退出逻辑
        }
      }
    } catch (err) {
      // Error handling
    } finally {
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

      if (window.ethereum) {
        const provider = new ethers.BrowserProvider(window.ethereum)
        provider.getBalance(walletAddress.value).then((balanceBigNumber) => {
          balance.value = ethers.formatEther(balanceBigNumber)
        }).catch(() => {})

        provider.getNetwork().then(network => {
          chainId.value = Number(network.chainId)
        }).catch(() => {})
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
      // Error handling
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
