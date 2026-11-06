<template>
  <view class="ios-page">
    <view class="search-bar">
      <view class="search-box">
        <uni-icons type="search" size="16" color="#8E8E93"></uni-icons>
        <input v-model="keyword" class="search-input" placeholder="搜索商品名称或编码" placeholder-class="ios-placeholder"
          confirm-type="search" @confirm="reload" />
      </view>
    </view>

    <view class="chips">
      <view class="chip" :class="{ 'chip-active': !lowStockOnly }" @click="setFilter(false)" hover-class="tap-hover">全部</view>
      <view class="chip" :class="{ 'chip-active': lowStockOnly }" @click="setFilter(true)" hover-class="tap-hover">库存预警</view>
      <view class="chip-actions">
        <view class="mini-btn" @click="go('/pages/stock/change?type=in')" hover-class="tap-hover">入库</view>
        <view class="mini-btn" @click="go('/pages/stock/change?type=out')" hover-class="tap-hover">出库</view>
        <view class="mini-btn" @click="go('/pages/stock/records')" hover-class="tap-hover">流水</view>
      </view>
    </view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无库存数据</view>
      <view v-for="item in list" :key="item.productId" class="ios-cell">
        <view class="ios-tile" :style="{ backgroundColor: tileBg(item) }">
          <uni-icons type="shop-filled" size="18" :color="tileColor(item)"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.productName }}</view>
          <view class="ios-cell-sub ios-ellipsis">
            可用 {{ qty(item.availableQty) }} · 在途 {{ qty(item.inTransitQty) }} · 安全 {{ qty(item.safetyStock) }}
          </view>
        </view>
        <view class="cell-right">
          <view class="cell-qty">{{ qty(item.qty) }}{{ item.unit || '' }}</view>
          <view class="ios-badge" :class="statusClass(item.stockStatus)">{{ statusText(item.stockStatus) }}</view>
        </view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { stockList } from '@/api/business'

  export default {
    data() {
      return {
        keyword: '',
        lowStockOnly: false,
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 20,
        loading: false,
        finished: false
      }
    },
    onLoad(options) {
      this.lowStockOnly = options && options.lowStockOnly === '1'
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
      go(path) {
        this.$tab.navigateTo(path)
      },
      setFilter(low) {
        if (this.lowStockOnly === low) return
        this.lowStockOnly = low
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
        stockList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.keyword || undefined,
          lowStockOnly: this.lowStockOnly ? true : undefined
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
      statusText(status) {
        return status === '2' ? '缺货' : status === '1' ? '低于安全库存' : '正常'
      },
      statusClass(status) {
        return status === '2' ? 'ios-badge-red' : status === '1' ? 'ios-badge-orange' : 'ios-badge-green'
      },
      tileColor(item) {
        return item.stockStatus === '2' ? '#FF3B30' : item.stockStatus === '1' ? '#FF9500' : '#34C759'
      },
      tileBg(item) {
        return item.stockStatus === '2' ? 'rgba(255,59,48,.14)'
          : item.stockStatus === '1' ? 'rgba(255,149,0,.16)' : 'rgba(52,199,89,.14)'
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
    align-items: center;
    padding: 12rpx 24rpx 16rpx;
  }

  .chip-actions {
    display: flex;
    margin-left: auto;
  }

  .mini-btn {
    height: 64rpx;
    line-height: 64rpx;
    padding: 0 20rpx;
    margin-left: 12rpx;
    border-radius: 32rpx;
    font-size: 24rpx;
    color: #007aff;
    background-color: rgba(0, 122, 255, 0.12);
  }

  .chip {
    height: 68rpx;
    line-height: 68rpx;
    padding: 0 28rpx;
    margin-right: 16rpx;
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
  }

  .cell-qty {
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 6rpx;
  }
</style>
