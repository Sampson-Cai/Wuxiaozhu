const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const Vue = require('vue')
const root = path.resolve(__dirname, '..')
const source = fs.readFileSync(path.join(root, 'pages/purchase/detail.vue'), 'utf8')
const script = source.match(/<script>([\s\S]*?)<\/script>/)[1]
  .replace(/import\s*\{[\s\S]*?\}\s*from\s*['"]@\/api\/business['"]/, '')
  .replace('export default', 'module.exports =')
function deferred() {
  let resolve, reject
  const promise = new Promise((res, rej) => { resolve = res; reject = rej })
  return { promise, resolve, reject }
}
function page(overrides = {}, modalResult = { confirm: true, content: '' }) {
  const calls = { modal: 0, toast: [], submit: 0, approve: 0, reject: 0, receive: 0, detail: 0 }
  let currentStatus = '0'
  const context = {
    module: { exports: {} },
    uni: { showModal: options => { calls.modal++; options.success(modalResult) } },
    purchaseDetail: () => { calls.detail++; return Promise.resolve({ data: { orderId: 7, status: currentStatus, items: [] } }) },
    purchaseSubmit: () => { calls.submit++; currentStatus = '1'; return Promise.resolve({ code: 200 }) },
    purchaseApprove: () => { calls.approve++; currentStatus = '2'; return Promise.resolve({ code: 200 }) },
    purchaseReject: () => { calls.reject++; currentStatus = '3'; return Promise.resolve({ code: 200 }) },
    purchaseReceive: () => { calls.receive++; currentStatus = '4'; return Promise.resolve({ code: 200 }) },
    buildExportUrl: () => '',
    ...overrides
  }
  vm.runInNewContext(script, context)
  const options = context.module.exports
  const instance = new Vue(options)
  instance.orderId = 7
  instance.order = { orderId: 7, status: '0', items: [] }
  instance.loading = false
  instance.$modal = {
    msgSuccess: message => calls.toast.push(message),
    msgError: message => calls.toast.push(message)
  }
  return { instance, calls, options }
}
const cases = []
const test = (name, run) => cases.push({ name, run })

test('旧产物确有错误的循环函数渲染引用，新模板改为按 key 分派', async () => {
  const oldPath = '/opt/qixiaozhu/backups/purchase-ui-20261106-1530/detail-before.js'
  if (fs.existsSync(oldPath)) assert.match(fs.readFileSync(oldPath, 'utf8'), /_vm\.e0\s*=\s*_vm\.action\.handler/)
  assert.doesNotMatch(source, /action\.handler|handler:\s*this\./)
  assert.match(source, /@click="handleAction\(action\.key\)"/)
  assert.match(source, /:disabled=/)
  assert.match(source, /:loading="busyAction === action\.key"/)
})
test('提交成功：草稿变待审批，提交按钮消失，审批按钮出现', async () => {
  const { instance: p, calls } = page()
  await p.handleAction('submit')
  assert.equal(calls.submit, 1)
  assert.equal(p.order.status, '1')
  assert.equal(p.statusText(p.order.status), '待审批')
  assert.equal(p.actions.map(a => a.key).join(','), 'reject,approve')
  assert.equal(p.operationMessage, '已提交审批')
  assert.equal(calls.toast[0], '已提交审批')
  assert.equal(p.busyAction, '')
})
test('快速重复点击：只弹一个确认框且只提交一次', async () => {
  const write = deferred(), modal = deferred()
  let submissions = 0
  const { instance: p } = page({
    uni: { showModal: options => modal.promise.then(options.success) },
    purchaseSubmit: () => { submissions++; return write.promise },
    purchaseDetail: () => Promise.resolve({ data: { orderId: 7, status: '1' } })
  })
  const first = p.handleAction('submit')
  assert.equal(p.confirming, true)
  await p.handleAction('submit')
  modal.resolve({ confirm: true })
  await new Promise(resolve => setImmediate(resolve))
  assert.equal(p.busyAction, 'submit')
  assert.equal(p.busyLabel('submit'), '提交中…')
  await p.handleAction('submit')
  assert.equal(submissions, 1)
  write.resolve({ code: 200 })
  await first
})
test('取消确认：不提交、不改变状态', async () => {
  const { instance: p, calls } = page({}, { confirm: false })
  await p.handleAction('submit')
  assert.equal(calls.submit, 0)
  assert.equal(p.order.status, '0')
  assert.equal(p.confirming, false)
})
test('提交成功但刷新失败：保留待审批及成功结果，禁止再次写入', async () => {
  const { instance: p } = page({ purchaseDetail: () => Promise.reject(new Error('offline')) })
  await p.handleAction('submit')
  assert.equal(p.order.status, '1')
  assert.equal(p.operationMessage, '已提交审批')
  assert.equal(p.refreshFailed, true)
  assert.equal(p.canExecute('approve'), false)
})
test('写请求失败且服务端仍是草稿：不虚报成功，释放锁', async () => {
  const { instance: p, calls } = page({ purchaseSubmit: () => Promise.reject(new Error('denied')) })
  await p.handleAction('submit')
  assert.equal(p.order.status, '0')
  assert.equal(p.operationMessage, '')
  assert.equal(calls.toast.length, 0)
  assert.equal(p.busyAction, '')
})
test('提交超时但服务端已完成：重读真实待审批状态', async () => {
  const { instance: p } = page({
    purchaseSubmit: () => Promise.reject(new Error('timeout')),
    purchaseDetail: () => Promise.resolve({ data: { orderId: 7, status: '1' } })
  })
  await p.handleAction('submit')
  assert.equal(p.order.status, '1')
  assert.equal(p.canExecute('submit'), false)
})
test('审批、驳回、收货各自调用正确接口并切换按钮', async () => {
  const approved = page(); approved.instance.order.status = '1'
  await approved.instance.handleAction('approve')
  assert.equal(approved.calls.approve, 1)
  assert.equal(approved.instance.order.status, '2')
  assert.equal(approved.instance.actions[0].key, 'receive')
  assert.equal(approved.instance.canPrint, true)
  await approved.instance.handleAction('receive')
  assert.equal(approved.calls.receive, 1)
  assert.equal(approved.instance.order.status, '4')
  assert.equal(approved.instance.actions.length, 0)
  const rejected = page({}, { confirm: true, content: '原因' }); rejected.instance.order.status = '1'
  await rejected.instance.handleAction('reject')
  assert.equal(rejected.calls.reject, 1)
  assert.equal(rejected.instance.order.status, '3')
  assert.equal(rejected.instance.actions[0].key, 'submit')
})
test('空白驳回理由不提交，数字状态正规化为字符串', async () => {
  const { instance: p, calls } = page({ purchaseDetail: () => Promise.resolve({ data: { orderId: 7, status: 1 } }) }, { confirm: true, content: '  ' })
  await p.load()
  assert.equal(p.order.status, '1')
  await p.handleAction('reject')
  assert.equal(calls.reject, 0)
  assert.equal(calls.toast[0], '驳回必须填写原因')
})
test('较早详情晚返回：不能覆盖新的状态', async () => {
  const old = deferred(); let reads = 0
  const { instance: p } = page({ purchaseDetail: () => ++reads === 1 ? old.promise : Promise.resolve({ data: { orderId: 7, status: '2' } }) })
  const stale = p.load()
  await p.load()
  old.resolve({ data: { orderId: 7, status: '0' } })
  await stale
  assert.equal(p.order.status, '2')
})
test('页面已离开：写请求完成后不追加刷新或弹提示', async () => {
  const write = deferred()
  const { instance: p, calls, options } = page({ purchaseSubmit: () => write.promise })
  const submitted = p.handleAction('submit')
  await new Promise(resolve => setImmediate(resolve))
  options.onUnload.call(p)
  write.resolve({ code: 200 })
  await submitted
  assert.equal(calls.detail, 0)
  assert.equal(calls.toast.length, 0)
  assert.equal(p.canExecute('submit'), false)
})
test('返回详情会重读，列表返回时已有刷新生命周期', async () => {
  const { instance: p, calls, options } = page()
  p.loaded = true
  options.onShow.call(p)
  await Promise.resolve(); await Promise.resolve()
  assert.equal(calls.detail, 1)
  const list = fs.readFileSync(path.join(root, 'pages/purchase/index.vue'), 'utf8')
  assert.match(list, /onShow\(\)[\s\S]*?if \(this\.loaded\)[\s\S]*?this\.reload\(\)/)
})

;(async () => {
  for (const { name, run } of cases) {
    await run()
    console.log('PASS ' + name)
  }
  console.log(`${cases.length}/${cases.length} tests passed (mock APIs; no production writes)`)
})().catch(error => { console.error(error); process.exitCode = 1 })
