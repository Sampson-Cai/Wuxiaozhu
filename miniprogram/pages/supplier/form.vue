<template>
  <view class="ios-page">
    <view class="ios-section-title">{{ isEdit ? '编辑供应商' : '新增供应商' }}</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">编码</text>
        <input v-model="form.supplierCode" class="ios-form-input" placeholder="必填，单位内唯一"
          placeholder-class="ios-form-placeholder" maxlength="64" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">名称</text>
        <input v-model="form.supplierName" class="ios-form-input" placeholder="必填" placeholder-class="ios-form-placeholder"
          maxlength="100" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">联系人</text>
        <input v-model="form.contact" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">电话</text>
        <input v-model="form.phone" class="ios-form-input" type="number" placeholder="选填"
          placeholder-class="ios-form-placeholder" maxlength="20" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">邮箱</text>
        <input v-model="form.email" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">地址</text>
        <input v-model="form.address" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">账期（天）</text>
        <input v-model="form.payTermDays" class="ios-form-input" type="number" placeholder="默认 30"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">交期（天）</text>
        <input v-model="form.leadDays" class="ios-form-input" type="number" placeholder="默认 3"
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

    <view v-if="isEdit" class="ios-group danger-group">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="toggleStatus">
        <view class="ios-cell-main">
          <view class="ios-cell-title">{{ form.status === '0' ? '停用供应商' : '启用供应商' }}</view>
          <view class="ios-cell-sub">停用后新建采购单选不到该供应商</view>
        </view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="remove">
        <view class="ios-cell-main">
          <view class="ios-cell-title" style="color: #ff3b30;">删除供应商</view>
          <view class="ios-cell-sub">有采购单、被商品引用或有未结清应付时不可删</view>
        </view>
      </view>
    </view>

    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { supplierDetail, supplierAdd, supplierEdit, supplierStatus, supplierDelete } from '@/api/business'

  export default {
    data() {
      return {
        supplierId: null,
        form: {
          supplierCode: '',
          supplierName: '',
          contact: '',
          phone: '',
          email: '',
          address: '',
          payTermDays: '',
          leadDays: '',
          remark: '',
          status: '0'
        }
      }
    },
    computed: {
      isEdit() {
        return !!this.supplierId
      }
    },
    onLoad(options) {
      if (options && options.supplierId) {
        this.supplierId = options.supplierId
        this.load()
      }
    },
    methods: {
      load() {
        supplierDetail(this.supplierId).then(res => {
          const data = res.data || {}
          Object.keys(this.form).forEach(key => {
            if (data[key] !== undefined && data[key] !== null) this.form[key] = String(data[key])
          })
        }).catch(() => {})
      },
      save() {
        if (!this.form.supplierCode || !this.form.supplierName) {
          this.$modal.msgError('编码和名称不能为空')
          return
        }
        const payload = {
          supplierCode: this.form.supplierCode,
          supplierName: this.form.supplierName,
          contact: this.form.contact,
          phone: this.form.phone,
          email: this.form.email,
          address: this.form.address,
          payTermDays: this.form.payTermDays ? Number(this.form.payTermDays) : null,
          leadDays: this.form.leadDays ? Number(this.form.leadDays) : null,
          remark: this.form.remark
        }
        const request = this.isEdit
          ? supplierEdit(Object.assign({ supplierId: Number(this.supplierId) }, payload))
          : supplierAdd(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
          setTimeout(() => this.$tab.navigateBack(), 600)
        }).catch(() => {})
      },
      toggleStatus() {
        const next = this.form.status === '0' ? '1' : '0'
        supplierStatus(this.supplierId, next).then(() => {
          this.form.status = next
          this.$modal.msgSuccess('状态已更新')
        }).catch(() => {})
      },
      remove() {
        uni.showModal({
          title: '删除供应商',
          content: '删除后不可恢复，确认删除？',
          confirmColor: '#FF3B30',
          success: res => {
            if (!res.confirm) return
            supplierDelete(this.supplierId).then(() => {
              this.$modal.msgSuccess('删除成功')
              setTimeout(() => this.$tab.navigateBack(), 600)
            }).catch(() => {})
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .form-actions {
    padding: 40rpx 24rpx 0;
  }

  .danger-group {
    margin-top: 40rpx;
  }
</style>
