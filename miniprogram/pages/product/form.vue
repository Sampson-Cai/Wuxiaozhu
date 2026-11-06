<template>
  <view class="ios-page">
    <view class="ios-section-title">{{ isEdit ? '编辑商品' : '新增商品' }}</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">编码</text>
        <input v-model="form.productCode" class="ios-form-input" placeholder="必填，单位内唯一"
          placeholder-class="ios-form-placeholder" maxlength="64" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">名称</text>
        <input v-model="form.productName" class="ios-form-input" placeholder="必填" placeholder-class="ios-form-placeholder"
          maxlength="100" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">分类</text>
        <picker class="ios-form-picker" :range="categoryNames" :value="categoryIndex" @change="onCategoryChange">
          <view class="ios-form-picker">{{ categoryNames[categoryIndex] }}</view>
        </picker>
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">默认供应商</text>
        <picker class="ios-form-picker" :range="supplierNames" :value="supplierIndex" @change="onSupplierChange">
          <view class="ios-form-picker">{{ supplierNames[supplierIndex] }}</view>
        </picker>
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">规格</text>
        <input v-model="form.spec" class="ios-form-input" placeholder="如 M2×5" placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">单位</text>
        <input v-model="form.unit" class="ios-form-input" placeholder="个 / 箱 / 包" placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">参考进价</text>
        <input v-model="form.refPrice" class="ios-form-input" type="digit" placeholder="0.00"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">状态</text>
        <switch :checked="form.status === '0'" color="#34C759" @change="onStatusChange" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">备注</text>
        <input v-model="form.remark" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
    </view>

    <view class="form-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="save">保存</view>
    </view>

    <view v-if="isEdit" class="ios-group" style="margin-top: 40rpx;">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="goStock">
        <view class="ios-cell-main"><view class="ios-cell-title">查看库存</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="goReplenishParam">
        <view class="ios-cell-main"><view class="ios-cell-title">补货参数</view></view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="remove">
        <view class="ios-cell-main">
          <view class="ios-cell-title" style="color: #ff3b30;">删除商品</view>
          <view class="ios-cell-sub">被采购单引用或仍有库存时不可删，建议改为停用</view>
        </view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import {
    productDetail, productAdd, productEdit, productDelete,
    categoryList, supplierList
  } from '@/api/business'

  export default {
    data() {
      return {
        productId: null,
        form: {
          productCode: '',
          productName: '',
          categoryId: null,
          supplierId: null,
          spec: '',
          unit: '',
          refPrice: '',
          remark: '',
          status: '0'
        },
        categories: [],
        suppliers: [],
        categoryIndex: 0,
        supplierIndex: 0
      }
    },
    computed: {
      isEdit() {
        return !!this.productId
      },
      categoryNames() {
        return ['未分类'].concat(this.categories.map(item => item.name))
      },
      supplierNames() {
        return ['不指定'].concat(this.suppliers.map(item => item.supplierName))
      }
    },
    onLoad(options) {
      if (options && options.productId) {
        this.productId = options.productId
      }
      this.loadRefs()
    },
    methods: {
      onStatusChange(e) {
        this.form.status = e.detail.value ? '0' : '1'
      },
      loadRefs() {
        Promise.all([
          categoryList().catch(() => ({ rows: [] })),
          supplierList({ pageNum: 1, pageSize: 100 }).catch(() => ({ rows: [] }))
        ]).then(([categoryRes, supplierRes]) => {
          this.categories = categoryRes.rows || []
          this.suppliers = supplierRes.rows || []
          if (this.isEdit) this.loadDetail()
        })
      },
      loadDetail() {
        productDetail(this.productId).then(res => {
          const data = res.data || {}
          this.form.productCode = data.productCode || ''
          this.form.productName = data.productName || ''
          this.form.spec = data.spec || ''
          this.form.unit = data.unit || ''
          this.form.refPrice = data.refPrice === null || data.refPrice === undefined ? '' : String(data.refPrice)
          this.form.remark = data.remark || ''
          this.form.status = data.status || '0'
          this.form.categoryId = data.categoryId || null
          this.form.supplierId = data.supplierId || null
          const ci = this.categories.findIndex(item => item.categoryId === data.categoryId)
          this.categoryIndex = ci > -1 ? ci + 1 : 0
          const si = this.suppliers.findIndex(item => item.supplierId === data.supplierId)
          this.supplierIndex = si > -1 ? si + 1 : 0
        }).catch(() => {})
      },
      onCategoryChange(e) {
        this.categoryIndex = Number(e.detail.value)
        this.form.categoryId = this.categoryIndex === 0 ? null : this.categories[this.categoryIndex - 1].categoryId
      },
      onSupplierChange(e) {
        this.supplierIndex = Number(e.detail.value)
        this.form.supplierId = this.supplierIndex === 0 ? null : this.suppliers[this.supplierIndex - 1].supplierId
      },
      save() {
        if (!this.form.productCode || !this.form.productName) {
          this.$modal.msgError('编码和名称不能为空')
          return
        }
        const payload = {
          productCode: this.form.productCode,
          productName: this.form.productName,
          categoryId: this.form.categoryId,
          supplierId: this.form.supplierId,
          spec: this.form.spec,
          unit: this.form.unit,
          refPrice: this.form.refPrice === '' ? null : Number(this.form.refPrice),
          remark: this.form.remark,
          status: this.form.status
        }
        const request = this.isEdit
          ? productEdit(Object.assign({ productId: Number(this.productId) }, payload))
          : productAdd(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
          setTimeout(() => this.$tab.navigateBack(), 600)
        }).catch(() => {})
      },
      goStock() {
        this.$tab.navigateTo('/pages/stock/records?productId=' + this.productId)
      },
      goReplenishParam() {
        this.$tab.navigateTo('/pages/replenish/param?productId=' + this.productId)
      },
      remove() {
        uni.showModal({
          title: '删除商品',
          content: '确认删除该商品？',
          confirmColor: '#FF3B30',
          success: res => {
            if (!res.confirm) return
            productDelete(this.productId).then(() => {
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
</style>
