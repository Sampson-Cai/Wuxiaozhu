const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const Vue = require('vue')

const root = path.resolve(__dirname, '..')
const FORMS = ['pages/purchase/form.vue', 'pages/purchase/index.vue', 'pages/purchase/detail.vue']

function loadComponent(file, globals = {}) {
  const source = fs.readFileSync(path.join(root, file), 'utf8')
  const script = source.match(/<script>([\s\S]*?)<\/script>/)[1]
    .replace(/import\s*\{[\s\S]*?\}\s*from\s*['"][^'"]+['"]/g, '')
    .replace('export default', 'module.exports =')
  const calls = {
    api: [], toast: [], nav: [], errors: [], modal: 0, modalOpts: null, loading: 0, closeLoading: 0
  }
  const context = {
    module: { exports: {} },
    setTimeout,
    clearTimeout,
    console,
    uni: {
      showModal: opts => { calls.modal++; calls.modalOpts = opts; if (opts.success) opts.success({ confirm: true, content: '' }) },
      showToast: () => {},
      setNavigationBarTitle: () => {}
    },
    productList: () => Promise.resolve({ rows: [
      { productId: 11, productCode: 'M-SOCK-005', productName: '套筒组 1"方驱 24-85mm', spec: '24-85mm', unit: '套', refPrice: 68 },
      { productId: 12, productCode: 'M-SPROCKET-010', productName: '链轮 16B-1 35齿', spec: '16B-1', unit: '个', refPrice: 118 }
    ] }),
    supplierList: () => Promise.resolve({ rows: [{ supplierId: 3, supplierName: '华东五金供应商' }] }),
    purchaseDetail: () => Promise.resolve({ data: {
      orderId: 7, supplierId: 3, supplierName: '华东五金供应商', status: '0', remark: '原备注',
      items: [{ itemId: 1, productId: 11, productName: '套筒组 1"方驱 24-85mm', spec: '24-85mm', unit: '套', qty: 2, price: 68 }]
    } }),
    purchaseCreate: payload => { calls.api.push(['create', payload]); return Promise.resolve({ code: 200, orderId: 99 }) },
    purchaseUpdate: (id, payload) => { calls.api.push(['update', id, payload]); return Promise.resolve({ code: 200 }) },
    purchaseDelete: id => { calls.api.push(['delete', id]); return Promise.resolve({ code: 200 }) },
    purchaseSubmit: () => Promise.resolve({ code: 200 }),
    purchaseApprove: () => Promise.resolve({ code: 200 }),
    purchaseReject: () => Promise.resolve({ code: 200 }),
    purchaseReceive: () => Promise.resolve({ code: 200 }),
    buildExportUrl: () => '',
    ...globals
  }
  vm.runInNewContext(script, context)
  const options = context.module.exports
  const instance = new Vue(options)
  // uni-app 页面生命周期在 options 顶层，Vue 不会注册成实例方法，手动绑定便于测试
  for (const lifecycle of ['onLoad', 'onShow', 'onUnload']) {
    if (typeof options[lifecycle] === 'function' && typeof instance[lifecycle] !== 'function') {
      instance[lifecycle] = options[lifecycle].bind(instance)
    }
  }
  instance.$modal = {
    msgSuccess: m => calls.toast.push(m),
    msgError: m => calls.errors.push(m)
  }
  instance.$tab = {
    navigateTo: u => calls.nav.push(['to', u]),
    redirectTo: u => calls.nav.push(['redirect', u]),
    navigateBack: () => calls.nav.push(['back'])
  }
  // 用响应式 state，保证测试中修改权限能让 computed 重新求值
  instance.$store = { state: Vue.observable({ user: { permissions: ['*:*:*'] } }) }
  return { instance, calls, source, options }
}

const cases = []
const test = (name, run) => cases.push({ name, run })
const tick = () => new Promise(resolve => setImmediate(resolve))
const input = value => ({ detail: { value: String(value) } })

test('pages.json 注册了采购单表单页', () => {
  const cfg = JSON.parse(fs.readFileSync(path.join(root, 'pages.json'), 'utf8'))
  const pkg = cfg.subPackages.find(s => s.root === 'pages/purchase')
  assert.ok(pkg)
  const paths = pkg.pages.map(p => typeof p === 'string' ? p : p.path)
  assert.ok(paths.includes('form'), 'form 未注册')
  assert.ok(paths.includes('index') && paths.includes('detail'))
})

test('采购列表页有新建入口且受权限控制', () => {
  const { instance, calls, source } = loadComponent('pages/purchase/index.vue')
  assert.equal(instance.canCreate, true)
  instance.createOrder()
  assert.equal(JSON.stringify(calls.nav[0]), JSON.stringify(['to', '/pages/purchase/form']))
  instance.$store.state.user.permissions = []
  assert.equal(instance.canCreate, false)
  assert.match(source, /class="fab"/)
  assert.match(source, /新建/)
})

test('新建页：加载档案、搜索过滤、添加明细', async () => {
  const { instance } = loadComponent('pages/purchase/form.vue')
  instance.onLoad({})
  await tick(); await tick()
  assert.equal(instance.suppliers.length, 1)
  assert.equal(instance.products.length, 2)
  assert.equal(instance.filteredProducts.length, 2)
  instance.openPicker()
  assert.equal(instance.showPicker, true)
  instance.pickerKeyword = '链轮'
  assert.equal(instance.filteredProducts.length, 1)
  assert.equal(instance.filteredProducts[0].productId, 12)
  instance.chooseProduct(instance.filteredProducts[0])
  assert.equal(instance.showPicker, false)
  assert.equal(instance.items.length, 1)
  assert.equal(instance.items[0].qty, '1')
  assert.equal(instance.items[0].price, '118')
  assert.equal(instance.totalText, '118.00')
  assert.match(instance.itemSub(instance.items[0]), /链轮|SPROCKET/)
})

test('新建页：数量与单价编辑自动更新合计、移除明细', () => {
  const { instance } = loadComponent('pages/purchase/form.vue')
  instance.items = [
    { productId: 11, productName: '套筒组', productCode: 'M-SOCK-005', spec: '', unit: '套', qty: '1', price: '68' },
    { productId: 12, productName: '链轮', productCode: 'M-SPROCKET-010', spec: '', unit: '个', qty: '1', price: '118' }
  ]
  assert.equal(instance.totalText, '186.00')
  instance.onItemInput(0, 'qty', input(3))
  assert.equal(instance.totalText, '322.00')
  instance.onItemInput(1, 'price', input(100))
  assert.equal(instance.totalText, '304.00')
  instance.removeItem(0)
  assert.equal(instance.items.length, 1)
  assert.equal(instance.totalText, '100.00')
})

test('新建页：校验供应商、明细、数量、负单价', () => {
  const { instance, calls } = loadComponent('pages/purchase/form.vue')
  instance.save(); assert.equal(calls.errors.pop(), '请选择供应商')
  instance.form.supplierId = 3
  instance.save(); assert.equal(calls.errors.pop(), '请添加至少一条明细')
  instance.items = [{ productId: 11, qty: '0', price: '68' }]
  instance.save(); assert.equal(calls.errors.pop(), '数量必须大于 0')
  instance.items = [{ productId: 11, qty: '2', price: '-1' }]
  instance.save(); assert.equal(calls.errors.pop(), '单价不能为负数')
  assert.equal(calls.api.length, 0)
})

test('新建页：保存草稿调用 create，成功后跳详情', async () => {
  const { instance, calls } = loadComponent('pages/purchase/form.vue')
  instance.form.supplierId = 3
  instance.form.remark = '测试'
  instance.items = [{ productId: 11, qty: '2', price: '68' }]
  instance.save()
  assert.equal(calls.api[0][0], 'create')
  assert.equal(calls.api[0][1].supplierId, 3)
  assert.equal(JSON.stringify(calls.api[0][1].items), JSON.stringify([{ productId: 11, qty: 2, price: 68 }]))
  assert.equal(instance.saving, true)
  await tick()
  assert.equal(calls.toast[0], '已创建草稿')
  assert.equal(instance.saving, false)
  await new Promise(r => setTimeout(r, 700))
  assert.equal(JSON.stringify(calls.nav[0]), JSON.stringify(['redirect', '/pages/purchase/detail?orderId=99']))
})

test('新建页：单价留空传 null，由后端取参考价', async () => {
  const { instance, calls } = loadComponent('pages/purchase/form.vue')
  instance.form.supplierId = 3
  instance.items = [{ productId: 11, qty: '2', price: '' }]
  instance.save()
  assert.equal(calls.api[0][1].items[0].price, null)
  await tick()
})

test('编辑页：回填草稿明细、调用 update、返回详情', async () => {
  const { instance, calls } = loadComponent('pages/purchase/form.vue')
  instance.onLoad({ orderId: '7' })
  await tick(); await tick(); await tick()
  assert.equal(instance.isEdit, true)
  assert.equal(instance.form.supplierId, 3)
  assert.equal(instance.form.remark, '原备注')
  assert.equal(instance.items.length, 1)
  assert.equal(instance.items[0].qty, '2')
  assert.equal(instance.totalText, '136.00')
  instance.save()
  assert.equal(calls.api[0][0], 'update')
  assert.equal(calls.api[0][1], '7')
  await tick()
  assert.equal(calls.toast[0], '保存成功')
  await new Promise(r => setTimeout(r, 700))
  assert.equal(JSON.stringify(calls.nav[0]), JSON.stringify(['back']))
})

test('详情页：草稿显示编辑/删除入口，非草稿隐藏', () => {
  const { instance, source } = loadComponent('pages/purchase/detail.vue')
  instance.order = { orderId: 7, status: '0', items: [] }
  assert.equal(instance.canEditOrder, true)
  assert.match(source, /编辑采购单/)
  assert.match(source, /删除草稿/)
  instance.order.status = '1'
  assert.equal(instance.canEditOrder, false)
  instance.order.status = '3'
  assert.equal(instance.canEditOrder, true)
  instance.$store.state.user.permissions = []
  assert.equal(instance.canEditOrder, false)
})

test('详情页：编辑跳表单，删除草稿需确认并返回列表', async () => {
  const { instance, calls } = loadComponent('pages/purchase/detail.vue')
  instance.orderId = 7
  instance.order = { orderId: 7, status: '0', items: [] }
  instance.editOrder()
  assert.equal(JSON.stringify(calls.nav[0]), JSON.stringify(['to', '/pages/purchase/form?orderId=7']))
  await instance.deleteOrder()
  assert.equal(calls.modal, 1)
  assert.equal(calls.api[0][0], 'delete')
  assert.equal(calls.api[0][1], 7)
  assert.equal(calls.toast[0], '已删除')
  assert.equal(instance.busyAction, '')
  await new Promise(r => setTimeout(r, 700))
  assert.equal(JSON.stringify(calls.nav[1]), JSON.stringify(['back']))
})

test('详情页：已通过/已完成的采购单不出现删除入口', () => {
  const { instance } = loadComponent('pages/purchase/detail.vue')
  instance.order = { orderId: 7, status: '2', items: [] }
  assert.equal(instance.canEditOrder, false)
  instance.order = { orderId: 7, status: '4', items: [] }
  assert.equal(instance.canEditOrder, false)
})

test('新建表单不含页脚备注式说明文案', () => {
  const source = fs.readFileSync(path.join(root, 'pages/purchase/form.vue'), 'utf8')
  assert.ok(!source.includes('ios-section-footer'), '不应出现页脚说明容器')
  assert.ok(!/导入新文档|计算口径|真机调试|资料由/.test(source))
})

test('原有审批交互回归未被破坏', async () => {
  const { instance, calls } = loadComponent('pages/purchase/detail.vue', {
    purchaseDetail: () => Promise.resolve({ data: { orderId: 7, status: '1', items: [] } })
  })
  instance.orderId = 7
  instance.order = { orderId: 7, status: '0', items: [] }
  instance.loading = false
  instance.loaded = true
  await instance.handleAction('submit')
  assert.equal(instance.order.status, '1')
  assert.equal(instance.actions.map(a => a.key).join(','), 'reject,approve')
  assert.equal(calls.toast[0], '已提交审批')
})

;(async () => {
  for (const { name, run } of cases) {
    await run()
    console.log('PASS ' + name)
  }
  console.log(`${cases.length}/${cases.length} purchase form tests passed (mock APIs; no production writes)`)
})().catch(error => { console.error(error); process.exitCode = 1 })
