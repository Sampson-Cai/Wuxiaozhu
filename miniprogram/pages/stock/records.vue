<template>
  <view class="ios-page">
    <view class="filter-bar">
      <picker mode="date" :value="beginTime" @change="onBeginChange">
        <view class="date-chip">{{ beginTime || '开始日期' }}</view>
      </picker>
      <text class="date-sep">至</text>
      <picker mode="date" :value="endTime" @change="onEndChange">
        <view class="date-chip">{{ endTime || '结束日期' }}</view>
      </picker>
      <view class="reset" v-if="beginTime || endTime" @click="resetDate" hover-class="tap-hover">重置</view>
    </view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无流水</view>
      <view v-for="item in list" :key="item.recordId" class="ios-cell">
        <view class="ios-tile" :style="{ backgroundColor: item.recordType === '1' ? 'rgba(52,199,89,.14)' : 'rgba(255,149,0,.16)' }">
          <uni-icons :type="item.recordType === '1' ? 'cloud-download-filled' : 'cloud-upload-filled'" size="18"
            :color="item.recordType === '1' ? '#34C759' : '#FF9500'"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">
            {{ item.productName }}
            <text class="qty" :class="item.recordType === '1' ? 'qty-in' : 'qty-out'">
              {{ item.recordType === '1' ? '+' : '-' }}{{ qty(item.qty) }}
            </text>
          </view>
          <view class="ios-cell-sub ios-ellipsis">
            {{ item.bizType }}{{ item.sourceNo ? ' · ' + item.sourceNo : '' }} · {{ item.createTime }}
          </view>
        </view>
        <view class="cell-right">
          <view class="cell-stock">结存 {{ qty(item.afterQty) }}</view>
          <view v-if="item.reverseFlag === '1'" class="ios-badge ios-badge-gray">已冲销</view>
          <view v-else-if="!item.sourceNo" class="reverse-btn" @click.stop="reverse(item)" hover-class="tap-hover">冲销</view>
        </view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { stockRecords, stockReverse } from '@/api/business'

  export default {
    data() {
      return {
        productId: null,
        beginTime: '',
        endTime: '',
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 20,
        loading: false,
        finished: false
      }
    },
    onLoad(options) {
      if (options && options.productId) {
        this.productId = options.productId
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
      onBeginChange(e) {
        this.beginTime = e.detail.value
        this.reload()
      },
      onEndChange(e) {
        this.endTime = e.detail.value
        this.reload()
      },
      resetDate() {
        this.beginTime = ''
        this.endTime = ''
        this.reload()
      },
      reload(done) {
        this.pageNum = 1
        this.finished = false
        this.list = []
        this.load(done)
      },
      load(done) {
        this.loading = true
        stockRecords({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          productId: this.productId || undefined,
          beginTime: this.beginTime || undefined,
          endTime: this.endTime || undefined
        }).then(res => {
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
      reverse(item) {
        uni.showModal({
          title: '冲销流水',
          content: `将生成一笔反向流水冲销 ${this.qty(item.qty)}，确认？`,
          success: res => {
            if (!res.confirm) return
            stockReverse(item.recordId, '手工冲销').then(() => {
              this.$modal.msgSuccess('冲销成功')
              this.reload()
            }).catch(() => {})
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .filter-bar {
    display: flex;
    align-items: center;
    padding: 16rpx 24rpx;
  }

  .date-chip {
    height: 60rpx;
    line-height: 60rpx;
    padding: 0 20rpx;
    border-radius: 30rpx;
    font-size: 26rpx;
    background-color: rgba(120, 120, 128, 0.12);
  }

  .date-sep {
    margin: 0 12rpx;
    font-size: 24rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .reset {
    margin-left: auto;
    font-size: 26rpx;
    color: #007aff;
  }

  .qty {
    margin-left: 8rpx;
    font-size: 28rpx;
    font-weight: 600;
  }

  .qty-in {
    color: #34c759;
  }

  .qty-out {
    color: #ff9500;
  }

  .cell-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
  }

  .cell-stock {
    font-size: 24rpx;
    color: rgba(60, 60, 67, 0.6);
    margin-bottom: 6rpx;
  }

  .reverse-btn {
    padding: 14rpx 22rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    color: #ff3b30;
    background-color: rgba(255, 59, 48, 0.12);
  }
</style>
