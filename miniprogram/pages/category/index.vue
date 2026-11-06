<template>
  <view class="ios-page">
    <view class="toolbar">
      <view class="toolbar-title">共 {{ list.length }} 个分类</view>
      <view class="add-btn" @click="openForm()" hover-class="tap-hover">
        <uni-icons type="plus" size="20" color="#007AFF"></uni-icons>
        <text class="add-text">新增</text>
      </view>
    </view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无分类</view>
      <view v-for="item in list" :key="item.categoryId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openForm(item.categoryId)">
        <view class="ios-tile" :style="{ marginLeft: (item.parentId && item.parentId !== 0 ? '40rpx' : '0') }"
          style="background-color: rgba(0,122,255,.12)">
          <uni-icons :type="item.parentId && item.parentId !== 0 ? 'list' : 'folder-add-filled'" size="18" color="#007AFF"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.name }}</view>
          <view class="ios-cell-sub">排序 {{ item.sort }}</view>
        </view>
        <view class="ios-badge" :class="item.status === '0' ? 'ios-badge-green' : 'ios-badge-gray'">
          {{ item.status === '0' ? '正常' : '停用' }}
        </view>
        <view class="ios-chevron"></view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { categoryList } from '@/api/business'

  export default {
    data() {
      return {
        list: [],
        loading: false
      }
    },
    onShow() {
      this.load()
    },
    methods: {
      load() {
        this.loading = true
        categoryList().then(res => {
          this.list = res.rows || []
        }).catch(() => {}).finally(() => {
          this.loading = false
        })
      },
      openForm(categoryId) {
        this.$tab.navigateTo('/pages/category/form' + (categoryId ? '?categoryId=' + categoryId : ''))
      }
    }
  }
</script>

<style lang="scss" scoped>
  .toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 32rpx 16rpx;
  }

  .toolbar-title {
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .add-btn {
    display: flex;
    align-items: center;
    height: 80rpx;
    padding: 0 28rpx;
    border-radius: 40rpx;
    background-color: rgba(0, 122, 255, 0.12);
  }

  .add-text {
    margin-left: 6rpx;
    font-size: 26rpx;
    color: #007aff;
  }
</style>
