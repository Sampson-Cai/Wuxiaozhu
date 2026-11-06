<template>
  <view class="ios-page">
    <view class="search-bar">
      <view class="search-box">
        <uni-icons type="search" size="16" color="#8E8E93"></uni-icons>
        <input v-model="keyword" class="search-input" placeholder="搜索单号或供应商" placeholder-class="ios-placeholder"
          confirm-type="search" @confirm="reload" />
      </view>
    </view>

    <scroll-view scroll-x class="chips-scroll" :show-scrollbar="false">
      <view class="chips">
        <view v-for="tab in tabs" :key="tab.value" class="chip" :class="{ 'chip-active': status === tab.value }"
          @click="setStatus(tab.value)" hover-class="tap-hover">{{ tab.label }}</view>
      </view>
    </scroll-view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无采购单</view>
      <view v-for="item in list" :key="item.orderId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openDetail(item)">
        <view class="ios-tile" :style="{ backgroundColor: statusBg(item.status) }">
          <uni-icons type="cart-filled" size="18" :color="statusColor(item.status)"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.orderNo }}</view>
          <view class="ios-cell-sub ios-ellipsis">{{ item.supplierName || '未指定供应商' }} · {{ item.createTime }}</view>
        </view>
        <view class="cell-right">
          <view class="cell-amount">¥{{ money(item.totalAmount) }}</view>
          <view class="ios-badge" :class="statusBadge(item.status)">{{ statusText(item.status) }}</view>
        </view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>

    <view class="fab-spacer" v-if="canCreate"></view>
    <view v-if="canCreate" class="fab" hover-class="fab-hover" @click="createOrder">
      <uni-icons type="plusempty" size="20" color="#fff"></uni-icons>
      <text class="fab-label">新建</text>
    </view>
  </view>
</template>

<script>
  import { purchaseList } from '@/api/business'

  export default {
    data() {
      return {
        keyword: '',
        status: '',
        tabs: [
          { label: '全部', value: '' },
          { label: '草稿', value: '0' },
          { label: '待审批', value: '1' },
          { label: '已通过', value: '2' },
          { label: '已完成', value: '4' },
          { label: '已驳回', value: '3' },
          { label: '已取消', value: '5' }
        ],
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 20,
        loading: false,
        finished: false,
        loaded: false
      }
    },
    computed: {
      canCreate() {
        const store = this.$store
        const user = store && store.state && store.state.user
        const perms = (user && user.permissions) || []
        return perms.indexOf('*:*:*') > -1 || perms.indexOf('business:purchase:edit') > -1
      }
    },
    onLoad(options) {
      if (options && options.status) {
        this.status = options.status
      }
      this.load()
    },
    onShow() {
      // 从详情返回时刷新状态；首次进入由 onLoad 负责，避免重复请求
      if (this.loaded) {
        this.reload()
      }
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
        this.load(done)
      },
      load(done) {
        this.loading = true
        purchaseList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.status || undefined,
          keyword: this.keyword || undefined
        }).then(res => {
          const rows = res.rows || []
          this.list = this.pageNum === 1 ? rows : this.list.concat(rows)
          this.total = res.total || 0
          this.finished = this.list.length >= this.total || rows.length === 0
        }).catch(() => {
          this.finished = true
        }).finally(() => {
          this.loading = false
          this.loaded = true
          if (done) done()
        })
      },
      money(value) {
        return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      },
      statusText(status) {
        return {
          '0': '草稿',
          '1': '待审批',
          '2': '已通过',
          '3': '已驳回',
          '4': '已完成',
          '5': '已取消'
        }[status] || '未知'
      },
      statusBadge(status) {
        return {
          '0': 'ios-badge-gray',
          '1': 'ios-badge-blue',
          '2': 'ios-badge-green',
          '3': 'ios-badge-red',
          '4': 'ios-badge-green',
          '5': 'ios-badge-gray'
        }[status] || 'ios-badge-gray'
      },
      statusColor(status) {
        return {
          '1': '#007AFF',
          '2': '#34C759',
          '3': '#FF3B30',
          '4': '#34C759'
        }[status] || '#8E8E93'
      },
      statusBg(status) {
        return {
          '1': 'rgba(0,122,255,.12)',
          '2': 'rgba(52,199,89,.14)',
          '3': 'rgba(255,59,48,.14)',
          '4': 'rgba(52,199,89,.14)'
        }[status] || 'rgba(120,120,128,.12)'
      },
      openDetail(item) {
        this.$tab.navigateTo(`/pages/purchase/detail?orderId=${item.orderId}`)
      },
      createOrder() {
        this.$tab.navigateTo('/pages/purchase/form')
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

  .chips-scroll {
    white-space: nowrap;
  }

  .chips {
    display: inline-flex;
    padding: 12rpx 24rpx 16rpx;
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

  /* 悬浮新建按钮：44pt 以上触控区，避开底部安全区 */
  .fab-spacer {
    height: calc(160rpx + env(safe-area-inset-bottom));
  }

  .fab {
    position: fixed;
    right: 32rpx;
    bottom: calc(40rpx + env(safe-area-inset-bottom));
    z-index: 20;
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 176rpx;
    height: 96rpx;
    padding: 0 32rpx;
    border-radius: 48rpx;
    background-color: #007aff;
    box-shadow: 0 8rpx 24rpx rgba(0, 122, 255, 0.28);
  }

  .fab-hover {
    opacity: 0.75;
  }

  .fab-label {
    margin-left: 8rpx;
    font-size: 30rpx;
    font-weight: 600;
    color: #fff;
  }
</style>
