<template>
  <view class="ios-page" v-if="receipt">
    <view class="ios-card ios-card-pad">
      <view class="ios-between">
        <view class="head-no">{{ receipt.receiptNo }}</view>
        <view class="ios-badge ios-badge-green">已入库</view>
      </view>
      <view class="head-meta">采购单 {{ receipt.orderNo }}</view>
      <view class="head-meta">收货人 {{ receipt.receiverName }} · {{ receipt.receiptTime }}</view>
      <view class="head-meta" v-if="receipt.remark">备注：{{ receipt.remark }}</view>
      <view class="head-total">合计 {{ qty(receipt.totalQty) }} 件</view>
    </view>

    <view class="ios-section-title">收货明细</view>
    <view class="ios-group">
      <view v-for="item in items" :key="item.itemId" class="ios-cell">
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.productName || '商品 #' + item.productId }}</view>
          <view class="ios-cell-sub">{{ item.productCode }} · ¥{{ money(item.price) }}</view>
        </view>
        <view class="ios-cell-value">× {{ qty(item.qty) }}</view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
  <view v-else class="ios-page">
    <view class="ios-loading">{{ loading ? '加载中…' : '收货单不存在' }}</view>
  </view>
</template>

<script>
  import { receiptDetail } from '@/api/business'

  export default {
    data() {
      return {
        receiptId: null,
        receipt: null,
        items: [],
        loading: true
      }
    },
    onLoad(options) {
      this.receiptId = options.receiptId
      this.load()
    },
    methods: {
      load() {
        this.loading = true
        receiptDetail(this.receiptId).then(res => {
          this.receipt = res.data
          this.items = res.items || []
        }).catch(() => {
          this.receipt = null
        }).finally(() => {
          this.loading = false
        })
      },
      qty(value) {
        const num = Number(value || 0)
        return num % 1 === 0 ? num : num.toFixed(2)
      },
      money(value) {
        return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .head-no {
    font-size: 32rpx;
    font-weight: 600;
  }

  .head-meta {
    margin-top: 8rpx;
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .head-total {
    margin-top: 16rpx;
    font-size: 40rpx;
    font-weight: 700;
  }
</style>
