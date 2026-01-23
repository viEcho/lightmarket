import { ethers } from 'ethers'

// ==================== 配置 ====================
const CONFIG = {
  USDC_ADDRESS: import.meta.env.VITE_USDC_ADDRESS || '',
  CHAIN_ID: import.meta.env.VITE_CHAIN_ID || '31337'
}

// ==================== ABI ====================
const MOCK_USDC_ABI = [
  "function mint(address to, uint256 amount) external",
  "function balanceOf(address account) view returns (uint256)"
]

// ==================== 工具函数 ====================

/**
 * 连接钱包
 * @returns {Promise<{address: string, provider: *, signer: *}>}
 */
async function connectWallet() {
  if (!window.ethereum) {
    throw new Error('请安装 MetaMask 钱包!')
  }

  const provider = new ethers.BrowserProvider(window.ethereum)
  await provider.send('eth_requestAccounts', [])
  const signer = await provider.getSigner()
  const address = await signer.getAddress()

  // 检查网络
  const network = await provider.getNetwork()
  const networkId = Number(network.chainId)

  if (networkId !== parseInt(CONFIG.CHAIN_ID)) {
    throw new Error(`请切换到正确的网络! 当前: ${networkId}, 需要: ${CONFIG.CHAIN_ID}`)
  }

  return { address, provider, signer }
}

/**
 * 铸造测试 USDC（水龙头）
 *
 * ⚠️ 仅用于测试网络！
 *
 * @param {string|number} amount - 铸造数量（USDC）
 * @param {Object} options - 选项
 * @param {Function} options.onTransactionHash - 交易哈希回调
 * @param {Function} options.onReceipt - 交易收据回调
 * @returns {Promise<Object>} 铸造结果
 *
 * @example
 * const result = await mintTestUSDC(10000, {
 *   onTransactionHash: (hash) => console.log('交易哈希:', hash),
 *   onReceipt: (receipt) => console.log('交易确认')
 * })
 */
async function mintTestUSDC(amount, { onTransactionHash, onReceipt } = {}) {
  try {
    // 1. 连接钱包
    const { signer, address } = await connectWallet()

    // 2. 验证配置
    if (!CONFIG.USDC_ADDRESS) {
      throw new Error('USDC 合约地址未配置，请在 .env 文件中设置 VITE_USDC_ADDRESS')
    }

    // 3. 创建合约实例
    const usdcContract = new ethers.Contract(CONFIG.USDC_ADDRESS, MOCK_USDC_ABI, signer)

    // 4. 查询铸造前余额
    const balanceBefore = await usdcContract.balanceOf(address)

    // 5. 转换为 wei
    const amountWei = ethers.parseUnits(amount.toString(), 6)

    // 6. 铸造 USDC
    const tx = await usdcContract.mint(address, amountWei)
    console.log('[Faucet] 交易已提交:', tx.hash)

    // 触发回调
    if (onTransactionHash) {
      onTransactionHash(tx.hash)
    }

    // 7. 等待交易确认
    const receipt = await tx.wait()
    console.log('[Faucet] 交易已确认, Gas Used:', receipt?.gasUsed?.toString())

    // 触发回调
    if (onReceipt) {
      onReceipt(receipt)
    }

    // 8. 查询铸造后余额
    const balanceAfter = await usdcContract.balanceOf(address)
    const minted = ethers.formatUnits(balanceAfter - balanceBefore, 6)

    return {
      success: true,
      txHash: receipt.hash,
      balanceBefore: ethers.formatUnits(balanceBefore, 6),
      balanceAfter: ethers.formatUnits(balanceAfter, 6),
      minted
    }

  } catch (error) {
    console.error('[Faucet] ❌ 铸造失败:', error)

    let userMessage = error.message
    if (error.code === 'ACTION_REJECTED') {
      userMessage = '用户拒绝了交易'
    }

    return {
      success: false,
      error: userMessage,
      code: error.code
    }
  }
}

/**
 * 查询 USDC 余额
 * @param {string} userAddress - 用户地址（可选，默认当前连接的钱包）
 * @returns {Promise<string>} USDC 余额（字符串格式）
 */
async function getUSDCBalance(userAddress = null) {
  try {
    const { provider, address: connectedAddress } = await connectWallet()
    const targetAddress = userAddress || connectedAddress

    if (!CONFIG.USDC_ADDRESS) {
      throw new Error('USDC 合约地址未配置')
    }

    const usdcContract = new ethers.Contract(CONFIG.USDC_ADDRESS, MOCK_USDC_ABI, provider)
    const balance = await usdcContract.balanceOf(targetAddress)

    return ethers.formatUnits(balance, 6)
  } catch (error) {
    console.error('[Faucet] 查询余额失败:', error)
    return '0'
  }
}

// 导出
export {
  connectWallet,
  mintTestUSDC,
  getUSDCBalance
}
