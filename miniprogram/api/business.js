import request from '@/utils/request'
import { getBaseUrl } from '@/utils/server'
import { getToken } from '@/utils/auth'

/**
 * 物小助业务接口（/business/**）
 *
 * 返回体统一 { code, msg, rows, total, data }；
 * request 已处理 401/500 与 token，写接口自动带 X-Request-Id。
 */

/* ---------------- 首页 ---------------- */
export function dashboardSummary() {
  return request({ url: '/business/dashboard/summary', method: 'get' })
}

export function dashboardTrend(days = 30) {
  return request({ url: '/business/dashboard/trend', method: 'get', params: { days } })
}

/* ---------------- 单位 ---------------- */
export function getEnterprise() {
  return request({ url: '/business/enterprise', method: 'get' })
}

export function updateEnterprise(data) {
  return request({ url: '/business/enterprise', method: 'put', data })
}

/* ---------------- 分类 ---------------- */
export function categoryList() {
  return request({ url: '/business/category/list', method: 'get' })
}

export function categoryDetail(categoryId) {
  return request({ url: `/business/category/${categoryId}`, method: 'get' })
}

export function categoryAdd(data) {
  return request({ url: '/business/category', method: 'post', data })
}

export function categoryEdit(data) {
  return request({ url: '/business/category', method: 'put', data })
}

export function categoryDelete(categoryId) {
  return request({ url: `/business/category/${categoryId}`, method: 'delete' })
}

/* ---------------- 商品 ---------------- */
export function productList(params) {
  return request({ url: '/business/product/list', method: 'get', params })
}

export function productDetail(productId) {
  return request({ url: `/business/product/${productId}`, method: 'get' })
}

export function productAdd(data) {
  return request({ url: '/business/product', method: 'post', data })
}

export function productEdit(data) {
  return request({ url: '/business/product', method: 'put', data })
}

export function productStatus(productId, status) {
  return request({ url: `/business/product/${productId}/status`, method: 'put', params: { status } })
}

export function productDelete(productId) {
  return request({ url: `/business/product/${productId}`, method: 'delete' })
}

/* ---------------- 供应商 ---------------- */
export function supplierList(params) {
  return request({ url: '/business/supplier/list', method: 'get', params })
}

export function supplierDetail(supplierId) {
  return request({ url: `/business/supplier/${supplierId}`, method: 'get' })
}

export function supplierAdd(data) {
  return request({ url: '/business/supplier', method: 'post', data })
}

export function supplierEdit(data) {
  return request({ url: '/business/supplier', method: 'put', data })
}

export function supplierStatus(supplierId, status) {
  return request({ url: `/business/supplier/${supplierId}/status`, method: 'put', params: { status } })
}

export function supplierDelete(supplierId) {
  return request({ url: `/business/supplier/${supplierId}`, method: 'delete' })
}

/* ---------------- 库存 ---------------- */
export function stockList(params) {
  return request({ url: '/business/stock/list', method: 'get', params })
}

export function stockRecords(params) {
  return request({ url: '/business/stock/records', method: 'get', params })
}

export function stockDetail(productId) {
  return request({ url: `/business/stock/${productId}`, method: 'get' })
}

export function stockInbound(data) {
  return request({ url: '/business/stock/inbound', method: 'post', data })
}

export function stockOutbound(data) {
  return request({ url: '/business/stock/outbound', method: 'post', data })
}

export function stockReverse(recordId, remark) {
  return request({ url: `/business/stock/records/${recordId}/reverse`, method: 'post', data: { remark } })
}

/* ---------------- 采购 ---------------- */
export function purchaseList(params) {
  return request({ url: '/business/purchase/list', method: 'get', params })
}

export function purchaseTodo(params) {
  return request({ url: '/business/purchase/todo', method: 'get', params })
}

export function purchaseDetail(orderId) {
  return request({ url: `/business/purchase/${orderId}`, method: 'get' })
}

export function purchaseCreate(data) {
  return request({ url: '/business/purchase', method: 'post', data })
}

export function purchaseUpdate(orderId, data) {
  return request({ url: `/business/purchase/${orderId}`, method: 'put', data })
}

export function purchaseDelete(orderId) {
  return request({ url: `/business/purchase/${orderId}`, method: 'delete' })
}

export function purchaseSubmit(orderId) {
  return request({ url: `/business/purchase/${orderId}/submit`, method: 'post' })
}

export function purchaseApprove(orderId, opinion) {
  return request({ url: `/business/purchase/${orderId}/approve`, method: 'post', data: { opinion } })
}

export function purchaseReject(orderId, opinion) {
  return request({ url: `/business/purchase/${orderId}/reject`, method: 'post', data: { opinion } })
}

export function purchaseCancel(orderId, reason) {
  return request({ url: `/business/purchase/${orderId}/cancel`, method: 'post', data: { reason } })
}

export function purchaseReceive(orderId, data) {
  return request({ url: `/business/purchase/${orderId}/receive`, method: 'post', data: data || {} })
}

/* ---------------- 收货 ---------------- */
export function receiptList(params) {
  return request({ url: '/business/receipt/list', method: 'get', params })
}

export function receiptDetail(receiptId) {
  return request({ url: `/business/receipt/${receiptId}`, method: 'get' })
}

/* ---------------- 应付 ---------------- */
export function payableList(params) {
  return request({ url: '/business/payable/list', method: 'get', params })
}

export function payableDetail(payableId) {
  return request({ url: `/business/payable/${payableId}`, method: 'get' })
}

export function payablePay(payableId, data) {
  return request({ url: `/business/payable/${payableId}/payment`, method: 'post', data })
}

/* ---------------- 补货 ---------------- */
export function replenishSuggestions(params) {
  return request({ url: '/business/replenish/suggestions', method: 'get', params })
}

export function replenishParam(productId) {
  return request({ url: `/business/replenish/param/${productId}`, method: 'get' })
}

export function saveReplenishParam(productId, data) {
  return request({ url: `/business/replenish/param/${productId}`, method: 'put', data })
}

export function replenishToPurchase(suggestionId) {
  return request({ url: `/business/replenish/suggestion/${suggestionId}/to-purchase`, method: 'post' })
}

export function replenishIgnore(suggestionId) {
  return request({ url: `/business/replenish/suggestion/${suggestionId}/status`, method: 'put', params: { status: 2 } })
}

export function replenishCalc() {
  return request({ url: '/business/replenish/calc', method: 'post' })
}

/* ---------------- 检索 ---------------- */
export function searchProduct(q, limit = 10) {
  return request({ url: '/business/search/product', method: 'get', params: { q, limit } })
}

/* ---------------- 账号 ---------------- */
export function userList(params) {
  return request({ url: '/business/user/list', method: 'get', params })
}

export function userDetail(userId) {
  return request({ url: `/business/user/${userId}`, method: 'get' })
}

export function userAdd(data) {
  return request({ url: '/business/user', method: 'post', data })
}

export function userEdit(userId, data) {
  return request({ url: `/business/user/${userId}`, method: 'put', data })
}

export function userResetPwd(userId, password) {
  return request({ url: `/business/user/${userId}/password`, method: 'put', data: { password } })
}

export function userStatus(userId, status) {
  return request({ url: `/business/user/${userId}/status`, method: 'put', params: { status } })
}

export function userAssignRoles(userId, roleIds) {
  return request({ url: `/business/user/${userId}/roles`, method: 'put', data: { roleIds } })
}

export function updateOwnPassword(oldPassword, newPassword) {
  return request({
    url: '/business/user/password',
    method: 'put',
    data: { oldPassword, newPassword }
  })
}

/* ---------------- 角色 ---------------- */
export function roleList() {
  return request({ url: '/business/role/list', method: 'get' })
}

export function roleDetail(roleId) {
  return request({ url: `/business/role/${roleId}`, method: 'get' })
}

export function roleMenus() {
  return request({ url: '/business/role/menus', method: 'get' })
}

export function roleAdd(data) {
  return request({ url: '/business/role', method: 'post', data })
}

export function roleEdit(data) {
  return request({ url: '/business/role', method: 'put', data })
}

export function roleDelete(roleId) {
  return request({ url: `/business/role/${roleId}`, method: 'delete' })
}

/* ---------------- 日志 ---------------- */
export function operLogList(params) {
  return request({ url: '/business/log/list', method: 'get', params })
}

export function operLogDetail(operId) {
  return request({ url: `/business/log/${operId}`, method: 'get' })
}

/* ---------------- AI 助手（RAG） ---------------- */
export function aiStatus() {
  return request({ url: '/business/ai/status', method: 'get' })
}

export function aiChat(sessionId, question) {
  return request({ url: '/business/ai/chat', method: 'post', data: { sessionId, question }, silent: true })
}

export function aiSessions(limit = 20) {
  return request({ url: '/business/ai/sessions', method: 'get', params: { limit } })
}

export function aiMessages(sessionId, limit = 50) {
  return request({ url: `/business/ai/sessions/${sessionId}/messages`, method: 'get', params: { limit } })
}

export function aiKbDocuments() {
  return request({ url: '/business/ai/kb/documents', method: 'get' })
}

export function aiKbImport(path, docType) {
  return request({ url: '/business/ai/kb/import', method: 'post', data: { path, docType } })
}

export function aiKbReindex(documentId) {
  return request({ url: `/business/ai/kb/${documentId}/reindex`, method: 'post' })
}

export function aiKbDelete(documentId) {
  return request({ url: `/business/ai/kb/${documentId}`, method: 'delete' })
}

/* ---------------- 导出 / 打印 ---------------- */
/**
 * 小程序下载文件不能带自定义请求头，令牌走 ?token=
 */
export function buildExportUrl(type, orderId) {
  const token = getToken()
  const host = getBaseUrl()
  if (type === 'pdf') {
    return `${host}/business/purchase/${orderId}/pdf?token=${token}`
  }
  return `${host}/business/export/${type}?token=${token}`
}
