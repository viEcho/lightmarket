/**
 * 市场状态映射常量
 * 统一管理所有市场状态相关的映射关系
 *
 * 市场状态说明：
 * 0 - 待审核
 * 1 - 已拒绝
 * 2 - 初审通过
 * 3 - 终审通过
 * 4 - 发布中
 * 5 - 已发布上链
 * 6 - 已关闭
 * 7 - 裁决中
 * 8 - 挑战中
 * 9 - 已终裁
 * 10 - 结算中
 * 99 - 已结算
 */

// 数字状态码 -> 中文描述
export const MARKET_STATUS_TEXT = {
  0: '待审核',
  1: '已拒绝',
  2: '初审通过',
  3: '终审通过',
  4: '发布中',
  5: '已发布',
  6: '已关闭',
  7: '裁决中',
  8: '挑战中',
  9: '已终裁',
  10: '结算中',
  99: '已结算'
}

// 数字状态码 -> CSS类名
export const MARKET_STATUS_CLASS = {
  0: 'status-pending',           // 待审核
  1: 'status-rejected',          // 已拒绝
  2: 'status-preliminary',       // 初审通过
  3: 'status-final',             // 终审通过
  4: 'status-deploying',         // 发布中
  5: 'status-active',            // 已发布
  6: 'status-closed',            // 已关闭
  7: 'status-arbitrating',       // 裁决中
  8: 'status-challenging',       // 挑战中
  9: 'status-final-arbitrated',  // 已终裁
  10: 'status-settling',         // 结算中
  99: 'status-settled'           // 已结算
}

// 简化版CSS类名(用于某些只需要简短类名的场景)
export const MARKET_STATUS_CLASS_SIMPLE = {
  0: 'pending',
  1: 'rejected',
  2: 'preliminary',
  3: 'final',
  4: 'deploying',
  5: 'active',
  6: 'closed',
  7: 'arbitrating',
  8: 'challenging',
  9: 'final-arbitrated',
  10: 'settling',
  99: 'settled'
}

// 数字状态码 -> 前端逻辑状态
export const MARKET_STATUS_LOGICAL = {
  0: 'pending',
  1: 'rejected',
  2: 'preliminary',
  3: 'final',
  4: 'deploying',
  5: 'active',
  6: 'closed',
  7: 'arbitrating',
  8: 'challenging',
  9: 'final-arbitrated',
  10: 'settling',
  99: 'settled'
}

// 审核阶段映射
export const MARKET_STAGE_MAP = {
  0: 'pre-review',     // 待审核 -> 预审
  2: 'pre-review',     // 初审通过 -> 预审阶段
  3: 'final-review',   // 终审通过 -> 终审阶段
  1: 'rejected',       // 已拒绝
  4: 'published',      // 已发布
  5: 'published'       // 已发布上链
}

/**
 * 根据市场状态码获取状态文本
 * @param {number} status - 市场状态码
 * @returns {string} 状态文本
 */
export function getMarketStatusText(status) {
  return MARKET_STATUS_TEXT[status] || `Status ${status}`
}

/**
 * 根据市场状态码获取CSS类名
 * @param {number} status - 市场状态码
 * @param {boolean} simple - 是否使用简化版类名
 * @returns {string} CSS类名
 */
export function getMarketStatusClass(status, simple = false) {
  const classMap = simple ? MARKET_STATUS_CLASS_SIMPLE : MARKET_STATUS_CLASS
  return classMap[status] || (simple ? 'pending' : 'status-pending')
}

/**
 * 根据市场状态码获取逻辑状态
 * @param {number} status - 市场状态码
 * @returns {string} 逻辑状态
 */
export function getMarketStatusLogical(status) {
  return MARKET_STATUS_LOGICAL[status] || 'pending'
}

/**
 * 根据市场状态码获取审核阶段
 * @param {number} status - 市场状态码
 * @returns {string} 审核阶段
 */
export function getMarketStage(status) {
  return MARKET_STAGE_MAP[status] || 'pre-review'
}

/**
 * 检查市场是否可以交易
 * @param {number} status - 市场状态码
 * @returns {boolean}
 */
export function isMarketTradeable(status) {
  return [4, 5].includes(status) // 发布中、已发布上链
}

/**
 * 检查市场是否已结算
 * @param {number} status - 市场状态码
 * @returns {boolean}
 */
export function isMarketSettled(status) {
  return status === 99
}

/**
 * 检查市场是否已关闭
 * @param {number} status - 市场状态码
 * @returns {boolean}
 */
export function isMarketClosed(status) {
  return status >= 6 && status <= 99
}
