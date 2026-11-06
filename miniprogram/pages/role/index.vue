<template>
  <view class="ios-page">
    <view class="toolbar">
      <view class="toolbar-title">角色决定小程序内可见的写操作</view>
      <view class="add-btn" @click="openForm()" hover-class="tap-hover">
        <uni-icons type="plus" size="20" color="#007AFF"></uni-icons>
      </view>
    </view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无角色</view>
      <view v-for="item in list" :key="item.roleId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openForm(item.roleId)">
        <view class="ios-tile" style="background-color: rgba(88,86,214,.12)">
          <uni-icons type="staff-filled" size="18" color="#5856D6"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.roleName }}</view>
          <view class="ios-cell-sub ios-ellipsis">{{ item.roleKey }}</view>
        </view>
        <view class="ios-badge" :class="item.status === '0' ? 'ios-badge-green' : 'ios-badge-gray'">
          {{ item.status === '0' ? '启用' : '停用' }}
        </view>
        <view class="ios-chevron"></view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { roleList } from '@/api/business'

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
        roleList().then(res => {
          this.list = res.rows || []
        }).catch(() => {}).finally(() => {
          this.loading = false
        })
      },
      openForm(roleId) {
        this.$tab.navigateTo('/pages/role/form' + (roleId ? '?roleId=' + roleId : ''))
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
    width: 80rpx;
    height: 80rpx;
    border-radius: 40rpx;
    background-color: rgba(0, 122, 255, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
