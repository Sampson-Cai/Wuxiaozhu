<template>
  <view class="ios-page">
    <scroll-view scroll-x class="chips-scroll" :show-scrollbar="false">
      <view class="chips">
        <view v-for="tab in tabs" :key="tab.value" class="chip" :class="{ 'chip-active': filter === tab.value }"
          @click="setFilter(tab.value)" hover-class="tap-hover">{{ tab.label }}</view>
      </view>
    </scroll-view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无应付账款</view>
      <view v-for="item in list" :key="item.payableId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openDetail(item)">
        <view class="ios-tile" :style="{ backgroundColor: dueBg(item.dueStatus) }">
          <uni-icons type="wallet-filled" size="18" :color="dueColor(item.dueStatus)"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.supplierName || item.payableNo }}</view>
          <view class="ios-cell-sub ios-ellipsis">{{ item.orderNo || item.payableNo }} · 到期 {{ item.dueDate }}</view>
        </view>
        <view class="cell-right">
          <view class="cell-amount">¥{{ money(item.remainingAmount) }}</view>
          <view class="ios-badge" :class="dueBadge(item.dueStatus)">{{ dueText(item.dueStatus) }}</view>
        </view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { payableList } from '@/api/business'

  export default {
    data() {
      return {
        filter: '',
        tabs: [
          { label: '全部', value: '' },
          { label: '未结清', value: 'unpaid' },
          { label: '7 天内到期', value: 'due' },
          { label: '已逾期', value: 'overdue' }
        ],
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 20,
        loading: false,
        finished: false
      }
    },
    onLoad(options) {
      if (options && options.filter) {
        this.filter = options.filter
      }
      this.load()
    },
    onPullDownRefresh() {
      this.reload(() => uni.stopPullDownRefresh())
    },
    onReachBottom() {
      if (!this.finished && !this.loading) {
        this.pageNum += 1
        this.load()
      }
    },
    methods: {
      setFilter(value) {
        if (this.filter === value) return
        this.filter = value
        this.reload()
      },
      reload(done) {
        this.pageNum = 1
        this.finished = false
        this.list = []
        this.load(done)
      },
      load(done) {
        const params = { pageNum: this.pageNum, pageSize: this.pageSize }
        if (this.filter === 'unpaid') params.unpaidOnly = true
        if (this.filter === 'due') params.dueWithinDays = 7
        if (this.filter === 'overdue') params.overdueOnly = true
        this.loading = true
        payableList(params).then(res => {
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
      money(value) {
        return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      },
      dueText(status) {
        return { '0': '未到期', '1': '临期', '2': '逾期', '3': '已结清' }[status] || '未到期'
      },
      dueBadge(status) {
        return {
          '0': 'ios-badge-gray', '1': 'ios-badge-orange', '2': 'ios-badge-red', '3': 'ios-badge-green'
        }[status] || 'ios-badge-gray'
      },
      dueColor(status) {
        return { '1': '#FF9500', '2': '#FF3B30', '3': '#34C759' }[status] || '#8E8E93'
      },
      dueBg(status) {
        return {
          '1': 'rgba(255,149,0,.16)', '2': 'rgba(255,59,48,.14)', '3': 'rgba(52,199,89,.14)'
        }[status] || 'rgba(120,120,128,.12)'
      },
      openDetail(item) {
        this.$tab.navigateTo('/pages/payable/detail?payableId=' + item.payableId)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .chips-scroll {
    white-space: nowrap;
  }

  .chips {
    display: inline-flex;
    padding: 16rpx 24rpx 16rpx;
  }

  .chip {
    height: 68rpx;
    line-height: 68rpx;
    padding: 0 26rpx;
    margin-right: 14rpx;
    border-radius: 34rpx;
    font-size: 26rpx;
    color: #000;
    background-color: rgba(120, 120, 128, 0.12);
  }

  .chip-active {
    background-color: #007aff;
    color: #fff;
  }

  .cell-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
    margin-left: 16rpx;
  }

  .cell-amount {
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 6rpx;
  }
</style>
