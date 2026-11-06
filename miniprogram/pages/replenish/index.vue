<template>
  <view class="ios-page">
    <view class="toolbar">
      <view class="chips">
        <view v-for="tab in tabs" :key="tab.value" class="chip" :class="{ 'chip-active': status === tab.value }"
          @click="setStatus(tab.value)" hover-class="tap-hover">{{ tab.label }}</view>
      </view>
      <view class="calc-btn" @click="calc" hover-class="tap-hover">
        <uni-icons type="refresh-filled" size="16" color="#007AFF"></uni-icons>
        <text class="calc-text">重新计算</text>
      </view>
    </view>

    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">暂无补货建议</view>
      <view v-for="item in list" :key="item.suggestionId" class="ios-cell suggestion">
        <view class="ios-tile" style="background-color: rgba(175,82,222,.14)">
          <uni-icons type="refresh-filled" size="18" color="#AF52DE"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ item.productName }}</view>
          <view class="ios-cell-sub">
            建议补 {{ qty(item.suggestQty) }}{{ item.unit || '' }} · 可用 {{ qty(item.availableQty) }} · 在途 {{ qty(item.inTransitQty) }}
          </view>
          <view class="ios-cell-sub">安全库存 {{ qty(item.safetyStock) }} · 日耗 {{ qty(item.dailyOutQty) }}</view>
          <view class="actions" v-if="item.status === '0'">
            <view class="action-btn primary" @click.stop="toPurchase(item)" hover-class="tap-hover">转采购草稿</view>
            <view class="action-btn" @click.stop="ignore(item)" hover-class="tap-hover">忽略</view>
          </view>
          <view v-else class="ios-badge" :class="item.status === '1' ? 'ios-badge-green' : 'ios-badge-gray'"
            style="margin-top: 10rpx;">
            {{ item.status === '1' ? '已建单' : '已忽略' }}
          </view>
        </view>
      </view>
    </view>

    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-loading" v-else-if="finished && list.length > 0">已加载全部 {{ total }} 条</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { replenishSuggestions, replenishToPurchase, replenishIgnore, replenishCalc } from '@/api/business'

  export default {
    data() {
      return {
        status: '0',
        tabs: [
          { label: '待处理', value: '0' },
          { label: '已建单', value: '1' },
          { label: '已忽略', value: '2' }
        ],
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 20,
        loading: false,
        finished: false
      }
    },
    onLoad() {
      this.load()
    },
    onPullDownRefresh() {
      this.reload(() => uni.stopPullDownRefresh())
    },
    onReachBottom() {
      if (!this.finished && !this.loading) {
        this.pageNum += 1
        this.load()
      }
    },
    methods: {
      setStatus(value) {
        if (this.status === value) return
        this.status = value
        this.reload()
      },
      reload(done) {
        this.pageNum = 1
        this.finished = false
        this.list = []
        this.load(done)
      },
      load(done) {
        this.loading = true
        replenishSuggestions({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.status
        }).then(res => {
          const rows = res.rows || []
          this.list = this.pageNum === 1 ? rows : this.list.concat(rows)
          this.total = res.total || 0
          this.finished = this.list.length >= this.total || rows.length === 0
        }).catch(() => {
          this.finished = true
        }).finally(() => {
          this.loading = false
          if (done) done()
        })
      },
      qty(value) {
        const num = Number(value || 0)
        return num % 1 === 0 ? num : num.toFixed(2)
      },
      calc() {
        this.$modal.loading('计算中…')
        replenishCalc().then(res => {
          this.$modal.closeLoading()
          this.$modal.msgSuccess(`已生成 ${res.count || 0} 条建议`)
          this.reload()
        }).catch(() => {
          this.$modal.closeLoading()
        })
      },
      toPurchase(item) {
        uni.showModal({
          title: '生成采购草稿',
          content: `按建议数量 ${this.qty(item.suggestQty)} 生成采购草稿？商品需有默认供应商。`,
          success: res => {
            if (!res.confirm) return
            replenishToPurchase(item.suggestionId).then(result => {
              this.$modal.msgSuccess('已生成采购草稿')
              this.reload()
              setTimeout(() => {
                this.$tab.navigateTo('/pages/purchase/detail?orderId=' + result.orderId)
              }, 600)
            }).catch(() => {})
          }
        })
      },
      ignore(item) {
        replenishIgnore(item.suggestionId).then(() => {
          this.$modal.msgSuccess('已忽略')
          this.reload()
        }).catch(() => {})
      }
    }
  }
</script>

<style lang="scss" scoped>
  .toolbar {
    padding: 16rpx 24rpx 12rpx;
  }

  .chips {
    display: flex;
    margin-bottom: 12rpx;
  }

  .chip {
    height: 68rpx;
    line-height: 68rpx;
    padding: 0 26rpx;
    margin-right: 14rpx;
    border-radius: 34rpx;
    font-size: 26rpx;
    background-color: rgba(120, 120, 128, 0.12);
  }

  .chip-active {
    background-color: #007aff;
    color: #fff;
  }

  .calc-btn {
    display: flex;
    align-items: center;
    align-self: flex-end;
  }

  .calc-text {
    margin-left: 6rpx;
    font-size: 26rpx;
    color: #007aff;
  }

  .suggestion {
    align-items: flex-start;
    padding-top: 24rpx;
    padding-bottom: 24rpx;
  }

  .actions {
    display: flex;
    margin-top: 16rpx;
  }

  .action-btn {
    height: 64rpx;
    line-height: 64rpx;
    padding: 0 24rpx;
    margin-right: 16rpx;
    border-radius: 32rpx;
    font-size: 24rpx;
    background-color: rgba(120, 120, 128, 0.12);
    color: #000;
  }

  .action-btn.primary {
    background-color: rgba(0, 122, 255, 0.12);
    color: #007aff;
  }
</style>
