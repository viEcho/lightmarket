/**
 * 测试 API 请求
 * 用于调试 tagCode 参数问题
 */
import { getMarketList } from './api'

export async function testTagCodeRequest() {
  // 直接构造请求体
  const testBody = {
    num: 1,
    size: 6,
    marketStatus: 3,
    tagCode: "5"
  }

  try {
    // 使用 fetch 直接发送
    const response = await fetch('http://localhost:9999/api/market/findList', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(testBody)
    })

    const data = await response.json()
    return data
  } catch (error) {
    throw error
  }
}

/**
 * 测试不同的参数名
 */
export async function testDifferentParamNames() {
  const paramNames = ['tagCode', 'tag', 'tag_code', 'tagcode']

  for (const paramName of paramNames) {
    const testBody = {
      num: 1,
      size: 6,
      marketStatus: 3
    }
    testBody[paramName] = "5"

    try {
      const response = await fetch('http://localhost:9999/api/market/findList', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(testBody)
      })
    } catch (error) {
      // Error handling
    }
  }
}
