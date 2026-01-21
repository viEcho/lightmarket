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
      return user.accessToken || null  // 修复：后端返回的是 accessToken
    } catch (err) {
      console.error('Error parsing userInfo:', err)
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

  try {
    const response = await fetch(url, config)

    // 处理 HTTP 错误状态
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    const data = await response.json()

    // 处理业务错误码（后端成功 code 是 1000，或者 success 为 true）
    if (!data.success || data.code !== 1000) {
      throw new Error(data.message || 'Request failed')
    }

    return data
  } catch (error) {
    console.error('API request error:', error)
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
