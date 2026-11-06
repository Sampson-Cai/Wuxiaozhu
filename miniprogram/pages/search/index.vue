<template>
  <view class="ios-page">
    <view class="search-bar">
      <view class="search-box">
        <uni-icons type="search" size="16" color="#8E8E93"></uni-icons>
        <input v-model="keyword" class="search-input" placeholder="用一句话描述你要找的商品" placeholder-class="ios-placeholder"
          confirm-type="search" @confirm="doSearch" />
        <text v-if="keyword" class="search-btn" @click="doSearch" hover-class="tap-hover">搜索</text>
      </view>
    </view>

    <view v-if="searched" class="ios-section-title">匹配结果（{{ rows.length }}）</view>
    <view class="ios-group" v-if="searched">
      <view v-if="rows.length === 0 && !loading" class="ios-empty">没有匹配的商品，换个说法试试</view>
      <view v-for="item in rows" :key="item.productId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openProduct(item)">
        <view class="ios-tile" style="background-color: rgba(48,176,199,.14)">
          <uni-icons type="search" size="18" color="#30B0C7"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.productName }}</view>
          <view class="ios-cell-sub ios-ellipsis">
            {{ item.productCode }}<text v-if="item.spec"> · {{ item.spec }}</text>
          </view>
          <view class="matched" v-if="item.matchedTerms && item.matchedTerms.length">
            <text v-for="term in item.matchedTerms" :key="term" class="matched-tag">{{ term }}</text>
          </view>
        </view>
        <view class="cell-right">
          <view class="score">{{ Math.round((item.score || 0) * 100) }}%</view>
          <view class="stock">库存 {{ qty(item.stockQty) }}</view>
        </view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">检索中…</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { searchProduct } from '@/api/business'

  export default {
    data() {
      return {
        keyword: '',
        rows: [],
        loading: false,
        searched: false
      }
    },
    methods: {
      doSearch() {
        if (!this.keyword) {
          this.$modal.msgError('请输入要查找的内容')
          return
        }
        this.loading = true
        searchProduct(this.keyword, 20).then(res => {
          this.rows = res.rows || []
          this.searched = true
        }).catch(() => {}).finally(() => {
          this.loading = false
        })
      },
      qty(value) {
        const num = Number(value || 0)
        return num % 1 === 0 ? num : num.toFixed(2)
      },
      openProduct(item) {
        this.$tab.navigateTo('/pages/product/form?productId=' + item.productId)
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
    height: 76rpx;
    padding: 0 20rpx;
    border-radius: 20rpx;
    background-color: rgba(120, 120, 128, 0.12);
  }

  .search-input {
    flex: 1;
    margin: 0 12rpx;
    font-size: 28rpx;
    height: 76rpx;
  }

  .search-btn {
    padding: 14rpx 8rpx;
    font-size: 28rpx;
    color: #007aff;
  }

  .tips {
    padding: 24rpx 32rpx;
    font-size: 26rpx;
    line-height: 38rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .matched {
    margin-top: 8rpx;
  }

  .matched-tag {
    display: inline-block;
    margin-right: 10rpx;
    padding: 0 12rpx;
    height: 36rpx;
    line-height: 36rpx;
    border-radius: 18rpx;
    font-size: 20rpx;
    color: #30b0c7;
    background-color: rgba(48, 176, 199, 0.12);
  }

  .cell-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
  }

  .score {
    font-size: 28rpx;
    font-weight: 600;
    color: #30b0c7;
  }

  .stock {
    margin-top: 4rpx;
    font-size: 22rpx;
    color: rgba(60, 60, 67, 0.6);
  }
</style>
