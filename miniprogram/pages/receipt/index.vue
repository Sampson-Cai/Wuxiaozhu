<template>
  <view class="ios-page">
    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无收货记录</view>
      <view v-for="item in list" :key="item.receiptId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openDetail(item)">
        <view class="ios-tile" style="background-color: rgba(52,199,89,.14)">
          <uni-icons type="download-filled" size="18" color="#34C759"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.receiptNo }}</view>
          <view class="ios-cell-sub ios-ellipsis">{{ item.orderNo }} · {{ item.receiverName }} · {{ item.receiptTime }}</view>
        </view>
        <view class="ios-cell-value">{{ qty(item.totalQty) }} 件</view>
        <view class="ios-chevron"></view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { receiptList } from '@/api/business'

  export default {
    data() {
      return {
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 20,
        loading: false,
        finished: false
      }
    },
    onLoad() {
      this.load()
    },
    onPullDownRefresh() {
      this.pageNum = 1
      this.finished = false
      this.list = []
      this.load(() => uni.stopPullDownRefresh())
    },
    onReachBottom() {
      if (!this.finished && !this.loading) {
        this.pageNum += 1
        this.load()
      }
    },
    methods: {
      load(done) {
        this.loading = true
        receiptList({ pageNum: this.pageNum, pageSize: this.pageSize }).then(res => {
          const rows = res.rows || []
          this.list = this.pageNum === 1 ? rows : this.list.concat(rows)
          this.total = res.total || 0
          this.finished = this.list.length >= this.total || rows.length === 0
        }).catch(() => {
          this.finished = true
        }).finally(() => {
          this.loading = false
          if (done) done()
        })
      },
      qty(value) {
        const num = Number(value || 0)
        return num % 1 === 0 ? num : num.toFixed(2)
      },
      openDetail(item) {
        this.$tab.navigateTo('/pages/receipt/detail?receiptId=' + item.receiptId)
      }
    }
  }
</script>
