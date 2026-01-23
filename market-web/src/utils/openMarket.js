import { ethers } from 'ethers'

// ==================== 配置 ====================
// 从环境变量读取合约地址
const CONFIG = {
  FACTORY_ADDRESS: import.meta.env.VITE_FACTORY_ADDRESS || '',
  USDC_ADDRESS: import.meta.env.VITE_USDC_ADDRESS || '',
  CHAIN_ID: import.meta.env.VITE_CHAIN_ID || '31337'
}

// ==================== ABI ====================
// MarketFactory 合约 ABI
const MARKET_FACTORY_ABI = [
  // 查询 USDC 地址
  "function usdc() view returns (address)",

  // 创建市场
  "function createMarket(bytes32 marketId, uint256 endTime, uint256 initialLiquidity) external returns (address)",

  // 查询市场地址
  "function getMarketAddress(bytes32) view returns (address)",

  // 检查市场是否存在
  "function marketExists(bytes32) view returns (bool)",

  // 事件
  "event MarketCreated(bytes32 indexed marketId, address indexed market, address indexed creator, uint256 endTime, uint256 initialLiquidity)"
]

// PredictionMarket (AMM) 合约 ABI
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

  // 链下订单撮合
  "function executeOrder(address buyer, address seller, uint256 yesAmount, uint256 price) external",

  // 结算功能
  "function finalize(bool yesWon) external",
  "function claimLiquidity() external",
  "function claimWinnings(bool yesWon) external",
  "function withdraw(uint256 amount) external",

  // 事件
  "event Initialized(uint256 yesAmount, uint256 noAmount, uint256 initialPrice)",
  "event BoughtYes(address indexed user, uint256 usdcSpent, uint256 yesReceived, uint256 newPrice)",
  "event SoldYes(address indexed user, uint256 yesSold, uint256 usdcReceived, uint256 newPrice)",
  "event OrderMatched(address indexed buyer, address indexed seller, uint256 yesAmount, uint256 price, uint256 usdcAmount)",
  "event MarketFinalized(bool yesWon)",
  "event LiquidityClaimed(address indexed user, uint256 amount)",
  "event WinningsClaimed(address indexed user, uint256 amount)"
]

// ERC20 (USDC) 合约 ABI
const ERC20_ABI = [
  "function approve(address spender, uint256 amount) external returns (bool)",
  "function allowance(address owner, address spender) view returns (uint256)",
  "function balanceOf(address account) view returns (uint256)",
  "function transfer(address to, uint256 amount) external returns (bool)",
  "function transferFrom(address from, address to, uint256 amount) external returns (bool)"
]

// ==================== 工具函数 ====================

/**
 * 检查用户是否已连接钱包
 * @returns {Promise<{address: string, provider: *, signer: *}>}
 */
async function connectWallet() {
  if (!window.ethereum) {
    throw new Error('请安装 MetaMask 钱包!')
  }

  const provider = new ethers.BrowserProvider(window.ethereum)

  // 请求用户连接钱包
  await provider.send('eth_requestAccounts', [])
  const signer = await provider.getSigner()
  const address = await signer.getAddress()

  // 检查网络是否匹配
  const network = await provider.getNetwork()
  const networkId = network.chainId.toString()

  if (networkId !== CONFIG.CHAIN_ID) {
    throw new Error(`请切换到正确的网络! Chain ID: ${CONFIG.CHAIN_ID}`)
  }

  return { address, provider, signer }
}

/**
 * 查询用户 USDC 余额
 * @param {string} userAddress - 用户地址
 * @returns {Promise<string>} USDC 余额（字符串格式）
 */
async function getUSDCBalance(userAddress) {
  if (!CONFIG.USDC_ADDRESS) {
    throw new Error('USDC 合约地址未配置')
  }

  console.log('[getUSDCBalance] 配置信息:')
  console.log('  USDC_ADDRESS:', CONFIG.USDC_ADDRESS)
  console.log('  CHAIN_ID:', CONFIG.CHAIN_ID)
  console.log('  用户地址:', userAddress)

  // 检查 window.ethereum
  if (!window.ethereum) {
    throw new Error('未检测到 MetaMask 或 Web3 钱包')
  }

  const provider = new ethers.BrowserProvider(window.ethereum)

  // 检查网络
  try {
    const network = await provider.getNetwork()
    console.log('[getUSDCBalance] 当前连接的网络:')
    console.log('  Chain ID:', network.chainId.toString())
    console.log('  配置的 Chain ID:', CONFIG.CHAIN_ID)

    if (network.chainId.toString() !== CONFIG.CHAIN_ID) {
      throw new Error(`网络不匹配！当前: ${network.chainId.toString()}, 需要: ${CONFIG.CHAIN_ID}`)
    }
  } catch (error) {
    console.error('[getUSDCBalance] 网络检查失败:', error)
    throw error
  }

  // 查询余额
  try {
    console.log('[getUSDCBalance] 调用 balanceOf...')
    const usdcContract = new ethers.Contract(CONFIG.USDC_ADDRESS, ERC20_ABI, provider)

    // 先测试合约是否可访问
    try {
      const name = await usdcContract.name()
      console.log('[getUSDCBalance] 合约名称:', name)
    } catch (e) {
      console.error('[getUSDCBalance] 合约不可访问:', e.message)
      throw new Error(`USDC 合约地址 ${CONFIG.USDC_ADDRESS} 不可访问，请检查合约是否已部署`)
    }

    const balance = await usdcContract.balanceOf(userAddress)
    console.log('[getUSDCBalance] 原始余额:', balance.toString())

    // USDC 使用 6 位小数
    return ethers.formatUnits(balance, 6)
  } catch (error) {
    console.error('[getUSDCBalance] 查询余额失败:', error)
    throw error
  }
}

/**
 * 开放市场（调用 Factory 合约创建市场合约 + 质押 USDC）
 *
 * ⚠️ 安全改进：
 * 1. 前端只调用合约并获取 txHash
 * 2. 不从事件中解析数据（防止被篡改）
 * 3. 只将 txHash 发送给后端
 * 4. 后端通过 txHash 查询链上真实数据
 *
 * @param {Object} marketData - 市场数据
 * @param {string} marketData.id - 后端数据库中的市场ID
 * @param {string} marketData.title - 市场标题
 * @param {string} marketData.endTime - 结束时间（ISO 8601 格式）
 * @param {number|string} marketData.baseLiquidity - 初始流动性（USDC数量）
 * @param {Function} onTransactionHash - 交易哈希回调（可选）
 * @param {Function} onReceipt - 交易收据回调（可选）
 * @returns {Promise<Object>} 创建结果
 */
async function openMarket(marketData, { onTransactionHash, onReceipt } = {}) {
  try {
    // 连接钱包
    const { address, signer } = await connectWallet()

    // 验证配置
    if (!CONFIG.FACTORY_ADDRESS) {
      throw new Error('Factory 合约地址未配置，请在 .env 文件中设置 VITE_FACTORY_ADDRESS')
    }
    if (!CONFIG.USDC_ADDRESS) {
      throw new Error('USDC 合约地址未配置，请在 .env 文件中设置 VITE_USDC_ADDRESS')
    }

    // 准备参数
    const onChainMarketId = ethers.keccak256(
      ethers.toUtf8Bytes(`${marketData.id}-${marketData.title}-${Date.now()}`)
    )

    const endTimeTimestamp = Math.floor(new Date(marketData.endTime).getTime() / 1000)

    // 验证结束时间
    const now = Math.floor(Date.now() / 1000)
    if (endTimeTimestamp <= now) {
      throw new Error('市场结束时间必须大于当前时间')
    }

    const liquidityWei = ethers.parseUnits(marketData.baseLiquidity.toString(), 6)

    // 创建合约实例
    const factoryContract = new ethers.Contract(
      CONFIG.FACTORY_ADDRESS,
      MARKET_FACTORY_ABI,
      signer
    )

    const usdcContract = new ethers.Contract(
      CONFIG.USDC_ADDRESS,
      ERC20_ABI,
      signer
    )

    // 检查并授权 USDC
    const currentAllowance = await usdcContract.allowance(address, CONFIG.FACTORY_ADDRESS)

    if (currentAllowance < liquidityWei) {
      const approveTx = await usdcContract.approve(CONFIG.FACTORY_ADDRESS, liquidityWei)
      console.log('[OpenMarket] Approve 交易:', approveTx.hash)
      await approveTx.wait()
    }

    // 调用 Factory.createMarket
    let gasEstimate
    try {
      gasEstimate = await factoryContract.createMarket.estimateGas(
        onChainMarketId,
        endTimeTimestamp,
        liquidityWei
      )
    } catch (error) {
      console.error('[OpenMarket] Gas 估算失败:', error)
      throw new Error(`Gas 估算失败: ${error.message}`)
    }

    // 发送交易
    console.log('[OpenMarket] 准备发送 createMarket 交易...')
    console.log('[OpenMarket] 参数: marketId, endTime:', endTimeTimestamp, ', liquidity:', liquidityWei.toString())

    const tx = await factoryContract.createMarket(
      onChainMarketId,
      endTimeTimestamp,
      liquidityWei,
      {
        gasLimit: (gasEstimate * BigInt(120)) / BigInt(100)
      }
    )

    console.log('[OpenMarket] ✅ CreateMarket 交易已发送, hash:', tx.hash)

    // 触发回调，传递交易哈希和链上市场ID
    if (onTransactionHash) {
      onTransactionHash(tx.hash, onChainMarketId)
    }

    // 等待交易确认
    const receipt = await tx.wait()

    console.log('[OpenMarket] 交易已确认, Gas Used:', receipt?.gasUsed?.toString())

    // 触发回调
    if (onReceipt) {
      onReceipt(receipt)
    }

    // ✅ 从收据解析 MarketCreated 事件，获取 marketAddress
    // 这是安全的，因为：
    // 1. 事件来自已确认的交易收据（来自 RPC 节点）
    // 2. CREATE2 地址是确定性的（无法伪造）
    // 3. 后端仍会通过 txHash 验证数据
    let marketAddress = null
    try {
      const event = receipt.logs.find(log => {
        try {
          const parsed = factoryContract.interface.parseLog(log)
          return parsed.name === 'MarketCreated'
        } catch (e) {
          return false
        }
      })

      if (event) {
        const parsed = factoryContract.interface.parseLog(event)
        marketAddress = parsed.args.market
        console.log('[OpenMarket] ✅ 从事件解析出 marketAddress:', marketAddress)
      }
    } catch (error) {
      console.warn('[OpenMarket] ⚠️ 解析 MarketCreated 事件失败:', error)
      // 不影响主流程，marketAddress 为空时后端会处理
    }

    const result = {
      success: true,
      txHash: receipt.hash,
      blockNumber: receipt.blockNumber,
      gasUsed: receipt.gasUsed.toString(),
      onChainMarketId: onChainMarketId,
      marketAddress: marketAddress // ✅ 添加 marketAddress
    }

    return result

  } catch (error) {
    console.error('[OpenMarket] ❌ 开放市场失败:', error)

    // 用户友好的错误信息
    let userMessage = error.message

    if (error.code === 'ACTION_REJECTED') {
      userMessage = '用户拒绝了交易'
    } else if (error.message.includes('insufficient funds')) {
      userMessage = 'USDC 余额不足'
    } else if (error.message.includes('invalid end time')) {
      userMessage = '市场结束时间无效'
    }

    return {
      success: false,
      error: userMessage,
      code: error.code
    }
  }
}

/**
 * 查询市场合约地址
 * @param {string} onChainMarketId - 链上市场ID
 * @returns {Promise<string|null>} 市场合约地址，如果不存在返回 null
 */
async function getMarketAddress(onChainMarketId) {
  try {
    if (!CONFIG.FACTORY_ADDRESS) {
      throw new Error('Factory 合约地址未配置')
    }

    const provider = new ethers.BrowserProvider(window.ethereum)
    const factoryContract = new ethers.Contract(
      CONFIG.FACTORY_ADDRESS,
      MARKET_FACTORY_ABI,
      provider
    )

    const address = await factoryContract.getMarketAddress(onChainMarketId)
    return address
  } catch (error) {
    console.error('[OpenMarket] 查询市场地址失败:', error)
    return null
  }
}

/**
 * 检查市场是否已部署
 * @param {string} onChainMarketId - 链上市场ID
 * @returns {Promise<boolean>}
 */
async function isMarketDeployed(onChainMarketId) {
  try {
    if (!CONFIG.FACTORY_ADDRESS) {
      return false
    }

    const provider = new ethers.BrowserProvider(window.ethereum)
    const factoryContract = new ethers.Contract(
      CONFIG.FACTORY_ADDRESS,
      MARKET_FACTORY_ABI,
      provider
    )

    return await factoryContract.marketExists(onChainMarketId)
  } catch (error) {
    console.error('[OpenMarket] 检查市场部署失败:', error)
    return false
  }
}

// ==================== AMM 交易功能 ====================

/**
 * 买入 YES 代币
 * @param {string} marketAddress - 市场合约地址
 * @param {string|number} usdcAmount - 支付的 USDC 数量
 * @returns {Promise<Object>} 交易结果
 */
async function buyYes(marketAddress, usdcAmount) {
  try {
    const { signer } = await connectWallet()

    const marketContract = new ethers.Contract(
      marketAddress,
      MARKET_ABI,
      signer
    )

    // 转换为 wei
    const amountWei = ethers.parseUnits(usdcAmount.toString(), 6)

    // Approve USDC
    const usdcContract = new ethers.Contract(
      CONFIG.USDC_ADDRESS,
      ERC20_ABI,
      signer
    )

    const approveTx = await usdcContract.approve(marketAddress, amountWei)
    await approveTx.wait()

    // 买入 YES
    const tx = await marketContract.buyYes(amountWei)
    const receipt = await tx.wait()

    // 解析事件
    const event = receipt.logs.find(log => {
      try {
        const parsed = marketContract.interface.parseLog(log)
        return parsed.name === 'BoughtYes'
      } catch (e) {
        return false
      }
    })

    if (event) {
      const parsed = marketContract.interface.parseLog(event)
      return {
        success: true,
        actualSpent: ethers.formatUnits(parsed.args.usdcSpent, 6),
        yesReceived: ethers.formatUnits(parsed.args.yesReceived, 6),
        newPrice: parsed.args.newPrice,
        txHash: receipt.hash
      }
    }

    return {
      success: true,
      txHash: receipt.hash
    }

  } catch (error) {
    console.error('[buyYes] 交易失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * 卖出 YES 代币
 * @param {string} marketAddress - 市场合约地址
 * @param {string|number} yesAmount - 卖出的 YES 数量
 * @returns {Promise<Object>} 交易结果
 */
async function sellYes(marketAddress, yesAmount) {
  try {
    const { signer } = await connectWallet()

    const marketContract = new ethers.Contract(
      marketAddress,
      MARKET_ABI,
      signer
    )

    // 转换为 wei
    const amountWei = ethers.parseUnits(yesAmount.toString(), 6)

    // 卖出 YES
    const tx = await marketContract.sellYes(amountWei)
    const receipt = await tx.wait()

    // 解析事件
    const event = receipt.logs.find(log => {
      try {
        const parsed = marketContract.interface.parseLog(log)
        return parsed.name === 'SoldYes'
      } catch (e) {
        return false
      }
    })

    if (event) {
      const parsed = marketContract.interface.parseLog(event)
      return {
        success: true,
        yesSold: ethers.formatUnits(parsed.args.yesSold, 6),
        usdcReceived: ethers.formatUnits(parsed.args.usdcReceived, 6),
        newPrice: parsed.args.newPrice,
        txHash: receipt.hash
      }
    }

    return {
      success: true,
      txHash: receipt.hash
    }

  } catch (error) {
    console.error('[sellYes] 交易失败:', error)
    return {
      success: false,
      error: error.message
    }
  }
}

/**
 * 查询当前价格和池子信息
 * @param {string} marketAddress - 市场合约地址
 * @returns {Promise<Object>} 池子信息
 */
async function getPoolInfo(marketAddress) {
  try {
    const provider = new ethers.BrowserProvider(window.ethereum)

    const marketContract = new ethers.Contract(
      marketAddress,
      MARKET_ABI,
      provider
    )

    const poolInfo = await marketContract.getPoolInfo()

    return {
      yesPool: ethers.formatUnits(poolInfo[0], 6),
      noPool: ethers.formatUnits(poolInfo[1], 6),
      yesPrice: poolInfo[2],
      noPrice: poolInfo[3],
      totalLiquidity: poolInfo[4]
    }
  } catch (error) {
    console.error('[getPoolInfo] 查询失败:', error)
    return null
  }
}

// 导出
export {
  openMarket,
  getMarketAddress,
  isMarketDeployed,
  getUSDCBalance,
  connectWallet,
  buyYes,
  sellYes,
  getPoolInfo,
  CONFIG
}
