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
 * @param {boolean} withToken - 是否需要携带 token
 * @returns {Promise<Object>} 响应数据
 */
const request = async (endpoint, options = {}, withToken = false) => {
  const url = `${API_BASE_URL}${endpoint}`
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  }

  // 如果需要 token，添加到请求头
  if (withToken) {
    const token = getToken()
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
  }

  const config = {
    ...options,
    headers
  }

  // 调试：打印 findList 接口的实际传参
  if (endpoint.includes('findList') && config.body) {
    console.log('[API Debug] findList 请求参数:', config.body)
    console.log('[API Debug] Content-Type:', headers['Content-Type'])
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
 * GET 请求（不需要 token）
 * @param {string} endpoint - API 端点
 * @param {Object} params - 查询参数
 * @returns {Promise<Object>} 响应数据
 */
export const get = (endpoint, params = {}) => {
  const queryString = new URLSearchParams(params).toString()
  const url = queryString ? `${endpoint}?${queryString}` : endpoint
  return request(url, { method: 'GET' }, false)
}

/**
 * GET 请求（需要 token）
 * @param {string} endpoint - API 端点
 * @param {Object} params - 查询参数
 * @returns {Promise<Object>} 响应数据
 */
export const getWithAuth = (endpoint, params = {}) => {
  const queryString = new URLSearchParams(params).toString()
  const url = queryString ? `${endpoint}?${queryString}` : endpoint
  return request(url, { method: 'GET' }, true)
}

/**
 * POST 请求（不需要 token）
 * @param {string} endpoint - API 端点
 * @param {Object} data - 请求体数据
 * @returns {Promise<Object>} 响应数据
 */
export const post = (endpoint, data = {}) => {
  return request(endpoint, {
    method: 'POST',
    body: JSON.stringify(data)
  }, false)
}

/**
 * POST 请求（需要 token）
 * @param {string} endpoint - API 端点
 * @param {Object} data - 请求体数据
 * @returns {Promise<Object>} 响应数据
 */
export const postWithAuth = (endpoint, data = {}) => {
  return request(endpoint, {
    method: 'POST',
    body: JSON.stringify(data)
  }, true)
}

/**
 * PUT 请求（需要 token）
 * @param {string} endpoint - API 端点
 * @param {Object} data - 请求体数据
 * @returns {Promise<Object>} 响应数据
 */
export const putWithAuth = (endpoint, data = {}) => {
  return request(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data)
  }, true)
}

/**
 * DELETE 请求（需要 token）
 * @param {string} endpoint - API 端点
 * @returns {Promise<Object>} 响应数据
 */
export const deleteWithAuth = (endpoint) => {
  return request(endpoint, { method: 'DELETE' }, true)
}

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
    size: 6,
    marketStatus: 3
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
  }, false)
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
  }, false)
}

/**
 * POST 请求创建市场（需要 token，使用 form 表单）
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
  }, true)
}
