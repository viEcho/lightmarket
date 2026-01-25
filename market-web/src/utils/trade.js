import { ethers } from 'ethers'

// ==================== 配置 ====================
const CONFIG = {
  USDC_ADDRESS: import.meta.env.VITE_USDC_ADDRESS || '',
  CHAIN_ID: import.meta.env.VITE_CHAIN_ID || '31337'
}

// ==================== ABI ====================
// PredictionMarket (OrderbookMarket) 合约 ABI
const MARKET_ABI = [
  // 查询功能
  "function getYesPrice() view returns (uint256)",
  "function getNoPrice() view returns (uint256)",
  "function getPoolInfo() view returns (uint256 yesPool, uint256 noPool, uint256 yesPrice, uint256 noPrice)",
  "function getUserBalances(address user) view returns (uint256 yesAmount, uint256 noAmount, uint256 lockedAmount, uint256 withdrawable)",

  // 交易功能
  "function buyYes(uint256 usdcAmount) external returns (uint256 actualSpent, uint256 yesReceived)",
  "function sellYes(uint256 yesAmount) external returns (uint256 usdcReceived)",

  // 预估功能
  "function getBuyYesEstimate(uint256 yesAmount) view returns (uint256 estimatedCost)",
  "function getSellYesEstimate(uint256 yesAmount) view returns (uint256 estimatedRevenue)",

  // 事件
  "event BoughtYes(address indexed user, uint256 usdcSpent, uint256 yesReceived, uint256 newPrice)",
  "event SoldYes(address indexed user, uint256 yesSold, uint256 usdcReceived, uint256 newPrice)"
]

// ERC20 (USDC) 合约 ABI
const ERC20_ABI = [
  "function name() view returns (string)",
  "function symbol() view returns (string)",
  "function decimals() view returns (uint8)",
  "function totalSupply() view returns (uint256)",
  "function balanceOf(address account) view returns (uint256)",
  "function approve(address spender, uint256 amount) external returns (bool)",
  "function allowance(address owner, address spender) view returns (uint256)",
  "function transfer(address to, uint256 amount) external returns (bool)",
  "function transferFrom(address from, address to, uint256 amount) external returns (bool)"
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
  const networkId = network.chainId.toString()

  if (networkId !== CONFIG.CHAIN_ID) {
    throw new Error(`请切换到正确的网络! Chain ID: ${CONFIG.CHAIN_ID}`)
  }

  return { address, provider, signer }
}

/**
 * 买入 YES 代币
 *
 * @param {string} marketAddress - 市场合约地址
 * @param {string|number} usdcAmount - 支付的 USDC 数量
 * @param {Object} options - 选项
 * @param {Function} options.onTransactionHash - 交易哈希回调
 * @param {Function} options.onReceipt - 交易收据回调
 * @param {Function} options.onEvent - 事件回调（返回解析后的事件数据）
 * @returns {Promise<Object>} 交易结果
 *
 * @example
 * const result = await buyYes(marketAddress, 100, {
 *   onTransactionHash: (hash) => console.log('交易哈希:', hash),
 *   onReceipt: (receipt) => console.log('交易确认'),
 *   onEvent: (event) => console.log('事件:', event)
 * })
 */
async function buyYes(marketAddress, usdcAmount, { onTransactionHash, onReceipt, onEvent } = {}) {
  try {
    // 1. 连接钱包
    const { signer, address } = await connectWallet()

    // 2. 创建合约实例
    const marketContract = new ethers.Contract(marketAddress, MARKET_ABI, signer)
    const usdcContract = new ethers.Contract(CONFIG.USDC_ADDRESS, ERC20_ABI, signer)

    // 3. 转换为 wei
    const amountWei = ethers.parseUnits(usdcAmount.toString(), 6)

    // 4. 检查 USDC 余额
    const balance = await usdcContract.balanceOf(address)

    if (balance < amountWei) {
      throw new Error(`USDC 余额不足! 需要: ${usdcAmount} USDC, 当前: ${ethers.formatUnits(balance, 6)} USDC`)
    }

    // 5. Approve USDC
    const currentAllowance = await usdcContract.allowance(address, marketAddress)

    if (currentAllowance < amountWei) {
      const approveTx = await usdcContract.approve(marketAddress, amountWei)
      await approveTx.wait()
    }

    // 6. 买入 YES
    const tx = await marketContract.buyYes(amountWei)
    console.log('[Trade] 交易已提交:', tx.hash)

    // 触发回调
    if (onTransactionHash) {
      onTransactionHash(tx.hash)
    }

    // 7. 等待交易确认
    const receipt = await tx.wait()
    console.log('[Trade] 交易已确认, Gas Used:', receipt?.gasUsed?.toString())

    // 触发回调
    if (onReceipt) {
      onReceipt(receipt)
    }

    // 8. 解析事件
    const event = receipt.logs.find(log => {
      try {
        const parsed = marketContract.interface.parseLog(log)
        return parsed.name === 'BoughtYes'
      } catch (e) {
        return false
      }
    })

    let eventData = null
    if (event) {
      const parsed = marketContract.interface.parseLog(event)
      eventData = {
        user: parsed.args.user,
        usdcSpent: ethers.formatUnits(parsed.args.usdcSpent, 6),
        yesReceived: ethers.formatUnits(parsed.args.yesReceived, 6),
        newPrice: parsed.args.newPrice.toString()
      }

      // 触发事件回调
      if (onEvent) {
        onEvent(eventData)
      }
    }

    // 9. 📡 记录持仓到后端
    if (eventData) {
      await recordPosition({
        marketAddress,
        action: 'buyYes',
        usdcSpent: eventData.usdcSpent,
        yesReceived: eventData.yesReceived,
        price: eventData.newPrice,
        txHash: receipt.hash
      })
    }

    return {
      success: true,
      txHash: receipt.hash,
      eventData
    }

  } catch (error) {
    console.error('[Trade] ❌ 买入 YES 失败:', error)

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
 * 卖出 YES 代币（相当于买入 NO）
 *
 * @param {string} marketAddress - 市场合约地址
 * @param {string|number} yesAmount - 要卖出的 YES 数量
 * @param {Object} options - 选项
 * @param {Function} options.onTransactionHash - 交易哈希回调
 * @param {Function} options.onReceipt - 交易收据回调
 * @param {Function} options.onEvent - 事件回调
 * @returns {Promise<Object>} 交易结果
 */
async function sellYes(marketAddress, yesAmount, { onTransactionHash, onReceipt, onEvent } = {}) {
  try {
    // 1. 连接钱包
    const { signer } = await connectWallet()

    // 2. 创建合约实例
    const marketContract = new ethers.Contract(marketAddress, MARKET_ABI, signer)

    // 3. 转换为 wei
    const amountWei = ethers.parseUnits(yesAmount.toString(), 6)

    // 4. 检查 YES 余额
    const address = await signer.getAddress()
    const balances = await marketContract.getUserBalances(address)

    if (balances[0] < amountWei) {
      throw new Error(`YES 余额不足! 需要: ${yesAmount} YES, 当前: ${ethers.formatUnits(balances[0], 6)} YES`)
    }

    // 5. 卖出 YES
    const tx = await marketContract.sellYes(amountWei)
    console.log('[Trade] 交易已提交:', tx.hash)

    // 触发回调
    if (onTransactionHash) {
      onTransactionHash(tx.hash)
    }

    // 6. 等待交易确认
    const receipt = await tx.wait()
    console.log('[Trade] 交易已确认, Gas Used:', receipt?.gasUsed?.toString())

    // 触发回调
    if (onReceipt) {
      onReceipt(receipt)
    }

    // 7. 解析事件
    const event = receipt.logs.find(log => {
      try {
        const parsed = marketContract.interface.parseLog(log)
        return parsed.name === 'SoldYes'
      } catch (e) {
        return false
      }
    })

    let eventData = null
    if (event) {
      const parsed = marketContract.interface.parseLog(event)
      eventData = {
        user: parsed.args.user,
        yesSold: ethers.formatUnits(parsed.args.yesSold, 6),
        usdcReceived: ethers.formatUnits(parsed.args.usdcReceived, 6),
        newPrice: parsed.args.newPrice.toString()
      }

      // 触发事件回调
      if (onEvent) {
        onEvent(eventData)
      }
    }

    // 8. 📡 记录持仓到后端
    if (eventData) {
      await recordPosition({
        marketAddress,
        action: 'sellYes',
        yesSold: eventData.yesSold,
        usdcReceived: eventData.usdcReceived,
        price: eventData.newPrice,
        txHash: receipt.hash
      })
    }

    return {
      success: true,
      txHash: receipt.hash,
      eventData
    }

  } catch (error) {
    console.error('[Trade] ❌ 卖出 YES 失败:', error)

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
 * 查询用户在某个市场的持仓
 * @param {string} marketAddress - 市场合约地址
 * @param {string} userAddress - 用户地址（可选，默认当前连接的钱包）
 * @returns {Promise<Object>} 持仓信息
 */
async function getUserPosition(marketAddress, userAddress = null) {
  try {
    const { provider, address: connectedAddress } = await connectWallet()
    const targetAddress = userAddress || connectedAddress

    const marketContract = new ethers.Contract(marketAddress, MARKET_ABI, provider)
    const balances = await marketContract.getUserBalances(targetAddress)

    return {
      yesAmount: ethers.formatUnits(balances[0], 6),
      noAmount: ethers.formatUnits(balances[1], 6),
      lockedAmount: ethers.formatUnits(balances[2], 6),
      withdrawable: ethers.formatUnits(balances[3], 6)
    }
  } catch (error) {
    console.error('[Trade] 查询持仓失败:', error)
    return null
  }
}

/**
 * 查询当前池子信息
 * @param {string} marketAddress - 市场合约地址
 * @returns {Promise<Object>} 池子信息
 */
async function getPoolInfo(marketAddress) {
  try {
    const { provider } = await connectWallet()
    const marketContract = new ethers.Contract(marketAddress, MARKET_ABI, provider)
    const poolInfo = await marketContract.getPoolInfo()

    return {
      yesPool: ethers.formatUnits(poolInfo[0], 6),
      noPool: ethers.formatUnits(poolInfo[1], 6),
      yesPrice: poolInfo[2].toString(),
      noPrice: poolInfo[3].toString()
    }
  } catch (error) {
    console.error('[Trade] 查询池子信息失败:', error)
    return null
  }
}

/**
 * 📡 记录持仓到后端
 * @param {Object} positionData - 持仓数据
 * @returns {Promise<void>}
 */
async function recordPosition(positionData) {
  try {
    // TODO: 调用后端 API 记录持仓
    // 示例接口：
    // await request('/position/record', {
    //   method: 'POST',
    //   body: JSON.stringify(positionData)
    // }, true)
  } catch (error) {
    console.error('[Trade] 记录持仓失败:', error)
    // 不抛出错误，避免影响主流程
  }
}

/**
 * 监听市场事件
 * @param {string} marketAddress - 市场合约地址
 * @param {Function} onBoughtYes - 买入YES事件回调
 * @param {Function} onSoldYes - 卖出YES事件回调
 * @returns {Function} 停止监听的函数
 */
function listenMarketEvents(marketAddress, { onBoughtYes, onSoldYes } = {}) {
  if (!window.ethereum) {
    console.error('[Trade] 未安装 MetaMask')
    return () => {}
  }

  const provider = new ethers.BrowserProvider(window.ethereum)
  const marketContract = new ethers.Contract(marketAddress, MARKET_ABI, provider)

  // 监听 BoughtYes 事件
  const boughtYesFilter = marketContract.filters.BoughtYes()
  marketContract.on(boughtYesFilter, (user, usdcSpent, yesReceived, newPrice) => {
    if (onBoughtYes) {
      onBoughtYes({
        user,
        usdcSpent: ethers.formatUnits(usdcSpent, 6),
        yesReceived: ethers.formatUnits(yesReceived, 6),
        newPrice: newPrice.toString()
      })
    }
  })

  // 监听 SoldYes 事件
  const soldYesFilter = marketContract.filters.SoldYes()
  marketContract.on(soldYesFilter, (user, yesSold, usdcReceived, newPrice) => {
    if (onSoldYes) {
      onSoldYes({
        user,
        yesSold: ethers.formatUnits(yesSold, 6),
        usdcReceived: ethers.formatUnits(usdcReceived, 6),
        newPrice: newPrice.toString()
      })
    }
  })

  // 返回停止监听的函数
  return () => {
    marketContract.removeAllListeners()
  }
}

// 导出
export {
  connectWallet,
  buyYes,
  sellYes,
  getUserPosition,
  getPoolInfo,
  listenMarketEvents,
  recordPosition
}
