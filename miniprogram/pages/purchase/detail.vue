<template>
  <view class="ios-page detail" v-if="order">
    <view class="ios-card ios-card-pad head">
      <view class="ios-between">
        <view class="head-no">{{ order.orderNo }}</view>
        <view class="ios-badge" :class="statusBadge(order.status)">{{ statusText(order.status) }}</view>
      </view>
      <view class="head-amount">¥{{ money(order.totalAmount) }}</view>
      <view class="head-meta">{{ order.supplierName || '未指定供应商' }} · {{ order.createTime }}</view>
      <view v-if="order.remark" class="head-remark">备注：{{ order.remark }}</view>
      <view v-if="operationMessage" class="operation-result">
        <uni-icons type="checkmarkempty" size="18" color="#007AFF"></uni-icons>
        <text>{{ operationMessage }}</text>
      </view>
      <view v-if="refreshFailed" class="refresh-state">
        <text>详情刷新失败</text>
        <button class="refresh-btn" :disabled="loading || !!busyAction" @click="load">重新刷新</button>
      </view>
    </view>

    <view class="ios-section-title">采购明细</view>
    <view class="ios-group">
      <view v-for="item in order.items" :key="item.itemId" class="ios-cell">
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.productName }}</view>
          <view class="ios-cell-sub">已收 {{ qty(item.receivedQty) }} / {{ qty(item.qty) }}{{ item.unit || '' }} · ¥{{ money(item.price) }}</view>
        </view>
        <view class="ios-cell-value">¥{{ money(item.amount) }}</view>
      </view>
    </view>

    <view v-if="canEditOrder" class="ios-section-title">草稿操作</view>
    <view v-if="canEditOrder" class="ios-group">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="editOrder">
        <view class="ios-cell-main">
          <view class="ios-cell-title">编辑采购单</view>
        </view>
        <view class="ios-chevron"></view>
      </view>
      <view v-if="order.status === '0'" class="ios-cell" hover-class="ios-cell-hover" @click="deleteOrder">
        <view class="ios-cell-main">
          <view class="ios-cell-title" style="color: #ff3b30;">删除草稿</view>
        </view>
      </view>
    </view>

    <view v-if="order.approvals && order.approvals.length" class="ios-section-title">审批记录</view>
    <view v-if="order.approvals && order.approvals.length" class="ios-group">
      <view v-for="row in order.approvals" :key="row.approvalId" class="ios-cell">
        <view class="ios-tile" :style="{ backgroundColor: row.result === '1' ? 'rgba(52,199,89,.14)' : 'rgba(255,59,48,.14)' }">
          <uni-icons :type="row.result === '1' ? 'checkmarkempty' : 'closeempty'" size="18"
            :color="row.result === '1' ? '#34C759' : '#FF3B30'"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title">{{ row.approverName }} · {{ row.result === '1' ? '通过' : '驳回' }}</view>
          <view class="ios-cell-sub">{{ row.opinion || '无意见' }}</view>
        </view>
        <view class="ios-cell-value">{{ row.approveTime }}</view>
      </view>
    </view>

    <view class="action-bar" v-if="actions.length || canPrint">
      <button v-if="canPrint" class="ios-btn ios-btn-secondary action-btn"
        :disabled="!!busyAction || confirming || loading" hover-class="ios-btn-hover" @click="printPdf">打印</button>
      <button v-for="action in actions" :key="action.key" class="ios-btn action-btn" :class="action.class"
        :disabled="!!busyAction || confirming || loading || refreshFailed" :loading="busyAction === action.key"
        hover-class="ios-btn-hover" @click="handleAction(action.key)">{{ busyAction === action.key ? busyLabel(action.key) : action.label }}</button>
    </view>
    <view v-if="actions.length || canPrint" class="action-spacer"></view>
    <view v-else class="ios-safe-bottom"></view>
  </view>

  <view v-else class="ios-page">
    <view class="ios-loading">{{ loading ? '加载中…' : (loadFailed ? '采购单加载失败' : '采购单不存在') }}</view>
    <button v-if="loadFailed && !loading" class="refresh-btn" @click="load">重新加载</button>
  </view>
</template>

<script>
  import {
    purchaseDetail,
    purchaseSubmit,
    purchaseApprove,
    purchaseReject,
    purchaseReceive,
    purchaseDelete,
    buildExportUrl
  } from '@/api/business'

  export default {
    data() {
      return {
        orderId: null,
        order: null,
        loading: true,
        loaded: false,
        disposed: false,
        loadVersion: 0,
        loadFailed: false,
        refreshFailed: false,
        confirming: false,
        busyAction: '',
        operationMessage: ''
      }
    },
    computed: {
      canPrint() {
        return this.order && (this.order.status === '2' || this.order.status === '4')
      },
      canEditOrder() {
        if (!this.order || (this.order.status !== '0' && this.order.status !== '3')) return false
        const store = this.$store
        const user = store && store.state && store.state.user
        const perms = (user && user.permissions) || []
        return perms.indexOf('*:*:*') > -1 || perms.indexOf('business:purchase:edit') > -1
      },
      actions() {
        if (!this.order) return []
        const status = this.order.status
        if (status === '0' || status === '3') {
          return [{ key: 'submit', label: '提交审批', class: 'ios-btn-primary' }]
        }
        if (status === '1') {
          return [
            { key: 'reject', label: '驳回', class: 'ios-btn-danger' },
            { key: 'approve', label: '通过', class: 'ios-btn-primary' }
          ]
        }
        if (status === '2') {
          return [{ key: 'receive', label: '确认收货', class: 'ios-btn-primary' }]
        }
        return []
      }
    },
    onLoad(options) {
      this.orderId = options.orderId
      this.load()
    },
    onShow() {
      if (this.loaded && !this.busyAction && !this.confirming) this.load()
    },
    onUnload() {
      this.disposed = true
      this.loadVersion += 1
    },
    methods: {
      async load() {
        if (this.disposed) return false
        const version = ++this.loadVersion
        this.loading = true
        try {
          const res = await purchaseDetail(this.orderId)
          if (version !== this.loadVersion) return false
          if (!res.data || res.data.orderId == null || res.data.status == null) {
            throw new Error('采购单详情返回不完整')
          }
          this.order = { ...res.data, status: String(res.data.status) }
          this.loadFailed = false
          this.refreshFailed = false
          return true
        } catch (error) {
          if (version === this.loadVersion) {
            this.loadFailed = true
            this.refreshFailed = !!this.order
          }
          return false
        } finally {
          if (version === this.loadVersion) {
            this.loading = false
            this.loaded = true
          }
        }
      },
      money(value) {
        return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      },
      qty(value) {
        const num = Number(value || 0)
        return num % 1 === 0 ? num : num.toFixed(2)
      },
      statusText(status) {
        return {
          '0': '草稿', '1': '待审批', '2': '已通过', '3': '已驳回', '4': '已完成', '5': '已取消'
        }[status] || '未知'
      },
      statusBadge(status) {
        return {
          '0': 'ios-badge-gray', '1': 'ios-badge-blue', '2': 'ios-badge-green',
          '3': 'ios-badge-red', '4': 'ios-badge-green', '5': 'ios-badge-gray'
        }[status] || 'ios-badge-gray'
      },
      busyLabel(key) {
        return { submit: '提交中…', approve: '审批中…', reject: '驳回中…', receive: '收货中…' }[key] || '处理中…'
      },
      canExecute(key) {
        return !this.disposed && !this.busyAction && !this.confirming && !this.loading && !this.refreshFailed &&
          this.actions.some(action => action.key === key)
      },
      handleAction(key) {
        if (!this.canExecute(key)) return
        if (key === 'submit') return this.handleSubmit()
        if (key === 'approve') return this.handleApprove()
        if (key === 'reject') return this.handleReject()
        if (key === 'receive') return this.handleReceive()
      },
      async performAction(key, operation, nextStatus, successMessage) {
        if (this.disposed || this.busyAction) return false
        this.busyAction = key
        this.operationMessage = ''
        try {
          await operation()
          if (this.disposed) return false
          // 写接口确认成功后再更新界面，并丢弃提交前尚未返回的旧详情。
          this.loadVersion += 1
          this.order = { ...this.order, status: nextStatus }
          this.operationMessage = successMessage
          await this.load()
          if (this.disposed) return false
          this.$modal.msgSuccess(successMessage)
          return true
        } catch (error) {
          // 超时不代表未落库，重新读取真实状态；请求层负责显示失败原因。
          await this.load()
          return false
        } finally {
          this.busyAction = ''
        }
      },
      confirmOperation(title, content) {
        this.confirming = true
        return new Promise(resolve => {
          uni.showModal({
            title,
            content,
            success: res => resolve(!!res.confirm),
            fail: () => resolve(false)
          })
        }).finally(() => { this.confirming = false })
      },
      askOpinion(title, placeholder) {
        this.confirming = true
        return new Promise(resolve => {
          uni.showModal({
            title,
            editable: true,
            placeholderText: placeholder,
            success: res => resolve(res.confirm ? (res.content || '').trim() : null),
            fail: () => resolve(null)
          })
        }).finally(() => { this.confirming = false })
      },
      async handleSubmit() {
        if (!this.canExecute('submit')) return
        const confirmed = await this.confirmOperation('提交审批', '提交后采购单进入待审批状态，确认提交？')
        if (!confirmed) return
        return this.performAction('submit', () => purchaseSubmit(this.orderId), '1', '已提交审批')
      },
      async handleApprove() {
        if (!this.canExecute('approve')) return
        const opinion = await this.askOpinion('审批通过', '可填写审批意见')
        if (opinion === null) return
        return this.performAction('approve', () => purchaseApprove(this.orderId, opinion), '2', '审批通过')
      },
      async handleReject() {
        if (!this.canExecute('reject')) return
        const opinion = await this.askOpinion('驳回原因', '驳回必须填写原因')
        if (opinion === null) return
        if (!opinion) {
          this.$modal.msgError('驳回必须填写原因')
          return
        }
        return this.performAction('reject', () => purchaseReject(this.orderId, opinion), '3', '已驳回')
      },
      async handleReceive() {
        if (!this.canExecute('receive')) return
        const confirmed = await this.confirmOperation('确认收货', '将按待收数量全部入库，并生成应付账款，确认收货？')
        if (!confirmed) return
        return this.performAction('receive', () => purchaseReceive(this.orderId, {}), '4', '收货完成')
      },
      editOrder() {
        if (!this.canEditOrder || this.busyAction) return
        this.$tab.navigateTo(`/pages/purchase/form?orderId=${this.orderId}`)
      },
      async deleteOrder() {
        if (!this.canEditOrder || this.order.status !== '0' || this.busyAction) return
        const confirmed = await this.confirmOperation('删除草稿', '确认删除该采购单？')
        if (!confirmed) return
        this.busyAction = 'delete'
        try {
          await purchaseDelete(this.orderId)
          if (this.disposed) return
          this.$modal.msgSuccess('已删除')
          setTimeout(() => {
            if (!this.disposed) this.$tab.navigateBack()
          }, 600)
        } catch (error) {
          if (!this.disposed) await this.load()
        } finally {
          this.busyAction = ''
        }
      },
      printPdf() {
        if (this.busyAction || this.confirming || this.loading) return
        const url = buildExportUrl('pdf', this.orderId)
        uni.showLoading({ title: '生成中…' })
        uni.downloadFile({
          url,
          success: res => {
            if (res.statusCode !== 200) {
              this.$modal.msgError('生成失败，请稍后重试')
              return
            }
            uni.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              fail: () => this.$modal.msgError('已下载，但无法预览')
            })
          },
          fail: () => this.$modal.msgError('下载失败，请检查网络'),
          complete: () => uni.hideLoading()
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .detail {
    padding-top: 16rpx;
  }

  .head-no {
    font-size: 32rpx;
    font-weight: 600;
  }

  .head-amount {
    margin-top: 16rpx;
    font-size: 56rpx;
    font-weight: 700;
    letter-spacing: -1rpx;
  }

  .head-meta {
    margin-top: 8rpx;
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .head-remark {
    margin-top: 12rpx;
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .operation-result {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #007aff;
  }

  .refresh-state {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12rpx;
    font-size: 28rpx;
    color: #000;
  }

  .refresh-btn {
    margin: 0;
    padding: 0 24rpx;
    min-height: 88rpx;
    line-height: 88rpx;
    font-size: 28rpx;
    color: #007aff;
    background-color: transparent;
  }

  .refresh-btn::after, .action-btn::after {
    border: none;
  }

  .action-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
    background-color: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(20px);
    border-top: 1rpx solid rgba(60, 60, 67, 0.29);
  }

  .action-spacer {
    height: calc(152rpx + env(safe-area-inset-bottom));
  }

  .action-btn {
    flex: 1;
    min-width: 0;
    margin: 0 8rpx;
    padding: 0;
  }

  .action-btn[disabled] {
    opacity: 0.55;
  }
</style>
