<template>
  <view class="login-page">
    <!-- 渐变背景与光斑 -->
    <view class="bg-gradient"></view>
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>
    <view class="blob blob-3"></view>

    <view :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 品牌区 -->
    <view class="brand">
      <view class="brand-mark">
        <image class="brand-logo" src="/static/images/logo.png" mode="aspectFill"></image>
      </view>
      <view class="brand-name">物小助</view>
      <view class="brand-slogan">智能补货 · 采购协同</view>
    </view>

    <!-- 玻璃卡片 -->
    <view class="glass-card">
      <view class="card-title">欢迎回来</view>
      <view class="card-sub">登录单位账号，开始今天的库存与采购</view>

      <view class="field">
        <text class="field-label">账号</text>
        <input v-model="loginForm.username" class="field-input" type="text" placeholder="请输入账号"
          placeholder-class="field-placeholder" maxlength="30" />
      </view>
      <view class="field">
        <text class="field-label">密码</text>
        <input v-model="loginForm.password" class="field-input" type="password" password placeholder="请输入密码"
          placeholder-class="field-placeholder" maxlength="20" />
      </view>
      <view v-if="captchaEnabled" class="field">
        <text class="field-label">验证码</text>
        <input v-model="loginForm.code" class="field-input" type="number" placeholder="请输入验证码"
          placeholder-class="field-placeholder" maxlength="4" />
        <image :src="codeUrl" class="code-img" @click="getCode" mode="aspectFit" hover-class="tap-hover"></image>
      </view>

      <view class="login-btn" hover-class="login-btn-hover" @click="handleLogin">
        <text class="login-btn-text">{{ loading ? '登录中…' : '登 录' }}</text>
      </view>

      <view class="helper">
        <text class="helper-text">忘记密码请联系单位管理员重置</text>
      </view>
    </view>

    <view class="footer">
      <text class="footer-text">登录即代表同意</text>
      <text class="footer-link" @click="handleUserAgrement" hover-class="tap-hover">《用户服务协议》</text>
      <text class="footer-text">与</text>
      <text class="footer-link" @click="handlePrivacy" hover-class="tap-hover">《隐私政策》</text>
    </view>
    <view class="version">物小助 v{{ globalConfig.appInfo.version }}</view>
    <view class="host" @click="editHost" hover-class="tap-hover">{{ host }}</view>
  </view>
</template>

<script>
  import { getCodeImg } from '@/api/login'
  import { getToken } from '@/utils/auth'
  import { getBaseUrl, setBaseUrl } from '@/utils/server'

  export default {
    data() {
      return {
        statusBarHeight: 20,
        host: '',
        codeUrl: '',
        captchaEnabled: true,
        loading: false,
        globalConfig: getApp().globalData.config,
        loginForm: {
          username: '',
          password: '',
          code: '',
          uuid: ''
        }
      }
    },
    created() {
      this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
      this.host = getBaseUrl()
      this.getCode()
    },
    onLoad() {
      //#ifdef H5
      if (getToken()) {
        this.$tab.reLaunch('/pages/index')
      }
      //#endif
    },
    methods: {
      editHost() {
        uni.showModal({
          title: '服务地址',
          editable: true,
          placeholderText: 'http://192.168.1.10:18080',
          content: this.host,
          success: res => {
            if (!res.confirm) return
            const value = (res.content || '').trim()
            if (!/^https?:\/\//i.test(value)) {
              this.$modal.msgError('请以 http:// 或 https:// 开头')
              return
            }
            setBaseUrl(value)
            this.host = getBaseUrl()
            this.$modal.msgSuccess('已切换，正在重连')
            this.getCode()
          }
        })
      },
      handlePrivacy() {
        const site = this.globalConfig.appInfo.agreements[0]
        this.$tab.navigateTo(`/pages/common/webview/index?title=${site.title}&url=${site.url}`)
      },
      handleUserAgrement() {
        const site = this.globalConfig.appInfo.agreements[1]
        this.$tab.navigateTo(`/pages/common/webview/index?title=${site.title}&url=${site.url}`)
      },
      getCode() {
        getCodeImg().then(res => {
          this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
          if (this.captchaEnabled) {
            this.codeUrl = 'data:image/jpeg;base64,' + res.img
            this.loginForm.uuid = res.uuid
          }
        }).catch(() => {})
      },
      handleLogin() {
        if (this.loading) return
        if (this.loginForm.username === '') {
          this.$modal.msgError('请输入账号')
        } else if (this.loginForm.password === '') {
          this.$modal.msgError('请输入密码')
        } else if (this.loginForm.code === '' && this.captchaEnabled) {
          this.$modal.msgError('请输入验证码')
        } else {
          this.pwdLogin()
        }
      },
      pwdLogin() {
        this.loading = true
        this.$store.dispatch('Login', this.loginForm).then(() => {
          this.loginSuccess()
        }).catch(() => {
          this.loading = false
          if (this.captchaEnabled) {
            this.getCode()
          }
        })
      },
      loginSuccess() {
        this.$store.dispatch('GetInfo').then(() => {
          this.loading = false
          this.$tab.reLaunch('/pages/index')
        }).catch(() => {
          this.loading = false
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .login-page {
    position: relative;
    min-height: 100vh;
    overflow: hidden;
    background-color: #0b1020;
  }

  .bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(165deg, #0a84ff 0%, #4f46e5 42%, #1b1b3a 78%, #0b1020 100%);
  }

  .blob {
    position: absolute;
    border-radius: 50%;
    opacity: 0.55;
  }

  .blob-1 {
    top: -120rpx;
    right: -160rpx;
    width: 520rpx;
    height: 520rpx;
    background: radial-gradient(circle, rgba(120, 220, 255, 0.75) 0%, rgba(120, 220, 255, 0) 68%);
  }

  .blob-2 {
    top: 320rpx;
    left: -220rpx;
    width: 560rpx;
    height: 560rpx;
    background: radial-gradient(circle, rgba(160, 120, 255, 0.6) 0%, rgba(160, 120, 255, 0) 70%);
  }

  .blob-3 {
    bottom: -180rpx;
    right: -120rpx;
    width: 460rpx;
    height: 460rpx;
    background: radial-gradient(circle, rgba(0, 200, 255, 0.35) 0%, rgba(0, 200, 255, 0) 70%);
  }

  .brand {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 88rpx 56rpx 0;
    animation: rise 0.6s ease-out both;
  }

  .brand-mark {
    width: 168rpx;
    height: 168rpx;
    border-radius: 42rpx;
    background-color: #ffffff;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 18rpx 44rpx rgba(8, 30, 70, 0.38);
  }

  .brand-logo {
    width: 168rpx;
    height: 168rpx;
  }

  .brand-name {
    margin-top: 30rpx;
    font-size: 60rpx;
    font-weight: 800;
    letter-spacing: 4rpx;
    color: #ffffff;
  }

  .brand-slogan {
    margin-top: 10rpx;
    font-size: 26rpx;
    letter-spacing: 3rpx;
    color: rgba(255, 255, 255, 0.72);
  }

  .glass-card {
    position: relative;
    margin: 56rpx 32rpx 0;
    padding: 44rpx 36rpx 32rpx;
    border-radius: 44rpx;
    background-color: rgba(255, 255, 255, 0.13);
    border: 1rpx solid rgba(255, 255, 255, 0.28);
    box-shadow: 0 24rpx 60rpx rgba(4, 16, 42, 0.35);
    animation: rise 0.7s ease-out 0.08s both;
  }

  .card-title {
    font-size: 40rpx;
    font-weight: 700;
    color: #ffffff;
  }

  .card-sub {
    margin-top: 8rpx;
    margin-bottom: 28rpx;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.66);
  }

  .field {
    display: flex;
    align-items: center;
    height: 104rpx;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.16);
  }

  .field-label {
    width: 110rpx;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.72);
  }

  .field-input {
    flex: 1;
    height: 104rpx;
    font-size: 30rpx;
    color: #ffffff;
  }

  .field-placeholder {
    color: rgba(255, 255, 255, 0.42);
  }

  .code-img {
    width: 180rpx;
    height: 72rpx;
    margin-left: 12rpx;
    border-radius: 12rpx;
    background-color: rgba(255, 255, 255, 0.9);
  }

  .login-btn {
    margin-top: 44rpx;
    height: 104rpx;
    border-radius: 52rpx;
    transition: transform 0.12s ease, opacity 0.12s ease;
    background: linear-gradient(135deg, #ffffff 0%, #e8f1ff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 16rpx 36rpx rgba(0, 40, 110, 0.35);
  }

  .login-btn-hover {
    opacity: 0.88;
    transform: scale(0.99);
  }

  .login-btn-text {
    font-size: 34rpx;
    font-weight: 700;
    letter-spacing: 6rpx;
    color: #0a84ff;
  }

  .helper {
    margin-top: 22rpx;
    text-align: center;
  }

  .helper-text {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.5);
  }

  .footer {
    position: relative;
    margin-top: 40rpx;
    text-align: center;
    font-size: 22rpx;
  }

  .footer-text {
    color: rgba(255, 255, 255, 0.45);
  }

  .footer-link {
    padding: 10rpx 6rpx;
    color: rgba(255, 255, 255, 0.78);
  }

  .version {
    position: relative;
    margin-top: 16rpx;
    padding-bottom: 4rpx;
    text-align: center;
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.32);
  }

  .host {
    position: relative;
    padding: 8rpx 0 calc(32rpx + env(safe-area-inset-bottom));
    text-align: center;
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.22);
  }

  @keyframes rise {
    from {
      opacity: 0;
      transform: translateY(28rpx);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
