/**
 * API 基础配置
 */
const API_BASE_URL = 'http://localhost:9999/api'

/**
 * 获取存储的 token
 */
const getToken = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.accessToken || null
    } catch (err) {
      return null
    }
  }
  return null
}

/**
 * 通用请求处理函数
 * @param {string} endpoint - API 端点
 * @param {Object} options - 请求配置
 * @returns {Promise<Object>} 响应数据
 */
const request = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  }

  // 自动添加 token 到所有请求
  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const config = {
    ...options,
    headers
  }

  try {
    const response = await fetch(url, config)

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    const data = await response.json()

    if (!data.success || data.code !== 1000) {
      throw new Error(data.message || 'Request failed')
    }

    return data
  } catch (error) {
    throw error
  }
}

/**
 * GET 请求
 * @param {string} endpoint - API 端点
 * @param {Object} params - 查询参数
 * @returns {Promise<Object>} 响应数据
 */
export const get = (endpoint, params = {}) => {
  const queryString = new URLSearchParams(params).toString()
  const url = queryString ? `${endpoint}?${queryString}` : endpoint
  return request(url, { method: 'GET' })
}

/**
 * POST 请求
 * @param {string} endpoint - API 端点
 * @param {Object} data - 请求体数据
 * @returns {Promise<Object>} 响应数据
 */
export const post = (endpoint, data = {}) => {
  return request(endpoint, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

/**
 * PUT 请求
 * @param {string} endpoint - API 端点
 * @param {Object} data - 请求体数据
 * @returns {Promise<Object>} 响应数据
 */
export const put = (endpoint, data = {}) => {
  return request(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

/**
 * DELETE 请求
 * @param {string} endpoint - API 端点
 * @returns {Promise<Object>} 响应数据
 */
export const del = (endpoint) => {
  return request(endpoint, { method: 'DELETE' })
}

// ============ 兼容旧代码的别名 ============

/**
 * @deprecated 使用 get 代替
 */
export const getWithAuth = get

/**
 * @deprecated 使用 post 代替
 */
export const postWithAuth = post

/**
 * @deprecated 使用 put 代替
 */
export const putWithAuth = put

/**
 * @deprecated 使用 del 代替
 */
export const deleteWithAuth = del

/**
 * GET 请求获取配置选项（tag, ai 等）
 * @param {string} types - 选项类型，多个用逗号分隔，如 "tag,ai"
 * @returns {Promise<Object>} 响应数据
 */
export const getOptions = (types) => {
  return get(`/market/options?types=${types}`)
}

/**
 * POST 请求分页查询市场列表（使用表单传参）
 * @param {Object} params - 查询参数
 * @param {number} params.num - 页码，从1开始，默认1
 * @param {number} params.size - 每页大小，默认6
 * @param {string} params.tagCode - 市场标签代码（可选）
 * @param {number} params.marketStatus - 市场状态，默认3（已发布）
 * @param {string} params.keyword - 搜索关键词（可选）
 * @returns {Promise<Object>} 响应数据
 */
export const getMarketList = (params = {}) => {
  const defaultParams = {
    num: 1,
    size: 6
  }
  const finalParams = { ...defaultParams, ...params }

  // 使用表单传参方式
  const formData = new URLSearchParams()
  Object.keys(finalParams).forEach(key => {
    if (finalParams[key] !== undefined && finalParams[key] !== null) {
      formData.append(key, finalParams[key])
    }
  })

  return request('/market/findList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData.toString()
  })
}

/**
 * POST 请求分页查询用户参与的市场列表（使用表单传参）
 * @param {Object} params - 查询参数
 * @param {number} params.userId - 用户ID
 * @param {number} params.num - 页码，从1开始，默认1
 * @param {number} params.size - 每页大小，默认6
 * @returns {Promise<Object>} 响应数据
 */
export const getMyMarkets = (params = {}) => {
  const defaultParams = {
    num: 1,
    size: 6
  }
  const finalParams = { ...defaultParams, ...params }

  // 使用表单传参方式
  const formData = new URLSearchParams()
  Object.keys(finalParams).forEach(key => {
    if (finalParams[key] !== undefined && finalParams[key] !== null) {
      formData.append(key, finalParams[key])
    }
  })

  return request('/market/findMyList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData.toString()
  })
}

/**
 * POST 请求创建市场（使用 form 表单）
 * @param {Object} data - 市场数据
 * @param {string} data.title - 市场标题
 * @param {string} data.description - 市场描述
 * @param {number} data.category - 市场分类（数字code）
 * @param {string} data.closeTime - 市场截止时间（ISO 8601格式）
 * @param {string} data.baseLiquidity - 基础流动性金额
 * @param {string} data.oracleSource - 预言机来源
 * @param {string} data.aiModel - AI模型（逗号分隔的数字code）
 * @param {string} data.tags - 标签（逗号分隔的数字code）
 * @param {number} userId - 用户ID
 * @returns {Promise<Object>} 响应数据
 */
export const createMarket = (data, userId) => {
  // 将对象转为表单格式
  const formData = new URLSearchParams()
  Object.keys(data).forEach(key => {
    if (data[key] !== undefined && data[key] !== null) {
      formData.append(key, data[key])
    }
  })

  return request(`/market/add?userId=${userId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData.toString()
  })
}

/**
 * POST 请求查询管理员审核市场列表（使用 form 表单）
 * @param {Object} params - 查询参数
 * @param {number} params.marketStatus - 市场状态：0-待审核，1-已拒绝，2-审核通过，3-已发布
 * @param {number} params.num - 页码，从1开始，默认1
 * @param {number} params.size - 每页大小，默认10
 * @returns {Promise<Object>} 响应数据
 */
export const getAdminApproveList = (params = {}) => {
  const defaultParams = {
    num: 1,
    size: 10
  }
  const finalParams = { ...defaultParams, ...params }

  // 将对象转为表单格式
  const formData = new URLSearchParams()
  Object.keys(finalParams).forEach(key => {
    if (finalParams[key] !== undefined && finalParams[key] !== null) {
      formData.append(key, finalParams[key])
    }
  })

  return request('/admin/approveList', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData.toString()
  })
}

/**
 * GET 请求审批市场
 * @param {string} marketId - 市场ID
 * @param {number} status - 目标状态：1-已拒绝，2-初审通过，3-终审通过
 * @returns {Promise<Object>} 响应数据
 */
export const approveMarket = (marketId, status) => {
  return request(`/admin/approve?marketId=${encodeURIComponent(marketId)}&status=${status}`, {
    method: 'GET'
  })
}

/**
 * GET 请求开始发布市场
 *
 * 调用此接口前,前端应该:
 * 1. 检查用户钱包是否连接
 * 2. 检查USDC余额是否充足
 *
 * 后端会校验:
 * 1. 市场创建人是该用户
 * 2. 市场状态是终审通过(status=3)
 * 3. 如果校验通过,将状态改为"发布中"(status=4)
 * 4. 后端开始监听工厂合约的 MarketCreated 事件
 *
 * @param {string} userId - 用户ID
 * @param {string} marketId - 市场ID
 * @returns {Promise<Object>} 响应数据
 */
export const openMarket = (userId, marketId) => {
  return request(`/market/opening?userId=${encodeURIComponent(userId)}&marketId=${encodeURIComponent(marketId)}`, {
    method: 'GET'
  })
}

/**
 * POST 请求通知后端：开始创建市场合约（开放市场后调用）
 *
 * 🔒 安全方案：
 * - 前端调用合约创建市场
 * - 立即通知后端（只发送 txHash）
 * - 后端将状态改为"开放中"（status = 6，deploying）
 * - 后端定时任务每30秒查询链上事件
 * - 找到 MarketCreated 事件后，更新为"已发布"（status = 4）
 *
 * @param {string} marketId - 市场ID
 * @param {Object} data - 交易数据
 * @param {string} data.txHash - 交易哈希
 * @param {string} data.onChainMarketId - 链上市场ID（供后端参考）
 * @returns {Promise<Object>} 响应数据
 */
export const notifyMarketDeploying = (marketId, data) => {
  // 将对象转为表单格式
  const formData = new URLSearchParams()
  Object.keys(data).forEach(key => {
    if (data[key] !== undefined && data[key] !== null) {
      formData.append(key, data[key])
    }
  })

  return request(`/market/deploying?marketId=${encodeURIComponent(marketId)}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData.toString()
  })
}

/**
 * GET 请求获取管理员统计数据
 * @returns {Promise<Object>} 响应数据，包含 totalMarkets, activeMarkets, pendingReview, totalLiquidity
 */
export const getAdminStatistics = () => {
  return request('/admin/sum', {
    method: 'GET'
  })
}

/**
 * POST 请求添加钱包到用户账号
 * @param {Object} data - 添加钱包数据
 * @param {number} data.userId - 用户ID
 * @param {string} data.walletAddress - 钱包地址
 * @param {number} data.chainId - 链ID
 * @param {string} data.walletType - 钱包类型（可选）
 * @param {string} data.signature - 签名
 * @returns {Promise<Object>} 响应数据
 */
export const addWallet = (data) => {
  return post('/user/wallet/add', data)
}

/**
 * POST 请求获取 Nonce
 * @param {Object} data - 请求数据
 * @param {string} data.walletAddress - 钱包地址
 * @param {number} data.chainId - 链ID
 * @returns {Promise<Object>} 响应数据，包含 nonce, expiredAt, message
 */
export const getNonce = (data) => {
  return post('/user/nonce', data)
}

/**
 * POST 请求钱包登录
 * @param {Object} data - 登录数据
 * @param {string} data.walletAddress - 钱包地址
 * @param {number} data.chainId - 链ID
 * @param {string} data.signature - 签名
 * @param {string} data.walletType - 钱包类型（可选）
 * @returns {Promise<Object>} 响应数据，包含 userId, token, uid, nickname, avatar, walletAddress
 */
export const walletLogin = (data) => {
  return post('/user/login', data)
}
