<template>
  <view class="ios-page">
    <view class="ios-section-title">修改登录密码</view>
    <view class="ios-group">
      <view class="pwd-field">
        <text class="pwd-label">原密码</text>
        <input v-model="form.oldPassword" class="ios-input" type="password" password placeholder="请输入原密码"
          placeholder-class="ios-placeholder" maxlength="20" />
      </view>
      <view class="pwd-field">
        <text class="pwd-label">新密码</text>
        <input v-model="form.newPassword" class="ios-input" type="password" password placeholder="不少于 6 位"
          placeholder-class="ios-placeholder" maxlength="20" />
      </view>
      <view class="pwd-field">
        <text class="pwd-label">确认密码</text>
        <input v-model="form.confirmPassword" class="ios-input" type="password" password placeholder="再次输入新密码"
          placeholder-class="ios-placeholder" maxlength="20" />
      </view>
    </view>

    <view class="pwd-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="submit">确认修改</view>
    </view>
  </view>
</template>

<script>
  import { updateOwnPassword } from '@/api/business'

  export default {
    data() {
      return {
        form: {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        },
        submitting: false
      }
    },
    methods: {
      submit() {
        if (this.submitting) return
        if (!this.form.oldPassword) {
          this.$modal.msgError('请输入原密码')
          return
        }
        if (!this.form.newPassword || this.form.newPassword.length < 6) {
          this.$modal.msgError('新密码不能少于 6 位')
          return
        }
        if (this.form.newPassword !== this.form.confirmPassword) {
          this.$modal.msgError('两次输入的密码不一致')
          return
        }
        this.submitting = true
        updateOwnPassword(this.form.oldPassword, this.form.newPassword).then(() => {
          this.$modal.msgSuccess('密码已修改')
          setTimeout(() => this.$tab.navigateBack(), 800)
        }).catch(() => {}).finally(() => {
          this.submitting = false
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .pwd-field {
    display: flex;
    align-items: center;
    min-height: 100rpx;
    padding: 0 32rpx;
    position: relative;
  }

  .pwd-field + .pwd-field::before {
    content: '';
    position: absolute;
    top: 0;
    left: 32rpx;
    right: 0;
    height: 1rpx;
    background-color: rgba(60, 60, 67, 0.29);
    transform: scaleY(0.5);
  }

  .pwd-label {
    width: 160rpx;
    font-size: 30rpx;
  }

  .pwd-actions {
    padding: 48rpx 24rpx 0;
  }
</style>
