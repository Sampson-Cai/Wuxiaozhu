<template>
  <view class="ios-page">
    <view class="profile-head">
      <view class="profile-avatar">{{ letter }}</view>
      <view class="profile-name">{{ user.userName || '—' }}</view>
      <view class="profile-sub">{{ user.loginName }}</view>
    </view>

    <view class="ios-section-title">基本信息</view>
    <view class="ios-group">
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">姓名</view></view>
        <view class="ios-cell-value">{{ user.userName || '—' }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">登录账号</view></view>
        <view class="ios-cell-value">{{ user.loginName || '—' }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">手机号码</view></view>
        <view class="ios-cell-value">{{ user.phonenumber || '—' }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">邮箱</view></view>
        <view class="ios-cell-value">{{ user.email || '—' }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">部门</view></view>
        <view class="ios-cell-value">{{ user.deptName || '—' }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">账号状态</view></view>
        <view class="ios-badge" :class="user.status === '0' ? 'ios-badge-green' : 'ios-badge-gray'">
          {{ user.status === '0' ? '正常' : '停用' }}
        </view>
      </view>
    </view>

    <view class="ios-section-title">角色</view>
    <view class="ios-group">
      <view v-if="roleNames.length === 0" class="ios-empty" style="padding: 48rpx 0;">未分配角色</view>
      <view v-for="name in roleNames" :key="name" class="ios-cell">
        <view class="ios-tile" style="background-color: rgba(88,86,214,.12)">
          <uni-icons type="staff-filled" size="18" color="#5856D6"></uni-icons>
        </view>
        <view class="ios-cell-main"><view class="ios-cell-title">{{ name }}</view></view>
      </view>
    </view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { userDetail, roleList } from '@/api/business'

  export default {
    data() {
      return {
        user: {},
        roleMap: {},
        roleNames: []
      }
    },
    computed: {
      letter() {
        return (this.user.userName || '仓').slice(0, 1)
      }
    },
    onLoad() {
      this.load()
    },
    methods: {
      load() {
        const userId = this.$store.state.user.id
        if (!userId) {
          this.$modal.msgError('登录状态已失效')
          return
        }
        // 先取角色字典再取账号详情，避免并发导致角色名缺失
        roleList().then(res => {
          const map = {}
          ;(res.rows || []).forEach(role => {
            map[role.roleId] = role.roleName
          })
          this.roleMap = map
        }).catch(() => {}).finally(() => {
          userDetail(userId).then(res => {
            this.user = res.data || {}
            const ids = this.user.roleIds || []
            this.roleNames = ids.map(id => this.roleMap[id]).filter(name => !!name)
          }).catch(() => {})
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .profile-head {
    padding: 48rpx 0 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .profile-avatar {
    width: 128rpx;
    height: 128rpx;
    border-radius: 64rpx;
    background: linear-gradient(135deg, #007aff, #5856d6);
    color: #fff;
    font-size: 52rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .profile-name {
    margin-top: 16rpx;
    font-size: 38rpx;
    font-weight: 600;
  }

  .profile-sub {
    margin-top: 4rpx;
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
  }
</style>
