<template>
  <view class="ios-page">
    <view class="search-bar">
      <view class="search-box">
        <uni-icons type="search" size="16" color="#8E8E93"></uni-icons>
        <input v-model="keyword" class="search-input" placeholder="搜索供应商名称或编码" placeholder-class="ios-placeholder"
          confirm-type="search" @confirm="reload" />
      </view>
      <view class="add-btn" @click="openForm()" hover-class="tap-hover">
        <uni-icons type="plus" size="20" color="#007AFF"></uni-icons>
      </view>
    </view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无供应商</view>
      <view v-for="item in list" :key="item.supplierId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openForm(item.supplierId)">
        <view class="ios-tile" style="background-color: rgba(255,149,0,.16)">
          <uni-icons type="contact-filled" size="18" color="#FF9500"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.supplierName }}</view>
          <view class="ios-cell-sub ios-ellipsis">{{ item.supplierCode }}<text v-if="item.contact"> · {{ item.contact }}</text></view>
        </view>
        <view class="ios-badge" :class="item.status === '0' ? 'ios-badge-green' : 'ios-badge-gray'">
          {{ item.status === '0' ? '合作中' : '已停用' }}
        </view>
        <view class="ios-chevron"></view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { supplierList } from '@/api/business'

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
    onShow() {
      if (this.list.length) this.reload()
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
      reload(done) {
        this.pageNum = 1
        this.finished = false
        this.list = []
        this.load(done)
      },
      load(done) {
        this.loading = true
        supplierList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          supplierName: this.keyword || undefined
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
      openForm(supplierId) {
        this.$tab.navigateTo('/pages/supplier/form' + (supplierId ? '?supplierId=' + supplierId : ''))
      }
    }
  }
</script>

<style lang="scss" scoped>
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

  .search-input {
    flex: 1;
    margin: 0 12rpx;
    font-size: 28rpx;
    height: 72rpx;
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
</style>
