<template>
  <view class="ios-page setting-page">
    <view class="ios-section-title">服务器</view>
    <view class="ios-group">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="editHost">
        <view class="ios-tile" style="background-color: rgba(0,122,255,.12)">
          <uni-icons type="link" size="18" color="#007AFF"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title">服务地址</view>
          <view class="ios-cell-sub ios-ellipsis">{{ host }}</view>
        </view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="testHost">
        <view class="ios-tile" style="background-color: rgba(52,199,89,.14)">
          <uni-icons type="checkmarkempty" size="18" color="#34C759"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">测试连接</view></view>
        <view class="ios-cell-value">{{ testing ? '测试中…' : '' }}</view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="resetHost">
        <view class="ios-tile" style="background-color: rgba(120,120,128,.12)">
          <uni-icons type="refreshempty" size="18" color="#8E8E93"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">恢复默认</view></view>
      </view>
    </view>
    <view class="ios-section-title">账号</view>
    <view class="ios-group">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/mine/pwd/index')">
        <view class="ios-tile" style="background-color: rgba(88,86,214,.12)">
          <uni-icons type="locked-filled" size="18" color="#5856D6"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">修改密码</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="go('/pages/mine/about/index')">
        <view class="ios-tile" style="background-color: rgba(48,176,199,.14)">
          <uni-icons type="info-filled" size="18" color="#30B0C7"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">关于物小助</view></view>
        <view class="ios-cell-value">{{ version }}</view>
        <view class="ios-chevron"></view>
      </view>
    </view>

    <view class="logout-wrap">
      <view class="ios-btn ios-btn-danger" hover-class="ios-btn-hover" @click="logout">退出登录</view>
    </view>

    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { getBaseUrl, setBaseUrl, resetBaseUrl, pingBaseUrl } from '@/utils/server'

  export default {
    data() {
      return {
        host: '',
        testing: false,
        version: 'v1.0.0'
      }
    },
    onShow() {
      this.host = getBaseUrl()
    },
    methods: {
      go(path) {
        this.$tab.navigateTo(path)
      },
      editHost() {
        uni.showModal({
          title: '服务地址',
          editable: true,
          placeholderText: 'http://192.168.1.10:18080',
          content: this.host,
          success: res => {
            if (!res.confirm) return
            const value = (res.content || '').trim()
            if (!value) {
              this.$modal.msgError('地址不能为空')
              return
            }
            if (!/^https?:\/\//i.test(value)) {
              this.$modal.msgError('请以 http:// 或 https:// 开头')
              return
            }
            setBaseUrl(value)
            this.host = getBaseUrl()
            this.$modal.msgSuccess('已保存，重新登录后生效')
          }
        })
      },
      testHost() {
        if (this.testing) return
        this.testing = true
        pingBaseUrl(this.host).then(() => {
          this.$modal.msgSuccess('连接正常')
        }).catch(() => {
          this.$modal.msgError('连接失败，请检查地址、端口与网络')
        }).finally(() => {
          this.testing = false
        })
      },
      resetHost() {
        resetBaseUrl()
        this.host = getBaseUrl()
        this.$modal.msgSuccess('已恢复默认地址')
      },
      logout() {
        this.$modal.confirm('确定退出登录吗？').then(() => {
          this.$store.dispatch('LogOut').then(() => {
            this.$tab.reLaunch('/pages/login')
          })
        }).catch(() => {})
      }
    }
  }
</script>

<style lang="scss" scoped>
  .setting-page {
    padding-bottom: 40rpx;
  }

  .logout-wrap {
    padding: 48rpx 32rpx 0;
  }
</style>
