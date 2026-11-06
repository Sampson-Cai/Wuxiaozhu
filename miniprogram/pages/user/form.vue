<template>
  <view class="ios-page">
    <view class="ios-section-title">{{ isEdit ? '编辑账号' : '新增账号' }}</view>
    <view class="ios-group">
      <view class="ios-form-field">
        <text class="ios-form-label">登录账号</text>
        <input v-model="form.loginName" class="ios-form-input" :disabled="isEdit" placeholder="2-20 位"
          placeholder-class="ios-form-placeholder" maxlength="20" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">姓名</text>
        <input v-model="form.userName" class="ios-form-input" placeholder="必填" placeholder-class="ios-form-placeholder"
          maxlength="30" />
      </view>
      <view v-if="!isEdit" class="ios-form-field">
        <text class="ios-form-label">初始密码</text>
        <input v-model="form.password" class="ios-form-input" type="password" password placeholder="不少于 6 位"
          placeholder-class="ios-form-placeholder" maxlength="20" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">手机号</text>
        <input v-model="form.phonenumber" class="ios-form-input" type="number" placeholder="选填"
          placeholder-class="ios-form-placeholder" maxlength="11" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">邮箱</text>
        <input v-model="form.email" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
      <view class="ios-form-field">
        <text class="ios-form-label">备注</text>
        <input v-model="form.remark" class="ios-form-input" placeholder="选填" placeholder-class="ios-form-placeholder" />
      </view>
    </view>

    <view class="ios-section-title">角色</view>
    <view class="ios-group">
      <view v-if="roles.length === 0" class="ios-empty" style="padding: 48rpx 0;">暂无可分配角色</view>
      <view v-for="role in roles" :key="role.roleId" class="ios-cell" hover-class="ios-cell-hover"
        @click="toggleRole(role.roleId)">
        <view class="ios-cell-main">
          <view class="ios-cell-title">{{ role.roleName }}</view>
          <view class="ios-cell-sub">{{ role.roleKey }}</view>
        </view>
        <uni-icons v-if="form.roleIds.indexOf(role.roleId) > -1" type="checkmarkempty" size="20" color="#007AFF"></uni-icons>
      </view>
    </view>

    <view class="form-actions">
      <view class="ios-btn ios-btn-primary" hover-class="ios-btn-hover" @click="save">保存</view>
    </view>

    <view v-if="isEdit" class="ios-group" style="margin-top: 40rpx;">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="resetPassword">
        <view class="ios-cell-main">
          <view class="ios-cell-title">重置密码</view>
          <view class="ios-cell-sub">重置后需用新密码登录</view>
        </view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="toggleStatus">
        <view class="ios-cell-main">
          <view class="ios-cell-title">{{ form.status === '0' ? '停用账号' : '启用账号' }}</view>
          <view class="ios-cell-sub">停用后无法登录，历史单据不受影响</view>
        </view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { userDetail, userAdd, userEdit, userResetPwd, userStatus, userAssignRoles, roleList } from '@/api/business'

  export default {
    data() {
      return {
        userId: null,
        form: {
          loginName: '',
          userName: '',
          password: '',
          phonenumber: '',
          email: '',
          remark: '',
          status: '0',
          roleIds: []
        },
        roles: []
      }
    },
    computed: {
      isEdit() {
        return !!this.userId
      }
    },
    onLoad(options) {
      if (options && options.userId) {
        this.userId = options.userId
      }
      this.loadRoles()
    },
    methods: {
      loadRoles() {
        roleList().then(res => {
          this.roles = res.rows || []
          if (this.isEdit) this.loadDetail()
        }).catch(() => {
          if (this.isEdit) this.loadDetail()
        })
      },
      loadDetail() {
        userDetail(this.userId).then(res => {
          const data = res.data || {}
          this.form.loginName = data.loginName || ''
          this.form.userName = data.userName || ''
          this.form.phonenumber = data.phonenumber || ''
          this.form.email = data.email || ''
          this.form.remark = data.remark || ''
          this.form.status = data.status || '0'
          this.form.roleIds = data.roleIds || []
        }).catch(() => {})
      },
      toggleRole(roleId) {
        const index = this.form.roleIds.indexOf(roleId)
        if (index > -1) {
          this.form.roleIds.splice(index, 1)
        } else {
          this.form.roleIds.push(roleId)
        }
      },
      save() {
        if (!this.form.userName) {
          this.$modal.msgError('姓名不能为空')
          return
        }
        if (!this.isEdit) {
          if (!this.form.loginName) {
            this.$modal.msgError('登录账号不能为空')
            return
          }
          if (!this.form.password || this.form.password.length < 6) {
            this.$modal.msgError('初始密码不能少于 6 位')
            return
          }
        }
        const payload = {
          userName: this.form.userName,
          phonenumber: this.form.phonenumber,
          email: this.form.email,
          remark: this.form.remark,
          roleIds: this.form.roleIds
        }
        if (!this.isEdit) {
          payload.loginName = this.form.loginName
          payload.password = this.form.password
        }
        const done = () => {
          this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
          setTimeout(() => this.$tab.navigateBack(), 600)
        }
        if (this.isEdit) {
          // 改资料与改角色是两个接口，角色单独提交
          userEdit(this.userId, payload)
            .then(() => userAssignRoles(this.userId, this.form.roleIds))
            .then(done)
            .catch(() => {})
        } else {
          userAdd(payload).then(done).catch(() => {})
        }
      },
      resetPassword() {
        uni.showModal({
          title: '重置密码',
          editable: true,
          placeholderText: '请输入新密码（不少于 6 位）',
          success: res => {
            if (!res.confirm) return
            const password = res.content || ''
            if (password.length < 6) {
              this.$modal.msgError('新密码不能少于 6 位')
              return
            }
            userResetPwd(this.userId, password).then(() => {
              this.$modal.msgSuccess('密码已重置')
            }).catch(() => {})
          }
        })
      },
      toggleStatus() {
        const next = this.form.status === '0' ? '1' : '0'
        userStatus(this.userId, next).then(() => {
          this.form.status = next
          this.$modal.msgSuccess('状态已更新')
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
