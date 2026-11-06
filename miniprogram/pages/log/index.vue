<template>
  <view class="ios-page">
    <view class="search-bar">
      <view class="search-box">
        <uni-icons type="search" size="16" color="#8E8E93"></uni-icons>
        <input v-model="operName" class="search-input" placeholder="按操作人筛选" placeholder-class="ios-placeholder"
          confirm-type="search" @confirm="reload" />
      </view>
    </view>

    <view class="chips">
      <view class="chip" :class="{ 'chip-active': status === '' }" @click="setStatus('')" hover-class="tap-hover">全部</view>
      <view class="chip" :class="{ 'chip-active': status === '0' }" @click="setStatus('0')" hover-class="tap-hover">成功</view>
      <view class="chip" :class="{ 'chip-active': status === '1' }" @click="setStatus('1')" hover-class="tap-hover">失败</view>
    </view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无日志</view>
      <view v-for="item in list" :key="item.operId" class="log-item">
        <view class="ios-cell" hover-class="ios-cell-hover" @click="toggle(item.operId)">
          <view class="ios-tile" :style="{ backgroundColor: item.status === 0 ? 'rgba(52,199,89,.14)' : 'rgba(255,59,48,.14)' }">
            <uni-icons :type="item.status === 0 ? 'checkmarkempty' : 'closeempty'" size="18"
              :color="item.status === 0 ? '#34C759' : '#FF3B30'"></uni-icons>
          </view>
          <view class="ios-cell-main">
            <view class="ios-cell-title ios-ellipsis">{{ item.title }}</view>
            <view class="ios-cell-sub ios-ellipsis">{{ item.operName }} · {{ item.operTime }}</view>
          </view>
          <view class="ios-cell-value">{{ item.costTime || 0 }}ms</view>
          <view class="ios-chevron" :style="{ transform: expandedId === item.operId ? 'rotate(135deg)' : 'rotate(45deg)' }"></view>
        </view>
        <view v-if="expandedId === item.operId" class="log-detail">
          <view class="detail-row"><text class="detail-label">IP</text><text>{{ item.operIp || '—' }}</text></view>
          <view class="detail-row"><text class="detail-label">请求</text><text>{{ item.requestMethod }} {{ item.operUrl }}</text></view>
          <view v-if="item.errorMsg" class="detail-row"><text class="detail-label">错误</text><text class="err">{{ item.errorMsg }}</text></view>
          <view class="detail-block">
            <view class="detail-label">参数</view>
            <view class="detail-code">{{ item.operParam || '—' }}</view>
          </view>
        </view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { operLogList } from '@/api/business'

  export default {
    data() {
      return {
        operName: '',
        status: '',
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 20,
        loading: false,
        finished: false,
        expandedId: null
      }
    },
    onLoad() {
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
      setStatus(value) {
        if (this.status === value) return
        this.status = value
        this.reload()
      },
      reload(done) {
        this.pageNum = 1
        this.finished = false
        this.list = []
        this.expandedId = null
        this.load(done)
      },
      load(done) {
        this.loading = true
        const params = { pageNum: this.pageNum, pageSize: this.pageSize }
        if (this.operName) params.operName = this.operName
        if (this.status !== '') params.status = Number(this.status)
        operLogList(params).then(res => {
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
      toggle(operId) {
        this.expandedId = this.expandedId === operId ? null : operId
      }
    }
  }
</script>

<style lang="scss" scoped>
  .search-bar {
    padding: 16rpx 24rpx 8rpx;
  }

  .search-box {
    display: flex;
    align-items: center;
    height: 72rpx;
    padding: 0 20rpx;
    border-radius: 20rpx;
    background-color: rgba(120, 120, 128, 0.12);
  }

  .search-input {
    flex: 1;
    margin: 0 12rpx;
    font-size: 28rpx;
    height: 72rpx;
  }

  .chips {
    display: flex;
    padding: 12rpx 24rpx 16rpx;
  }

  .chip {
    height: 68rpx;
    line-height: 68rpx;
    padding: 0 28rpx;
    margin-right: 16rpx;
    border-radius: 34rpx;
    font-size: 26rpx;
    background-color: rgba(120, 120, 128, 0.12);
  }

  .chip-active {
    background-color: #007aff;
    color: #fff;
  }

  .log-item + .log-item {
    border-top: 1rpx solid rgba(60, 60, 67, 0.12);
  }

  .log-detail {
    padding: 0 32rpx 24rpx;
    background-color: rgba(120, 120, 128, 0.06);
  }

  .detail-row {
    display: flex;
    padding: 6rpx 0;
    font-size: 24rpx;
    color: rgba(60, 60, 67, 0.8);
    word-break: break-all;
  }

  .detail-label {
    width: 90rpx;
    flex-shrink: 0;
    color: rgba(60, 60, 67, 0.5);
  }

  .err {
    color: #ff3b30;
  }

  .detail-block {
    margin-top: 8rpx;
  }

  .detail-code {
    margin-top: 6rpx;
    padding: 16rpx;
    border-radius: 12rpx;
    background-color: #fff;
    font-size: 22rpx;
    line-height: 34rpx;
    color: rgba(60, 60, 67, 0.8);
    word-break: break-all;
  }
</style>
