<template>
  <view class="ios-page">
    <view class="ios-section-title">单位信息</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">单位名称</text>
        <input v-model="form.name" class="ios-form-input" placeholder="必填" placeholder-class="ios-form-placeholder"
          maxlength="100" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">简称</text>
        <input v-model="form.shortName" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder"
          maxlength="50" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">联系人</text>
        <input v-model="form.contact" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder"
          maxlength="30" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">联系电话</text>
        <input v-model="form.phone" class="ios-form-input" type="number" placeholder="选填"
          placeholder-class="ios-form-placeholder" maxlength="20" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">默认账期</text>
        <input v-model="form.payTermDays" class="ios-form-input" type="number" placeholder="天，默认 30"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">备注</text>
        <input v-model="form.remark" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
    </view>

    <view class="form-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="save">保存</view>
    </view>
    <view class="ios-section-footer">账期用于收货后自动生成应付账款的到期日。</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { getEnterprise, updateEnterprise } from '@/api/business'

  export default {
    data() {
      return {
        form: {
          name: '',
          shortName: '',
          contact: '',
          phone: '',
          payTermDays: '',
          remark: ''
        }
      }
    },
    onLoad() {
      this.load()
    },
    methods: {
      load() {
        getEnterprise().then(res => {
          const data = res.data || {}
          this.form.name = data.name || ''
          this.form.shortName = data.shortName || ''
          this.form.contact = data.contact || ''
          this.form.phone = data.phone || ''
          this.form.payTermDays = data.payTermDays === null || data.payTermDays === undefined ? '' : String(data.payTermDays)
          this.form.remark = data.remark || ''
        }).catch(() => {})
      },
      save() {
        if (!this.form.name) {
          this.$modal.msgError('单位名称不能为空')
          return
        }
        updateEnterprise({
          name: this.form.name,
          shortName: this.form.shortName,
          contact: this.form.contact,
          phone: this.form.phone,
          payTermDays: this.form.payTermDays === '' ? null : Number(this.form.payTermDays),
          remark: this.form.remark
        }).then(() => {
          this.$modal.msgSuccess('保存成功')
        }).catch(() => {})
      }
    }
  }
</script>

<style lang="scss" scoped>
  .form-actions {
    padding: 40rpx 24rpx 0;
  }
</style>
