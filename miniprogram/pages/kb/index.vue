<template>
  <view class="ios-page">
    <view class="stat-row">
      <view class="stat-box">
        <view class="stat-num">{{ stats.documents || 0 }}</view>
        <view class="stat-cap">文档</view>
      </view>
      <view class="stat-box">
        <view class="stat-num">{{ stats.chunks || 0 }}</view>
        <view class="stat-cap">片段</view>
      </view>
      <view class="stat-box">
        <view class="stat-num">{{ stats.readyDocuments || 0 }}</view>
        <view class="stat-cap">已就绪</view>
      </view>
    </view>

    <view class="ios-section-title">模型</view>
    <view class="ios-group">
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">对话模型</view></view>
        <view class="ios-cell-value">{{ stats.chatModel || '-' }}</view>
      </view>
      <view class="ios-cell">
        <view class="ios-cell-main"><view class="ios-cell-title">向量模型</view></view>
        <view class="ios-cell-value">{{ shortModel(stats.embeddingModel) }}</view>
      </view>
    </view>

    <view class="ios-section-title">文档</view>
    <view class="ios-group">
      <view v-if="list.length === 0 && !loading" class="ios-empty">还没有知识库文档</view>
      <view v-for="doc in list" :key="doc.documentId" class="ios-cell" hover-class="ios-cell-hover"
        @click="openActions(doc)">
        <view class="ios-tile" :style="{ backgroundColor: bgOf(doc.status) }">
          <uni-icons type="paperplane-filled" size="18" :color="colorOf(doc.status)"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title ios-ellipsis">{{ doc.title }}</view>
          <view class="ios-cell-sub ios-ellipsis">
            {{ doc.docType || '资料' }} · {{ doc.chunkCount || 0 }} 段 · {{ (doc.createTime || '').slice(0, 10) }}
          </view>
        </view>
        <view class="ios-badge" :class="badgeOf(doc.status)">{{ statusText(doc.status) }}</view>
      </view>
    </view>

    <view class="ios-section-footer">
      导入新文档：把文件放到服务器后调用「知识库导入」接口即可自动切片与向量化；点文档可重建向量或删除。
    </view>
    <view class="ios-loading" v-if="loading">加载中…</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { aiStatus, aiKbDocuments, aiKbReindex, aiKbDelete } from '@/api/business'

  export default {
    data() {
      return {
        stats: {},
        list: [],
        loading: false
      }
    },
    onShow() {
      this.load()
    },
    onPullDownRefresh() {
      this.load(() => uni.stopPullDownRefresh())
    },
    methods: {
      load(done) {
        this.loading = true
        aiStatus().then(res => {
          this.stats = res.data || {}
        }).catch(() => {})
        aiKbDocuments().then(res => {
          this.list = res.rows || []
        }).catch(() => {}).finally(() => {
          this.loading = false
          if (done) done()
        })
      },
      shortModel(model) {
        if (!model) return '-'
        return model.indexOf('/') > -1 ? model.split('/')[1] : model
      },
      statusText(status) {
        return status === '1' ? '已就绪' : (status === '2' ? '失败' : '处理中')
      },
      badgeOf(status) {
        return status === '1' ? 'ios-badge-green' : (status === '2' ? 'ios-badge-red' : 'ios-badge-orange')
      },
      bgOf(status) {
        if (status === '2') return 'rgba(255,59,48,.12)'
        if (status === '1') return 'rgba(88,86,214,.12)'
        return 'rgba(255,149,0,.16)'
      },
      colorOf(status) {
        if (status === '2') return '#FF3B30'
        if (status === '1') return '#5856D6'
        return '#FF9500'
      },
      openActions(doc) {
        uni.showActionSheet({
          itemList: ['重新向量化', '删除文档'],
          success: res => {
            if (res.tapIndex === 0) {
              this.reindex(doc)
            } else if (res.tapIndex === 1) {
              this.remove(doc)
            }
          }
        })
      },
      reindex(doc) {
        uni.showLoading({ title: '向量化中…', mask: true })
        aiKbReindex(doc.documentId).then(() => {
          uni.hideLoading()
          this.$modal.msgSuccess('已重建向量')
          this.load()
        }).catch(() => {
          uni.hideLoading()
        })
      },
      remove(doc) {
        this.$modal.confirm('删除后该文档的片段与向量一并清除，确定删除？').then(() => {
          aiKbDelete(doc.documentId).then(() => {
            this.$modal.msgSuccess('已删除')
            this.load()
          }).catch(() => {})
        }).catch(() => {})
      }
    }
  }
</script>

<style lang="scss" scoped>
  .stat-row {
    display: flex;
    padding: 24rpx 24rpx 8rpx;
  }

  .stat-box {
    flex: 1;
    margin-right: 16rpx;
    padding: 26rpx 20rpx;
    background-color: #fff;
    border-radius: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    text-align: center;
  }

  .stat-box:last-child {
    margin-right: 0;
  }

  .stat-num {
    font-size: 44rpx;
    font-weight: 700;
    color: #000;
  }

  .stat-cap {
    margin-top: 6rpx;
    font-size: 22rpx;
    color: rgba(60, 60, 67, 0.6);
  }
</style>
