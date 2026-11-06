<template>
  <view class="ios-page">
    <view class="ios-section-title">{{ isEdit ? '编辑角色' : '新增角色' }}</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">角色名称</text>
        <input v-model="form.roleName" class="ios-form-input" placeholder="如 库管员" placeholder-class="ios-form-placeholder"
          maxlength="30" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">权限字符</text>
        <input v-model="form.roleKey" class="ios-form-input" placeholder="如 keeper" placeholder-class="ios-form-placeholder"
          maxlength="100" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">排序</text>
        <input v-model="form.roleSort" class="ios-form-input" type="number" placeholder="数字越小越靠前"
          placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">启用</text>
        <switch :checked="form.status === '0'" color="#34C759" @change="onStatusChange" />
      </view>
    </view>

    <view class="ios-section-title">权限点</view>
    <view class="ios-group">
      <view v-if="menus.length === 0" class="ios-empty" style="padding: 48rpx 0;">暂无权限点</view>
      <view v-for="menu in menus" :key="menu.menuId" class="ios-cell" hover-class="ios-cell-hover"
        @click="toggleMenu(menu.menuId)">
        <view class="ios-cell-main">
          <view class="ios-cell-title">{{ menu.menuName }}</view>
          <view class="ios-cell-sub">{{ menu.perms }}</view>
        </view>
        <uni-icons v-if="form.menuIds.indexOf(menu.menuId) > -1" type="checkmarkempty" size="20" color="#007AFF"></uni-icons>
      </view>
    </view>

    <view class="form-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="save">保存</view>
    </view>

    <view v-if="isEdit" class="ios-group" style="margin-top: 40rpx;">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="remove">
        <view class="ios-cell-main">
          <view class="ios-cell-title" style="color: #ff3b30;">删除角色</view>
          <view class="ios-cell-sub">仍有账号使用该角色时不可删除</view>
        </view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { roleMenus, roleDetail, roleAdd, roleEdit, roleDelete } from '@/api/business'

  export default {
    data() {
      return {
        roleId: null,
        form: {
          roleName: '',
          roleKey: '',
          roleSort: 0,
          status: '0',
          menuIds: []
        },
        menus: []
      }
    },
    computed: {
      isEdit() {
        return !!this.roleId
      }
    },
    onLoad(options) {
      if (options && options.roleId) {
        this.roleId = options.roleId
      }
      this.loadMenus()
    },
    methods: {
      onStatusChange(e) {
        this.form.status = e.detail.value ? '0' : '1'
      },
      loadMenus() {
        roleMenus().then(res => {
          this.menus = res.rows || []
          if (this.isEdit) this.loadDetail()
        }).catch(() => {
          if (this.isEdit) this.loadDetail()
        })
      },
      loadDetail() {
        roleDetail(this.roleId).then(res => {
          const data = res.data || {}
          this.form.roleName = data.roleName || ''
          this.form.roleKey = data.roleKey || ''
          this.form.roleSort = data.roleSort || 0
          this.form.status = data.status || '0'
          this.form.menuIds = data.menuIds || []
        }).catch(() => {})
      },
      toggleMenu(menuId) {
        const index = this.form.menuIds.indexOf(menuId)
        if (index > -1) {
          this.form.menuIds.splice(index, 1)
        } else {
          this.form.menuIds.push(menuId)
        }
      },
      save() {
        if (!this.form.roleName || !this.form.roleKey) {
          this.$modal.msgError('角色名称和权限字符不能为空')
          return
        }
        // 勾选权限点时自动带上父目录 2000
        const menuIds = this.form.menuIds.slice()
        if (menuIds.length > 0 && menuIds.indexOf(2000) === -1) {
          menuIds.push(2000)
        }
        const payload = {
          roleName: this.form.roleName,
          roleKey: this.form.roleKey,
          roleSort: String(this.form.roleSort || 0),
          status: this.form.status,
          dataScope: '1',
          menuIds
        }
        const request = this.isEdit
          ? roleEdit(Object.assign({ roleId: Number(this.roleId) }, payload))
          : roleAdd(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
          setTimeout(() => this.$tab.navigateBack(), 600)
        }).catch(() => {})
      },
      remove() {
        uni.showModal({
          title: '删除角色',
          content: '确认删除该角色？',
          confirmColor: '#FF3B30',
          success: res => {
            if (!res.confirm) return
            roleDelete(this.roleId).then(() => {
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
