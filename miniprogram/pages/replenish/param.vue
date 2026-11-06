<template>
  <view class="ios-page">
    <view class="ios-section-title">补货参数</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">商品</text>
        <picker class="ios-form-picker" :range="productNames" :value="productIndex" @change="onProductChange"
          :disabled="!!productId">
          <view class="ios-form-picker" :class="{ 'ios-form-placeholder': !form.productId }">
            {{ productIndex >= 0 ? productNames[productIndex] : '请选择商品' }}
          </view>
        </picker>
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">安全库存</text>
        <input v-model="form.safetyStock" class="ios-form-input" type="digit" placeholder="低于该值触发建议"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">最大库存</text>
        <input v-model="form.maxStock" class="ios-form-input" type="digit" placeholder="补货上限"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">补货周期</text>
        <input v-model="form.cycleDays" class="ios-form-input" type="number" placeholder="默认 7 天"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">交期</text>
        <input v-model="form.leadDays" class="ios-form-input" type="number" placeholder="默认 3 天"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">季节系数</text>
        <input v-model="form.seasonCoef" class="ios-form-input" type="digit" placeholder="0.80 ~ 1.50"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">参与计算</text>
        <switch :checked="form.enableFlag === '1'" color="#34C759" @change="onEnableChange" />
      </view>
    </view>

    <view class="form-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="save">保存参数</view>
    </view>    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { productList, replenishParam, saveReplenishParam } from '@/api/business'

  export default {
    data() {
      return {
        productId: null,
        products: [],
        productIndex: -1,
        form: {
          productId: null,
          safetyStock: '',
          maxStock: '',
          cycleDays: 7,
          leadDays: 3,
          seasonCoef: 1,
          enableFlag: '1'
        }
      }
    },
    computed: {
      productNames() {
        return this.products.map(item => item.productName)
      }
    },
    onLoad(options) {
      if (options && options.productId) {
        this.productId = options.productId
      }
      this.loadProducts()
    },
    methods: {
      onEnableChange(e) {
        this.form.enableFlag = e.detail.value ? '1' : '0'
      },
      loadProducts() {
        productList({ pageNum: 1, pageSize: 100 }).then(res => {
          this.products = res.rows || []
          if (this.productId) {
            const index = this.products.findIndex(item => String(item.productId) === String(this.productId))
            this.productIndex = index
            this.form.productId = Number(this.productId)
            this.loadParam()
          }
        }).catch(() => {})
      },
      loadParam() {
        replenishParam(this.productId).then(res => {
          const data = res.data || {}
          this.form.safetyStock = data.safetyStock === null || data.safetyStock === undefined ? '' : String(data.safetyStock)
          this.form.maxStock = data.maxStock === null || data.maxStock === undefined ? '' : String(data.maxStock)
          this.form.cycleDays = data.cycleDays || 7
          this.form.leadDays = data.leadDays || 3
          this.form.seasonCoef = data.seasonCoef || 1
          this.form.enableFlag = data.enableFlag || '1'
        }).catch(() => {})
      },
      onProductChange(e) {
        this.productIndex = Number(e.detail.value)
        this.form.productId = this.products[this.productIndex].productId
        this.loadParam()
      },
      save() {
        if (!this.form.productId) {
          this.$modal.msgError('请选择商品')
          return
        }
        const payload = {
          safetyStock: this.form.safetyStock === '' ? 0 : Number(this.form.safetyStock),
          maxStock: this.form.maxStock === '' ? 0 : Number(this.form.maxStock),
          cycleDays: Number(this.form.cycleDays || 7),
          leadDays: Number(this.form.leadDays || 3),
          seasonCoef: Number(this.form.seasonCoef || 1),
          enableFlag: this.form.enableFlag
        }
        saveReplenishParam(this.form.productId, payload).then(() => {
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
