<template>
  <view class="ios-page">
    <view class="ios-section-title">{{ isEdit ? '编辑采购单' : '新建采购单' }}</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">供应商</text>
        <picker class="ios-form-picker" :range="supplierNames" :value="supplierIndex" @change="onSupplierChange">
          <view class="ios-form-picker" :class="{ 'ios-form-placeholder': !form.supplierId }">
            {{ form.supplierId ? supplierNames[supplierIndex] : '请选择供应商' }}
          </view>
        </picker>
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">备注</text>
        <input v-model="form.remark" class="ios-form-input" placeholder="选填"
          placeholder-class="ios-form-placeholder" maxlength="200" />
      </view>
    </view>

    <view class="ios-section-title">采购明细（{{ items.length }}）</view>
    <view class="ios-group">
      <view v-if="!items.length" class="ios-empty">暂无明细</view>
      <view v-for="(item, index) in items" :key="index" class="item-block">
        <view class="ios-cell item-head">
          <view class="ios-cell-main">
            <view class="ios-cell-title ios-ellipsis">{{ item.productName }}</view>
            <view class="ios-cell-sub ios-ellipsis">{{ itemSub(item) }}</view>
          </view>
          <view class="item-remove" hover-class="item-remove-hover" @click="removeItem(index)">移除</view>
        </view>
        <view class="ios-form-field">
          <text class="ios-form-label">数量</text>
          <input :value="item.qty" class="ios-form-input" type="digit" placeholder="必须大于 0"
            placeholder-class="ios-form-placeholder" @input="onItemInput(index, 'qty', $event)" />
        </view>
        <view class="ios-form-field">
          <text class="ios-form-label">单价</text>
          <input :value="item.price" class="ios-form-input" type="digit" placeholder="0.00"
            placeholder-class="ios-form-placeholder" @input="onItemInput(index, 'price', $event)" />
        </view>
        <view class="ios-form-field">
          <text class="ios-form-label">小计</text>
          <text class="form-value">¥{{ itemAmount(item) }}</text>
        </view>
      </view>
    </view>

    <view class="add-row">
      <view class="ios-btn ios-btn-secondary" hover-class="ios-btn-hover" @click="openPicker">＋ 添加明细</view>
    </view>

    <view class="ios-section-title">合计</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">金额</text>
        <text class="form-value total-amount">¥{{ totalText }}</text>
      </view>
    </view>

    <view class="form-actions">
      <button class="ios-btn ios-btn-primary" :disabled="saving" :loading="saving"
        hover-class="ios-btn-hover" @click="save">保存草稿</button>
    </view>
    <view class="ios-safe-bottom"></view>

    <view v-if="showPicker" class="picker-mask" @click="closePicker">
      <view class="picker-panel" @click.stop>
        <view class="picker-title">选择商品</view>
        <view class="picker-search">
          <uni-icons type="search" size="16" color="#8E8E93"></uni-icons>
          <input v-model="pickerKeyword" class="picker-search-input" placeholder="搜索名称或编码"
            placeholder-class="ios-form-placeholder" confirm-type="search" />
        </view>
        <scroll-view scroll-y class="picker-list">
          <view v-for="p in filteredProducts" :key="p.productId" class="ios-cell picker-item"
            hover-class="ios-cell-hover" @click="chooseProduct(p)">
            <view class="ios-cell-main">
              <view class="ios-cell-title ios-ellipsis">{{ p.productName }}</view>
              <view class="ios-cell-sub ios-ellipsis">{{ p.productCode }} · {{ p.spec || '无规格' }} · ¥{{ p.refPrice || 0 }}</view>
            </view>
            <view class="ios-chevron"></view>
          </view>
          <view v-if="!filteredProducts.length" class="ios-empty">无匹配商品</view>
        </scroll-view>
        <view class="picker-close">
          <view class="ios-btn ios-btn-secondary" hover-class="ios-btn-hover" @click="closePicker">取消</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import {
    productList, supplierList, purchaseDetail, purchaseCreate, purchaseUpdate
  } from '@/api/business'

  export default {
    data() {
      return {
        orderId: null,
        form: {
          supplierId: null,
          remark: ''
        },
        items: [],
        suppliers: [],
        products: [],
        supplierIndex: 0,
        showPicker: false,
        pickerKeyword: '',
        saving: false,
        disposed: false
      }
    },
    computed: {
      isEdit() {
        return !!this.orderId
      },
      supplierNames() {
        return this.suppliers.map(item => item.supplierName || '未命名')
      },
      filteredProducts() {
        const kw = (this.pickerKeyword || '').trim().toLowerCase()
        const list = kw
          ? this.products.filter(item =>
              (item.productName || '').toLowerCase().indexOf(kw) > -1 ||
              (item.productCode || '').toLowerCase().indexOf(kw) > -1)
          : this.products
        return list.slice(0, 60)
      },
      totalText() {
        const total = this.items.reduce((sum, item) =>
          sum + (Number(item.qty) || 0) * (Number(item.price) || 0), 0)
        return total.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      }
    },
    onLoad(options) {
      if (options && options.orderId) {
        this.orderId = options.orderId
        uni.setNavigationBarTitle({ title: '编辑采购单' })
      }
      this.loadRefs()
    },
    onUnload() {
      this.disposed = true
    },
    methods: {
      loadRefs() {
        Promise.all([
          supplierList({ pageNum: 1, pageSize: 200 }).catch(() => ({ rows: [] })),
          productList({ pageNum: 1, pageSize: 500, status: '0' }).catch(() => ({ rows: [] }))
        ]).then(([supplierRes, productRes]) => {
          if (this.disposed) return
          this.suppliers = supplierRes.rows || []
          this.products = productRes.rows || []
          if (this.isEdit) this.loadDetail()
        })
      },
      loadDetail() {
        purchaseDetail(this.orderId).then(res => {
          if (this.disposed) return
          const data = res.data || {}
          this.form.supplierId = data.supplierId || null
          this.form.remark = data.remark || ''
          const index = this.suppliers.findIndex(item => item.supplierId === data.supplierId)
          this.supplierIndex = index > -1 ? index : 0
          this.items = (data.items || []).map(item => ({
            itemId: item.itemId,
            productId: item.productId,
            productName: item.productName,
            productCode: item.productCode || '',
            spec: item.spec || '',
            unit: item.unit || '',
            qty: item.qty === null || item.qty === undefined ? '' : String(item.qty),
            price: item.price === null || item.price === undefined ? '' : String(item.price)
          }))
        }).catch(() => {})
      },
      onSupplierChange(e) {
        const index = Number(e.detail.value)
        const supplier = this.suppliers[index]
        if (!supplier) return
        this.supplierIndex = index
        this.form.supplierId = supplier.supplierId
      },
      openPicker() {
        this.pickerKeyword = ''
        this.showPicker = true
      },
      closePicker() {
        this.showPicker = false
      },
      chooseProduct(product) {
        this.items.push({
          productId: product.productId,
          productName: product.productName,
          productCode: product.productCode || '',
          spec: product.spec || '',
          unit: product.unit || '',
          qty: '1',
          price: product.refPrice === null || product.refPrice === undefined ? '' : String(product.refPrice)
        })
        this.showPicker = false
      },
      removeItem(index) {
        this.items.splice(index, 1)
      },
      onItemInput(index, field, e) {
        this.items[index][field] = e.detail.value
      },
      itemSub(item) {
        const parts = [item.productCode, item.spec, item.unit].filter(Boolean)
        return parts.length ? parts.join(' · ') : (item.productName || '')
      },
      itemAmount(item) {
        const amount = (Number(item.qty) || 0) * (Number(item.price) || 0)
        return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      },
      save() {
        if (this.saving) return
        if (!this.form.supplierId) {
          this.$modal.msgError('请选择供应商')
          return
        }
        if (!this.items.length) {
          this.$modal.msgError('请添加至少一条明细')
          return
        }
        for (const item of this.items) {
          if (!item.productId) {
            this.$modal.msgError('明细商品不能为空')
            return
          }
          if (!(Number(item.qty) > 0)) {
            this.$modal.msgError('数量必须大于 0')
            return
          }
          if (item.price !== '' && Number(item.price) < 0) {
            this.$modal.msgError('单价不能为负数')
            return
          }
        }
        const payload = {
          supplierId: this.form.supplierId,
          remark: this.form.remark,
          items: this.items.map(item => ({
            productId: item.productId,
            qty: Number(item.qty),
            price: item.price === '' ? null : Number(item.price)
          }))
        }
        this.saving = true
        const request = this.isEdit
          ? purchaseUpdate(this.orderId, payload)
          : purchaseCreate(payload)
        request.then(res => {
          if (this.disposed) return
          this.$modal.msgSuccess(this.isEdit ? '保存成功' : '已创建草稿')
          setTimeout(() => {
            if (this.disposed) return
            if (this.isEdit) {
              this.$tab.navigateBack()
            } else {
              const newId = res && (res.orderId || (res.data && res.data.orderId))
              if (newId) {
                this.$tab.redirectTo('/pages/purchase/detail?orderId=' + newId)
              } else {
                this.$tab.navigateBack()
              }
            }
          }, 600)
        }).catch(() => {}).finally(() => {
          this.saving = false
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .item-block + .item-block {
    border-top: 16rpx solid #f2f2f7;
  }

  .item-head {
    padding-right: 32rpx;
  }

  .item-remove {
    flex-shrink: 0;
    min-width: 88rpx;
    min-height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    font-size: 28rpx;
    color: #ff3b30;
    border-radius: 16rpx;
  }

  .item-remove-hover {
    background-color: rgba(255, 59, 48, 0.08);
  }

  .form-value {
    flex: 1;
    min-width: 0;
    font-size: 30rpx;
    text-align: right;
    line-height: 100rpx;
    color: #000;
  }

  .total-amount {
    font-size: 36rpx;
    font-weight: 600;
  }

  .add-row {
    padding: 24rpx 24rpx 0;
  }

  .form-actions {
    padding: 40rpx 24rpx 0;
  }

  .form-actions .ios-btn::after {
    border: none;
  }

  .picker-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 100;
    display: flex;
    align-items: flex-end;
    background-color: rgba(0, 0, 0, 0.4);
  }

  .picker-panel {
    width: 100%;
    box-sizing: border-box;
    max-height: 82vh;
    display: flex;
    flex-direction: column;
    padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
    background-color: #f2f2f7;
    border-radius: 28rpx 28rpx 0 0;
  }

  .picker-title {
    padding: 8rpx 8rpx 20rpx;
    font-size: 34rpx;
    font-weight: 600;
    text-align: center;
    color: #000;
  }

  .picker-search {
    display: flex;
    align-items: center;
    height: 80rpx;
    padding: 0 20rpx;
    border-radius: 20rpx;
    background-color: #fff;
  }

  .picker-search-input {
    flex: 1;
    min-width: 0;
    margin-left: 12rpx;
    height: 80rpx;
    font-size: 28rpx;
  }

  .picker-list {
    height: 56vh;
    margin-top: 20rpx;
    border-radius: 20rpx;
    background-color: #fff;
  }

  .picker-item {
    min-height: 100rpx;
  }

  .picker-close {
    padding-top: 20rpx;
  }
</style>
