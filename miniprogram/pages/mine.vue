<template>
  <view class="ios-page">
    <view :style="{ height: topOffset + 'px' }"></view>
    <view class="ios-large-title">我的</view>

    <!-- 个人信息卡 -->
    <view class="ios-card profile" hover-class="ios-cell-hover" @click="go('/pages/mine/info/index')">
      <view class="profile-avatar">
        <image v-if="avatar" :src="avatar" class="profile-img" mode="aspectFill"></image>
        <image v-else class="profile-img" src="/static/images/logo.png" mode="aspectFill"></image>
      </view>
      <view class="ios-flex-1">
        <view class="profile-name">{{ name || '未登录' }}</view>
        <view class="profile-sub">{{ subTitle }}</view>
      </view>
      <view class="ios-chevron"></view>
    </view>

    <!-- 账号 -->
    <view class="ios-section-title">账号</view>
    <view class="ios-group">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/mine/info/index')">
        <view class="ios-tile" style="background-color: rgba(0,122,255,.12)">
          <uni-icons type="person-filled" size="18" color="#007AFF"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">个人信息</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/mine/pwd/index')">
        <view class="ios-tile" style="background-color: rgba(88,86,214,.12)">
          <uni-icons type="locked-filled" size="18" color="#5856D6"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">修改密码</view></view>
        <view class="ios-chevron"></view>
      </view>
    </view>

    <!-- 管理 -->
    <view class="ios-section-title">管理</view>
    <view class="ios-group">
      <view v-if="hasPerm('business:user:edit')" class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/user/index')">
        <view class="ios-tile" style="background-color: rgba(0,122,255,.12)">
          <uni-icons type="person-filled" size="18" color="#007AFF"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">账号管理</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view v-if="hasPerm('business:role:edit')" class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/role/index')">
        <view class="ios-tile" style="background-color: rgba(88,86,214,.12)">
          <uni-icons type="staff-filled" size="18" color="#5856D6"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">角色权限</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view v-if="hasPerm('business:log:view')" class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/log/index')">
        <view class="ios-tile" style="background-color: rgba(120,120,128,.12)">
          <uni-icons type="bars" size="18" color="#8E8E93"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">操作日志</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/enterprise/index')">
        <view class="ios-tile" style="background-color: rgba(52,199,89,.14)">
          <uni-icons type="home-filled" size="18" color="#34C759"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">单位信息</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/export/index')">
        <view class="ios-tile" style="background-color: rgba(255,149,0,.16)">
          <uni-icons type="cloud-download-filled" size="18" color="#FF9500"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">导出中心</view></view>
        <view class="ios-chevron"></view>
      </view>
    </view>

    <!-- 应用 -->
    <view class="ios-section-title">应用</view>
    <view class="ios-group">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/mine/setting/index')">
        <view class="ios-tile" style="background-color: rgba(120,120,128,.12)">
          <uni-icons type="settings-filled" size="18" color="#8E8E93"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">应用设置</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/mine/help/index')">
        <view class="ios-tile" style="background-color: rgba(52,199,89,.14)">
          <uni-icons type="help-filled" size="18" color="#34C759"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">常见问题</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/mine/about/index')">
        <view class="ios-tile" style="background-color: rgba(255,149,0,.16)">
          <uni-icons type="info-filled" size="18" color="#FF9500"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">关于物小助</view></view>
        <view class="ios-cell-value">v1.0</view>
        <view class="ios-chevron"></view>
      </view>
    </view>

    <!-- 退出 -->
    <view class="logout-wrap">
      <view class="ios-btn ios-btn-danger" hover-class="ios-btn-hover" @click="handleLogout">退出登录</view>
    </view>

    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { getNavTopOffset } from '@/utils/nav'

  export default {
    data() {
      return {
        topOffset: 64,
        name: '',
        avatar: '',
        roles: []
      }
    },
    computed: {
      letter() {
        return (this.name || '仓').slice(0, 1)
      },
      subTitle() {
        if (!this.name) return '点击登录'
        const role = (this.roles && this.roles[0]) || ''
        return role ? `${role} · 已登录` : '已登录'
      }
    },
    onLoad() {
      this.topOffset = getNavTopOffset()
    },
    onShow() {
      const user = this.$store.state.user
      this.name = user.name || ''
      this.avatar = user.avatar || ''
      this.roles = user.roles || []
    },
    methods: {
      hasPerm(perm) {
        const perms = this.$store.state.user.permissions || []
        return perms.indexOf('*:*:*') > -1 || perms.indexOf(perm) > -1
      },
      go(path) {
        if (!this.name) {
          this.$tab.reLaunch('/pages/login')
          return
        }
        this.$tab.navigateTo(path)
      },
      handleLogout() {
        uni.showModal({
          title: '退出登录',
          content: '确认退出当前账号？',
          confirmColor: '#FF3B30',
          success: res => {
            if (!res.confirm) return
            this.$store.dispatch('LogOut').then(() => {
              this.$tab.reLaunch('/pages/login')
            })
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .profile {
    display: flex;
    align-items: center;
    padding: 32rpx;
  }

  .profile-avatar {
    width: 108rpx;
    height: 108rpx;
    border-radius: 54rpx;
    margin-right: 24rpx;
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  }

  .profile-img {
    width: 108rpx;
    height: 108rpx;
  }

  .profile-letter {
    color: #fff;
    font-size: 44rpx;
    font-weight: 600;
  }

  .profile-name {
    font-size: 38rpx;
    font-weight: 600;
    line-height: 48rpx;
  }

  .profile-sub {
    margin-top: 6rpx;
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .logout-wrap {
    padding: 48rpx 24rpx 0;
  }
</style>
