<template>
  <view class="ios-page">
    <view class="search-bar">
      <view class="search-box">
        <uni-icons type="search" size="16" color="#8E8E93"></uni-icons>
        <input v-model="keyword" class="search-input" placeholder="搜索商品名称或编码" placeholder-class="ios-placeholder"
          confirm-type="search" @confirm="reload" />
        <uni-icons v-if="keyword" type="clear" size="16" color="#C7C7CC" @click="clearKeyword"></uni-icons>
      </view>
      <view class="add-btn" @click="openForm()" hover-class="tap-hover">
        <uni-icons type="plus" size="20" color="#007AFF"></uni-icons>
      </view>
    </view>

    <view class="ios-group list-card">
      <view v-if="list.length === 0 && !loading" class="ios-empty">没有匹配的商品</view>
      <view v-for="item in list" :key="item.productId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openDetail(item)">
        <image v-if="item.imageUrl" class="product-thumb" :src="media(item.imageUrl)" mode="aspectFit"></image>
        <view v-else class="ios-tile" :style="{ backgroundColor: item.status === '0' ? 'rgba(175,82,222,.14)' : 'rgba(120,120,128,.12)' }">
          <uni-icons type="gift-filled" size="18" :color="item.status === '0' ? '#AF52DE' : '#8E8E93'"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.productName }}</view>
          <view class="ios-cell-sub ios-ellipsis">
            {{ item.productCode }}<text v-if="item.spec"> · {{ item.spec }}</text>
          </view>
        </view>
        <view class="cell-right">
          <view class="cell-qty">库存 {{ qty(item.stockQty) }}{{ item.unit || '' }}</view>
          <view class="ios-badge" :class="item.status === '0' ? 'ios-badge-green' : 'ios-badge-gray'">
            {{ item.status === '0' ? '正常' : '停用' }}
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
  import { productList } from '@/api/business'
  import { getBaseUrl } from '@/utils/server'

  export default {
    data() {
      return {
        keyword: '',
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
      this.reload(() => uni.stopPullDownRefresh())
    },
    onReachBottom() {
      if (!this.finished && !this.loading) {
        this.pageNum += 1
        this.load()
      }
    },
    methods: {
      media(path) {
        if (!path) return ''
        if (/^https?:\/\//i.test(path)) return path
        return getBaseUrl() + path
      },
      reload(done) {
        this.pageNum = 1
        this.finished = false
        this.list = []
        this.load(done)
      },
      clearKeyword() {
        this.keyword = ''
        this.reload()
      },
      load(done) {
        this.loading = true
        productList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          productName: this.keyword || undefined
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
      openDetail(item) {
        this.$tab.navigateTo('/pages/product/form?productId=' + item.productId)
      },
      openForm() {
        this.$tab.navigateTo('/pages/product/form')
      }
    }
  }
</script>

<style lang="scss" scoped>
  .product-thumb {
    width: 96rpx;
    height: 64rpx;
    margin-right: 20rpx;
    border-radius: 12rpx;
    background-color: #f7f7fa;
    flex-shrink: 0;
  }

  .search-bar {
    display: flex;
    align-items: center;
    padding: 16rpx 24rpx 8rpx;
  }

  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    height: 72rpx;
    padding: 0 20rpx;
    border-radius: 20rpx;
    background-color: rgba(120, 120, 128, 0.12);
  }

  .add-btn {
    width: 80rpx;
    height: 80rpx;
    margin-left: 16rpx;
    border-radius: 22rpx;
    background-color: rgba(0, 122, 255, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .search-input {
    flex: 1;
    margin: 0 12rpx;
    font-size: 28rpx;
    height: 72rpx;
  }

  .list-card {
    margin-top: 12rpx;
  }

  .cell-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
    margin-left: 16rpx;
  }

  .cell-qty {
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
    margin-bottom: 6rpx;
  }
</style>
