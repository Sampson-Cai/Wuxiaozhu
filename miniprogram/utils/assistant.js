import {
  dashboardSummary,
  stockList,
  purchaseTodo,
  purchaseList,
  payableList,
  replenishSuggestions,
  searchProduct,
  supplierList,
  receiptList
} from '@/api/business'

/**
 * 物小助助手：把自然语言问题路由到真实业务接口，用返回数据作答。
 *
 * 说明：当前不接大模型（模型与 Embedding 参数未到位，见 readme 问题 005），
 * 这里做的是「意图识别 + 实时数据查询」，答案全部来自后端返回，不做任何编造。
 * 后续接入 LLM 时，只需把 ask() 换成模型调用，前端渲染结构保持不变。
 */

const STATUS_TEXT = {
  '0': '草稿',
  '1': '待审批',
  '2': '已通过',
  '3': '已驳回',
  '4': '已完成',
  '5': '已取消'
}

export const SUGGESTIONS = ['库存预警', '待我审批', '本月应付', '补货建议', '采购单进度']

function has(text, words) {
  for (let i = 0; i < words.length; i++) {
    if (text.indexOf(words[i]) > -1) return true
  }
  return false
}

function money(value) {
  const n = Number(value || 0)
  const fixed = n.toFixed(2)
  const parts = fixed.split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return '¥' + parts.join('.')
}

function num(value) {
  const n = Number(value || 0)
  return n % 1 === 0 ? String(n) : n.toFixed(2)
}

function shortDate(value) {
  if (!value) return ''
  return String(value).slice(0, 10)
}

// 本月第一天，格式 yyyy-MM-dd
function firstDayOfMonth() {
  const now = new Date()
  const month = now.getMonth() + 1
  return now.getFullYear() + '-' + (month < 10 ? '0' + month : month) + '-01'
}

// 去掉口语化修饰，留下可用于检索的关键词
const NOISE = [
  '帮我', '帮忙', '查一下', '查询', '查下', '查查', '看看', '看一下', '看一下',
  '现在', '当前', '目前', '还有', '多少', '几个', '是什么', '怎么样', '情况',
  '请问', '我想', '我要', '统计', '汇总', '列出', '显示', '一下', '有没有',
  '哪些', '哪个', '多少库存', '库存', '商品', '产品', '采购单', '采购', '订单',
  '单据', '应付', '账款', '供应商', '补货', '收货', '剩余', '还剩', '存量',
  '本月', '这个月', '上月', '进度', '状态', '记录', '列表', '最近', '全部', '所有'
]

function extractKeyword(text) {
  let kw = text
  NOISE.forEach(word => {
    kw = kw.split(word).join(' ')
  })
  return kw.replace(/[?？,，。！!、；;：:\s]+/g, ' ').trim()
}

function fail() {
  return {
    text: '这个我暂时没查到，可以换个说法，比如「库存预警」「待我审批」「本月应付」。'
  }
}

/* ---------------- 各意图 ---------------- */

function overview() {
  return dashboardSummary().then(res => {
    const lines = [
      '库存预警 ' + num(res.lowStock) + ' 个',
      '待审批 ' + num(res.pendingApproval) + ' 单',
      '7 天内到期应付 ' + num(res.payableDue) + ' 笔',
      '已逾期 ' + num(res.payableOverdue) + ' 笔'
    ]
    return {
      text: '当前概览：' + lines.join('，') + '。未结清应付合计 ' + money(res.unpaidAmount) + '。',
      link: { label: '查看工作台', path: '/pages/work/index' }
    }
  }).catch(fail)
}

function lowStock() {
  return stockList({ lowStockOnly: 1, pageNum: 1, pageSize: 5 }).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return {
        text: '当前没有低于安全库存的商品，库存水位正常。',
        link: { label: '去库存页', path: '/pages/stock/index' }
      }
    }
    return {
      text: '有 ' + (res.total || rows.length) + ' 个商品低于安全库存，建议优先补货：',
      items: rows.map(row => ({
        title: row.productName,
        sub: row.productCode + ' · 安全库存 ' + num(row.safetyStock),
        value: num(row.availableQty) + ' ' + (row.unit || ''),
        path: '/pages/stock/index?productId=' + row.productId
      })),
      link: { label: '查看全部预警', path: '/pages/stock/index?lowStockOnly=1' }
    }
  }).catch(fail)
}

function todo() {
  return purchaseTodo({ pageNum: 1, pageSize: 5 }).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return {
        text: '没有待你审批的采购单。',
        link: { label: '采购单列表', path: '/pages/purchase/index' }
      }
    }
    return {
      text: '有 ' + (res.total || rows.length) + ' 单待你审批：',
      items: rows.map(row => ({
        title: row.orderNo,
        sub: (row.supplierName || '未指定供应商') + ' · ' + shortDate(row.createTime),
        value: money(row.totalAmount),
        path: '/pages/purchase/detail?orderId=' + row.orderId
      })),
      link: { label: '去审批', path: '/pages/purchase/index?status=1' }
    }
  }).catch(fail)
}

function purchases(q) {
  let status = null
  if (has(q, ['草稿'])) status = '0'
  else if (has(q, ['待审批', '待审'])) status = '1'
  else if (has(q, ['已通过', '通过'])) status = '2'
  else if (has(q, ['驳回'])) status = '3'
  else if (has(q, ['完成', '已收'])) status = '4'
  else if (has(q, ['取消'])) status = '5'

  const keyword = extractKeyword(q)
  const params = { pageNum: 1, pageSize: 5 }
  if (status !== null) params.status = status
  if (keyword) params.keyword = keyword
  if (has(q, ['本月', '这个月'])) params.beginTime = firstDayOfMonth()

  return purchaseList(params).then(res => {
    const rows = res.rows || []
    const scope = status === null ? '采购单' : STATUS_TEXT[status] + '的采购单'
    if (!rows.length) {
      return {
        text: '没有找到符合条件的' + scope + '。',
        link: { label: '全部采购单', path: '/pages/purchase/index' }
      }
    }
    return {
      text: '共 ' + (res.total || rows.length) + ' 单' + scope + '：',
      items: rows.map(row => ({
        title: row.orderNo,
        sub: (row.supplierName || '未指定供应商') + ' · ' + (STATUS_TEXT[row.status] || '未知'),
        value: money(row.totalAmount),
        path: '/pages/purchase/detail?orderId=' + row.orderId
      })),
      link: { label: '查看全部', path: '/pages/purchase/index' }
    }
  }).catch(fail)
}

function payables(q) {
  const params = { pageNum: 1, pageSize: 5 }
  if (has(q, ['逾期'])) {
    params.overdueOnly = '1'
  } else if (has(q, ['到期', '即将'])) {
    params.dueWithinDays = 7
  } else if (has(q, ['未付', '未结清', '欠款', '应付'])) {
    params.unpaidOnly = '1'
  }
  return payableList(params).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return {
        text: '没有符合条件的应付账款。',
        link: { label: '应付账款', path: '/pages/payable/index' }
      }
    }
    const total = rows.reduce((sum, row) => sum + Number(row.remainingAmount || 0), 0)
    return {
      text: '共 ' + (res.total || rows.length) + ' 笔，本页待付合计 ' + money(total) + '：',
      items: rows.map(row => ({
        title: row.supplierName || row.payableNo,
        sub: row.payableNo + ' · 到期 ' + shortDate(row.dueDate),
        value: money(row.remainingAmount),
        path: '/pages/payable/detail?payableId=' + row.payableId
      })),
      link: { label: '去付款', path: '/pages/payable/index' }
    }
  }).catch(fail)
}

function replenish() {
  return replenishSuggestions({ pageNum: 1, pageSize: 5 }).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return {
        text: '暂无补货建议。可以到补货页点一次「重新计算」，系统会按销量与安全库存生成建议。',
        link: { label: '去补货建议', path: '/pages/replenish/index' }
      }
    }
    return {
      text: '有 ' + (res.total || rows.length) + ' 条补货建议：',
      items: rows.map(row => ({
        title: row.productName,
        sub: row.productCode + ' · 可用 ' + num(row.availableQty),
        value: '建议 ' + num(row.suggestQty) + ' ' + (row.unit || ''),
        path: '/pages/replenish/param?productId=' + row.productId
      })),
      link: { label: '查看建议', path: '/pages/replenish/index' }
    }
  }).catch(fail)
}

function receipts() {
  return receiptList({ pageNum: 1, pageSize: 5 }).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return {
        text: '还没有收货记录。',
        link: { label: '收货记录', path: '/pages/receipt/index' }
      }
    }
    return {
      text: '最近 ' + (res.total || rows.length) + ' 笔收货：',
      items: rows.map(row => ({
        title: row.receiptNo,
        sub: (row.orderNo || '') + ' · ' + shortDate(row.receiptTime || row.createTime),
        value: num(row.totalQty),
        path: '/pages/receipt/detail?receiptId=' + row.receiptId
      })),
      link: { label: '全部收货', path: '/pages/receipt/index' }
    }
  }).catch(fail)
}

function suppliers(q) {
  const keyword = extractKeyword(q || '')
  const params = { pageNum: 1, pageSize: 5 }
  if (keyword) params.keyword = keyword
  return supplierList(params).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return {
        text: '没有找到符合条件的供应商。',
        link: { label: '供应商', path: '/pages/supplier/index' }
      }
    }
    return {
      text: '共 ' + (res.total || rows.length) + ' 家供应商：',
      items: rows.map(row => ({
        title: row.supplierName,
        sub: (row.contact || '无联系人') + ' · 账期 ' + num(row.payTermDays) + ' 天',
        value: row.phone || '',
        path: '/pages/supplier/form?supplierId=' + row.supplierId
      })),
      link: { label: '供应商列表', path: '/pages/supplier/index' }
    }
  }).catch(fail)
}

function stockOf(q) {
  const keyword = extractKeyword(q)
  const params = { pageNum: 1, pageSize: 5 }
  if (keyword) params.keyword = keyword
  return stockList(params).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return {
        text: keyword ? '没有查到「' + keyword + '」的库存记录。' : '还没有库存数据。',
        link: { label: '库存页', path: '/pages/stock/index' }
      }
    }
    return {
      text: '查到 ' + (res.total || rows.length) + ' 条库存：',
      items: rows.map(row => ({
        title: row.productName,
        sub: row.productCode + ' · 可用 ' + num(row.availableQty) + ' · 在途 ' + num(row.inTransitQty),
        value: num(row.qty) + ' ' + (row.unit || ''),
        path: '/pages/stock/index?productId=' + row.productId
      })),
      link: { label: '库存流水', path: '/pages/stock/records' }
    }
  }).catch(fail)
}

function search(q, raw) {
  const keyword = extractKeyword(q) || raw
  if (!keyword) return Promise.resolve(fallback())
  return searchProduct(keyword, 5).then(res => {
    const rows = res.rows || []
    if (!rows.length) {
      return fallback(keyword)
    }
    return {
      text: '按「' + keyword + '」找到 ' + (res.total || rows.length) + ' 个商品：',
      items: rows.map(row => ({
        title: row.productName,
        sub: row.productCode + ' · ' + (row.categoryName || '未分类'),
        value: '库存 ' + num(row.stockQty),
        path: '/pages/product/form?productId=' + row.productId
      })),
      link: { label: '继续检索', path: '/pages/search/index?q=' + encodeURIComponent(keyword) }
    }
  }).catch(fail)
}

function fallback(keyword) {
  return {
    text: keyword
      ? '没找到和「' + keyword + '」相关的内容。可以试试「库存预警」「待我审批」「本月应付」。'
      : '我可以查库存、采购、应付、补货和商品，试试下面的快捷问题。',
    link: { label: '打开智能检索', path: '/pages/search/index' }
  }
}

/**
 * 提问入口
 * @param {String} question 用户输入
 * @returns {Promise<{text:String, items?:Array, link?:Object}>}
 */
export function ask(question) {
  const raw = (question || '').trim()
  if (!raw) return Promise.resolve(fallback())
  const q = raw.toLowerCase()

  if (has(q, ['概览', '总体', '大盘', '经营', '汇总', '概况', '总结', '情况怎么样'])) return overview()
  if (has(q, ['预警', '缺货', '库存不足', '低于安全', '安全库存', '断货'])) return lowStock()
  if (has(q, ['待审批', '待审', '待办', '要我审', '我审'])) return todo()
  if (has(q, ['应付', '欠款', '付款', '到期', '逾期', '账期', '还钱', '未结清'])) return payables(q)
  if (has(q, ['补货', '该补', '补多少'])) return replenish()
  if (has(q, ['收货', '到货'])) return receipts()
  if (has(q, ['供应商', '厂家'])) return suppliers(q)
  if (has(q, ['采购', '订单', '单据'])) return purchases(q)
  if (has(q, ['库存', '存量', '还剩', '剩余', '还有多少'])) return stockOf(q)
  return search(q, raw)
}

export default { ask, SUGGESTIONS }
