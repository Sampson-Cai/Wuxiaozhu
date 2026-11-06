<template>
  <view class="ios-page">
    <view :style="{ height: topOffset + 'px' }"></view>
    <view class="ios-large-title">工作台</view>

    <view v-for="group in groups" :key="group.title">
      <view class="ios-section-title">{{ group.title }}</view>
      <view class="ios-group work-group">
        <view v-for="item in visibleItems(group)" :key="item.label" class="work-item" hover-class="work-item-hover"
          @click="open(item)">
          <view class="ios-tile ios-tile-lg" :style="{ backgroundColor: item.bg }">
            <uni-icons :type="item.icon" size="22" :color="item.color"></uni-icons>
          </view>
          <text class="work-label">{{ item.label }}</text>
          <view v-if="!item.path" class="work-tag">待接入</view>
        </view>
      </view>
    </view>

    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { getNavTopOffset } from '@/utils/nav'

  export default {
    data() {
      return {
        topOffset: 64,
        groups: [
          {
            title: '采购',
            items: [
              { label: '采购单', icon: 'cart-filled', color: '#007AFF', bg: 'rgba(0,122,255,.12)', path: '/pages/purchase/index' },
              { label: '待审批', icon: 'auth-filled', color: '#5856D6', bg: 'rgba(88,86,214,.12)', path: '/pages/purchase/index?status=1' },
              { label: '收货记录', icon: 'download-filled', color: '#34C759', bg: 'rgba(52,199,89,.14)', path: '/pages/receipt/index' },
              { label: '供应商', icon: 'contact-filled', color: '#FF9500', bg: 'rgba(255,149,0,.16)', path: '/pages/supplier/index' }
            ]
          },
          {
            title: '库存',
            items: [
              { label: '库存查询', icon: 'shop-filled', color: '#34C759', bg: 'rgba(52,199,89,.14)', path: '/pages/stock/index' },
              { label: '入库', icon: 'cloud-download-filled', color: '#30B0C7', bg: 'rgba(48,176,199,.14)', path: '/pages/stock/change?type=in' },
              { label: '出库', icon: 'cloud-upload-filled', color: '#FF9500', bg: 'rgba(255,149,0,.16)', path: '/pages/stock/change?type=out' },
              { label: '库存流水', icon: 'list', color: '#8E8E93', bg: 'rgba(120,120,128,.12)', path: '/pages/stock/records' },
              { label: '补货建议', icon: 'refresh-filled', color: '#AF52DE', bg: 'rgba(175,82,222,.14)', path: '/pages/replenish/index' },
              { label: '补货参数', icon: 'tune-filled', color: '#5856D6', bg: 'rgba(88,86,214,.12)', path: '/pages/replenish/param' }
            ]
          },
          {
            title: '档案',
            items: [
              { label: '商品', icon: 'gift-filled', color: '#AF52DE', bg: 'rgba(175,82,222,.14)', path: '/pages/product/index' },
              { label: '商品分类', icon: 'folder-add-filled', color: '#007AFF', bg: 'rgba(0,122,255,.12)', path: '/pages/category/index' },
              { label: '供应商', icon: 'staff-filled', color: '#FF9500', bg: 'rgba(255,149,0,.16)', path: '/pages/supplier/index' },
              { label: '智能检索', icon: 'search', color: '#30B0C7', bg: 'rgba(48,176,199,.14)', path: '/pages/search/index' }
            ]
          },
          {
            title: '财务',
            items: [
              { label: '应付账款', icon: 'wallet-filled', color: '#FF9500', bg: 'rgba(255,149,0,.16)', path: '/pages/payable/index' },
              { label: '付款登记', icon: 'medal-filled', color: '#FF2D55', bg: 'rgba(255,45,85,.12)', path: '/pages/payable/index?filter=unpaid' },
              { label: '导出中心', icon: 'cloud-download-filled', color: '#8E8E93', bg: 'rgba(120,120,128,.12)', path: '/pages/export/index' }
            ]
          },
          {
            title: '系统',
            items: [
              { label: '账号管理', icon: 'person-filled', color: '#007AFF', bg: 'rgba(0,122,255,.12)', path: '/pages/user/index', perm: 'business:user:edit' },
              { label: '角色权限', icon: 'staff-filled', color: '#5856D6', bg: 'rgba(88,86,214,.12)', path: '/pages/role/index', perm: 'business:role:edit' },
              { label: '操作日志', icon: 'bars', color: '#8E8E93', bg: 'rgba(120,120,128,.12)', path: '/pages/log/index', perm: 'business:log:view' },
              { label: '单位信息', icon: 'home-filled', color: '#34C759', bg: 'rgba(52,199,89,.14)', path: '/pages/enterprise/index' }
            ]
          }
        ]
      }
    },
    computed: {
      totalCount() {
        return this.groups.reduce((sum, g) => sum + g.items.length, 0)
      },
      readyCount() {
        return this.groups.reduce((sum, g) => sum + g.items.filter(i => !!i.path).length, 0)
      }
    },
    onLoad() {
      this.topOffset = getNavTopOffset()
    },
    methods: {
      visibleItems(group) {
        return group.items.filter(item => !item.perm || this.hasPerm(item.perm))
      },
      hasPerm(perm) {
        const perms = this.$store.state.user.permissions || []
        return perms.indexOf('*:*:*') > -1 || perms.indexOf(perm) > -1
      },
      open(item) {
        if (!item.path) {
          this.$modal.showToast('接口已就绪，页面待接入')
          return
        }
        this.$tab.navigateTo(item.path)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .work-group {
    display: flex;
    flex-wrap: wrap;
    padding: 28rpx 0 8rpx;
  }

  .work-item {
    position: relative;
    width: 25%;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-sizing: border-box;
    padding: 12rpx 6rpx 24rpx;
    border-radius: 24rpx;
  }

  /* 按压态：整格浅底 + 圆角，内缩 6rpx，避免和相邻格子连成一片 */
  .work-item-hover {
    background-color: rgba(120, 120, 128, 0.08);
  }

  .work-label {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #000;
    text-align: center;
  }

  .work-tag {
    position: absolute;
    top: -4rpx;
    right: 18rpx;
    padding: 0 8rpx;
    height: 28rpx;
    line-height: 28rpx;
    font-size: 18rpx;
    color: #8e8e93;
    background-color: rgba(120, 120, 128, 0.12);
    border-radius: 14rpx;
  }
</style>
