<template>
  <view class="ios-page">
    <view class="header-section">
      <image class="about-logo" src="/static/images/logo.png" mode="aspectFill"></image>
      <view class="about-name">物小助</view>
      <view class="about-slogan">智能补货 · 采购协同</view>
    </view>

    <view class="ios-section-title">应用信息</view>
    <view class="ios-group">
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">版本信息</view></view>
        <view class="ios-cell-value">v{{ version }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">运行平台</view></view>
        <view class="ios-cell-value">微信小程序</view>
      </view>
    </view>

    <view class="ios-section-title">关于</view>
    <view class="ios-group ios-card-pad">
      <view class="about-text">
        物小助面向单位库管与采购场景，覆盖商品与供应商档案、库存出入库、采购审批、分批收货、
        应付账款与规则补货，支持按单位隔离数据与按角色控制权限。
      </view>
    </view>

    <view class="copyright">Copyright © 2026 物小助</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  export default {
    data() {
      return {
        version: getApp().globalData.config.appInfo.version
      }
    }
  }
</script>

<style lang="scss" scoped>
  .header-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 56rpx 0 16rpx;
  }

  .about-logo {
    width: 176rpx;
    height: 176rpx;
    border-radius: 44rpx;
    background-color: #fff;
    box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.08);
  }

  .about-name {
    margin-top: 24rpx;
    font-size: 44rpx;
    font-weight: 700;
    letter-spacing: 2rpx;
  }

  .about-slogan {
    margin-top: 6rpx;
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
    letter-spacing: 1rpx;
  }

  .about-text {
    font-size: 28rpx;
    line-height: 46rpx;
    color: rgba(60, 60, 67, 0.8);
  }

  .copyright {
    margin-top: 56rpx;
    text-align: center;
    font-size: 22rpx;
    color: rgba(60, 60, 67, 0.4);
  }
</style>
