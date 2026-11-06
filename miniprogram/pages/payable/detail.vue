<template>
  <view class="ios-page" v-if="payable">
    <view class="ios-card ios-card-pad">
      <view class="ios-between">
        <view class="head-no">{{ payable.payableNo }}</view>
        <view class="ios-badge" :class="dueBadge(payable.dueStatus)">{{ dueText(payable.dueStatus) }}</view>
      </view>
      <view class="head-amount">¥{{ money(payable.remainingAmount) }}</view>
      <view class="head-meta">未付金额 · 到期 {{ payable.dueDate }}</view>
      <view class="head-meta">{{ payable.supplierName }} · 采购单 {{ payable.orderNo }}</view>
    </view>

    <view class="ios-section-title">账款明细</view>
    <view class="ios-group">
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">应付金额</view></view>
        <view class="ios-cell-value">¥{{ money(payable.totalAmount) }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">已付金额</view></view>
        <view class="ios-cell-value">¥{{ money(payable.paidAmount) }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">付款状态</view></view>
        <view class="ios-cell-value">{{ statusText(payable.status) }}</view>
      </view>
    </view>

    <view class="ios-section-title">付款记录</view>
    <view class="ios-group">
      <view v-if="payments.length === 0" class="ios-empty" style="padding: 48rpx 0;">暂无付款记录</view>
      <view v-for="row in payments" :key="row.paymentId" class="ios-cell">
        <view class="ios-tile" style="background-color: rgba(255,149,0,.16)">
          <uni-icons type="wallet-filled" size="18" color="#FF9500"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title">¥{{ money(row.amount) }}</view>
          <view class="ios-cell-sub">{{ row.payMethod || '未填写方式' }} · {{ row.payTime }}</view>
        </view>
      </view>
    </view>

    <view v-if="canPay" class="pay-panel">
      <view class="ios-section-title">付款登记</view>
      <view class="ios-group">
        <view class="ios-form-field">
          <text class="ios-form-label">付款金额</text>
          <input v-model="payForm.amount" class="ios-form-input" type="digit" placeholder="不超过未付金额"
            placeholder-class="ios-form-placeholder" />
        </view>
        <view class="ios-form-field">
          <text class="ios-form-label">付款方式</text>
          <picker class="ios-form-picker" :range="payMethods" :value="methodIndex" @change="onMethodChange">
            <view class="ios-form-picker">{{ payMethods[methodIndex] }}</view>
          </picker>
        </view>
        <view class="ios-form-field">
          <text class="ios-form-label">备注</text>
          <input v-model="payForm.remark" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
        </view>
      </view>
      <view class="pay-actions">
        <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="submitPay">确认付款</view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
  <view v-else class="ios-page">
    <view class="ios-loading">{{ loading ? '加载中…' : '应付单不存在' }}</view>
  </view>
</template>

<script>
  import { payableDetail, payablePay } from '@/api/business'

  export default {
    data() {
      return {
        payableId: null,
        payable: null,
        payments: [],
        loading: true,
        payForm: { amount: '', remark: '' },
        payMethods: ['银行转账', '现金', '微信', '支付宝', '其他'],
        methodIndex: 0
      }
    },
    computed: {
      canPay() {
        return this.payable && this.payable.status !== '2'
      }
    },
    onLoad(options) {
      this.payableId = options.payableId
      this.load()
    },
    methods: {
      onMethodChange(e) {
        this.methodIndex = Number(e.detail.value)
      },
      load() {
        this.loading = true
        payableDetail(this.payableId).then(res => {
          this.payable = res.data
          this.payments = res.payments || []
        }).catch(() => {
          this.payable = null
        }).finally(() => {
          this.loading = false
        })
      },
      money(value) {
        return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      },
      dueText(status) {
        return { '0': '未到期', '1': '临期', '2': '逾期', '3': '已结清' }[status] || '未到期'
      },
      dueBadge(status) {
        return { '0': 'ios-badge-gray', '1': 'ios-badge-orange', '2': 'ios-badge-red', '3': 'ios-badge-green' }[status] || 'ios-badge-gray'
      },
      statusText(status) {
        return { '0': '未付款', '1': '部分付款', '2': '已结清' }[status] || '未知'
      },
      submitPay() {
        const amount = Number(this.payForm.amount)
        if (!amount || amount <= 0) {
          this.$modal.msgError('付款金额必须大于 0')
          return
        }
        if (amount > Number(this.payable.remainingAmount || 0)) {
          this.$modal.msgError('付款金额不能超过未付金额')
          return
        }
        payablePay(this.payableId, {
          amount,
          payMethod: this.payMethods[this.methodIndex],
          remark: this.payForm.remark
        }).then(() => {
          this.$modal.msgSuccess('付款已登记')
          this.payForm.amount = ''
          this.payForm.remark = ''
          this.load()
        }).catch(() => {})
      }
    }
  }
</script>

<style lang="scss" scoped>
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

  .pay-actions {
    padding: 32rpx 24rpx 0;
  }
</style>
