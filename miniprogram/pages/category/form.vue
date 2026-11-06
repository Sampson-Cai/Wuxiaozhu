<template>
  <view class="ios-page">
    <view class="ios-section-title">{{ isEdit ? '编辑分类' : '新增分类' }}</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">名称</text>
        <input v-model="form.name" class="ios-form-input" placeholder="必填" placeholder-class="ios-form-placeholder"
          maxlength="50" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">上级分类</text>
        <picker class="ios-form-picker" :range="parentNames" :value="parentIndex" @change="onParentChange">
          <view class="ios-form-picker">{{ parentNames[parentIndex] }}</view>
        </picker>
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">排序</text>
        <input v-model="form.sort" class="ios-form-input" type="number" placeholder="数字越小越靠前"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">状态</text>
        <switch :checked="form.status === '0'" color="#34C759" @change="onStatusChange" />
      </view>
    </view>

    <view class="form-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="save">保存</view>
    </view>

    <view v-if="isEdit" class="ios-group" style="margin-top: 40rpx;">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="remove">
        <view class="ios-cell-main">
          <view class="ios-cell-title" style="color: #ff3b30;">删除分类</view>
          <view class="ios-cell-sub">存在子分类或商品时不可删除</view>
        </view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { categoryList, categoryDetail, categoryAdd, categoryEdit, categoryDelete } from '@/api/business'

  export default {
    data() {
      return {
        categoryId: null,
        form: {
          name: '',
          parentId: 0,
          sort: 0,
          status: '0'
        },
        parents: [],
        parentIndex: 0
      }
    },
    computed: {
      isEdit() {
        return !!this.categoryId
      },
      parentNames() {
        return ['顶级分类'].concat(this.parents.map(item => item.name))
      }
    },
    onLoad(options) {
      if (options && options.categoryId) {
        this.categoryId = options.categoryId
      }
      this.loadParents()
    },
    methods: {
      onStatusChange(e) {
        this.form.status = e.detail.value ? '0' : '1'
      },
      loadParents() {
        categoryList().then(res => {
          this.parents = (res.rows || []).filter(item => String(item.categoryId) !== String(this.categoryId))
          this.afterParents()
        }).catch(() => {})
      },
      afterParents() {
        if (this.isEdit) {
          categoryDetail(this.categoryId).then(res => {
            const data = res.data || {}
            this.form.name = data.name || ''
            this.form.parentId = data.parentId || 0
            this.form.sort = data.sort || 0
            this.form.status = data.status || '0'
            const index = this.parents.findIndex(item => item.categoryId === this.form.parentId)
            this.parentIndex = index > -1 ? index + 1 : 0
          }).catch(() => {})
        }
      },
      onParentChange(e) {
        this.parentIndex = Number(e.detail.value)
        this.form.parentId = this.parentIndex === 0 ? 0 : this.parents[this.parentIndex - 1].categoryId
      },
      save() {
        if (!this.form.name) {
          this.$modal.msgError('分类名称不能为空')
          return
        }
        const payload = {
          name: this.form.name,
          parentId: this.form.parentId,
          sort: Number(this.form.sort || 0),
          status: this.form.status
        }
        const request = this.isEdit
          ? categoryEdit(Object.assign({ categoryId: Number(this.categoryId) }, payload))
          : categoryAdd(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
          setTimeout(() => this.$tab.navigateBack(), 600)
        }).catch(() => {})
      },
      remove() {
        uni.showModal({
          title: '删除分类',
          content: '确认删除该分类？',
          confirmColor: '#FF3B30',
          success: res => {
            if (!res.confirm) return
            categoryDelete(this.categoryId).then(() => {
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
