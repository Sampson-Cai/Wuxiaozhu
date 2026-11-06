<template>
  <view class="ios-page detail" v-if="order">
    <!-- 单头 -->
    <view class="ios-card ios-card-pad head">
      <view class="ios-between">
        <view class="head-no">{{ order.orderNo }}</view>
        <view class="ios-badge" :class="statusBadge(order.status)">{{ statusText(order.status) }}</view>
      </view>
      <view class="head-amount">¥{{ money(order.totalAmount) }}</view>
      <view class="head-meta">{{ order.supplierName || '未指定供应商' }} · {{ order.createTime }}</view>
      <view v-if="order.remark" class="head-remark">备注：{{ order.remark }}</view>
    </view>

    <!-- 明细 -->
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

    <!-- 审批记录 -->
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

    <!-- 操作栏 -->
    <view class="action-bar" v-if="actions.length || canPrint">
      <view v-if="canPrint" class="ios-btn ios-btn-secondary action-btn" hover-class="ios-btn-hover" @click="printPdf">
        打印
      </view>
      <view v-for="action in actions" :key="action.key" class="ios-btn action-btn" :class="action.class"
        hover-class="ios-btn-hover" @click="action.handler">{{ action.label }}</view>
    </view>
    <!-- 底部操作栏占位：高度随操作栏实际高度变化，避免最后一块内容被遮住 -->
    <view v-if="actions.length || canPrint" class="action-spacer"></view>
    <view v-else class="ios-safe-bottom"></view>
  </view>

  <view v-else class="ios-page">
    <view class="ios-loading">{{ loading ? '加载中…' : '采购单不存在' }}</view>
  </view>
</template>

<script>
  import {
    purchaseDetail,
    purchaseSubmit,
    purchaseApprove,
    purchaseReject,
    purchaseReceive,
    buildExportUrl
  } from '@/api/business'

  export default {
    data() {
      return {
        orderId: null,
        order: null,
        loading: true
      }
    },
    computed: {
      canPrint() {
        return this.order && (this.order.status === '2' || this.order.status === '4')
      },
      actions() {
        if (!this.order) return []
        const status = this.order.status
        if (status === '0' || status === '3') {
          return [{ key: 'submit', label: '提交审批', class: 'ios-btn-primary', handler: this.handleSubmit }]
        }
        if (status === '1') {
          return [
            { key: 'reject', label: '驳回', class: 'ios-btn-danger', handler: this.handleReject },
            { key: 'approve', label: '通过', class: 'ios-btn-primary', handler: this.handleApprove }
          ]
        }
        if (status === '2') {
          return [{ key: 'receive', label: '确认收货', class: 'ios-btn-primary', handler: this.handleReceive }]
        }
        return []
      }
    },
    onLoad(options) {
      this.orderId = options.orderId
      this.load()
    },
    methods: {
      load() {
        this.loading = true
        purchaseDetail(this.orderId).then(res => {
          this.order = res.data
        }).catch(() => {
          this.order = null
        }).finally(() => {
          this.loading = false
        })
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
      askOpinion(title, placeholder) {
        return new Promise(resolve => {
          uni.showModal({
            title,
            editable: true,
            placeholderText: placeholder,
            success: res => resolve(res.confirm ? (res.content || '') : null),
            fail: () => resolve(null)
          })
        })
      },
      handleSubmit() {
        uni.showModal({
          title: '提交审批',
          content: '提交后采购单进入待审批状态，确认提交？',
          success: res => {
            if (!res.confirm) return
            purchaseSubmit(this.orderId).then(() => {
              this.$modal.msgSuccess('已提交审批')
              this.load()
            }).catch(() => {})
          }
        })
      },
      handleApprove() {
        this.askOpinion('审批通过', '可填写审批意见').then(opinion => {
          if (opinion === null) return
          purchaseApprove(this.orderId, opinion).then(() => {
            this.$modal.msgSuccess('审批通过')
            this.load()
          }).catch(() => {})
        })
      },
      handleReject() {
        this.askOpinion('驳回原因', '驳回必须填写原因').then(opinion => {
          if (opinion === null) return
          if (!opinion) {
            this.$modal.msgError('驳回必须填写原因')
            return
          }
          purchaseReject(this.orderId, opinion).then(() => {
            this.$modal.msgSuccess('已驳回')
            this.load()
          }).catch(() => {})
        })
      },
      handleReceive() {
        uni.showModal({
          title: '确认收货',
          content: '将按待收数量全部入库，并生成应付账款，确认收货？',
          success: res => {
            if (!res.confirm) return
            purchaseReceive(this.orderId, {}).then(() => {
              this.$modal.msgSuccess('收货完成')
              this.load()
            }).catch(() => {})
          }
        })
      },
      printPdf() {
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

  /* 与操作栏等高：96rpx 按钮 + 上下 16rpx 内边距 + 安全区 */
  .action-spacer {
    height: calc(152rpx + env(safe-area-inset-bottom));
  }

  .action-btn {
    flex: 1;
    margin: 0 8rpx;
  }
</style>
