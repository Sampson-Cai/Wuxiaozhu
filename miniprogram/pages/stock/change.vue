<template>
  <view class="ios-page">
    <view class="ios-section-title">{{ isIn ? '入库' : '出库' }}</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">商品</text>
        <picker class="ios-form-picker" :range="productNames" :value="productIndex" @change="onProductChange">
          <view class="ios-form-picker" :class="{ 'ios-form-placeholder': !form.productId }">
            {{ productIndex >= 0 ? productNames[productIndex] : '请选择商品' }}
          </view>
        </picker>
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">数量</text>
        <input v-model="form.qty" class="ios-form-input" type="digit" placeholder="必须大于 0"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">业务类型</text>
        <input v-model="form.bizType" class="ios-form-input" :placeholder="isIn ? '其他入库' : '领用出库'"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view v-if="isIn" class="ios-form-field">
        <text class="ios-form-label">批次号</text>
        <input v-model="form.batchNo" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">备注</text>
        <input v-model="form.remark" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
    </view>

    <view class="form-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="submit">确认{{ isIn ? '入库' : '出库' }}</view>
    </view>  </view>
</template>

<script>
  import { productList, stockInbound, stockOutbound } from '@/api/business'

  export default {
    data() {
      return {
        type: 'in',
        form: {
          productId: null,
          qty: '',
          bizType: '',
          batchNo: '',
          remark: ''
        },
        products: [],
        productIndex: -1
      }
    },
    computed: {
      isIn() {
        return this.type === 'in'
      },
      productNames() {
        return this.products.map(item => `${item.productName}（库存 ${Number(item.stockQty || 0)}）`)
      }
    },
    onLoad(options) {
      this.type = options && options.type === 'out' ? 'out' : 'in'
      this.form.bizType = this.isIn ? '其他入库' : '领用出库'
      this.loadProducts()
    },
    methods: {
      loadProducts() {
        productList({ pageNum: 1, pageSize: 100, status: '0' }).then(res => {
          this.products = res.rows || []
        }).catch(() => {})
      },
      onProductChange(e) {
        this.productIndex = Number(e.detail.value)
        this.form.productId = this.products[this.productIndex].productId
      },
      submit() {
        if (!this.form.productId) {
          this.$modal.msgError('请选择商品')
          return
        }
        if (!this.form.qty || Number(this.form.qty) <= 0) {
          this.$modal.msgError('数量必须大于 0')
          return
        }
        const payload = {
          productId: this.form.productId,
          qty: Number(this.form.qty),
          bizType: this.form.bizType,
          remark: this.form.remark
        }
        if (this.isIn) payload.batchNo = this.form.batchNo
        const request = this.isIn ? stockInbound(payload) : stockOutbound(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.isIn ? '入库成功' : '出库成功')
          setTimeout(() => this.$tab.navigateBack(), 600)
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
