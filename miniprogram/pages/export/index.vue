<template>
  <view class="ios-page">
    <view class="ios-section-title">Excel 导出</view>
    <view class="ios-group">
      <view class="ios-cell" hover-class="ios-cell-hover" @click="download('purchase')">
        <view class="ios-tile" style="background-color: rgba(0,122,255,.12)">
          <uni-icons type="cart-filled" size="18" color="#007AFF"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title">采购单明细</view>
          <view class="ios-cell-sub">含单号、供应商、金额、状态</view>
        </view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="download('stock')">
        <view class="ios-tile" style="background-color: rgba(52,199,89,.14)">
          <uni-icons type="shop-filled" size="18" color="#34C759"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title">库存台账</view>
          <view class="ios-cell-sub">现存、可用、在途、安全库存</view>
        </view>
        <view class="ios-chevron"></view>
      </view>
      <view class="ios-cell" hover-class="ios-cell-hover" @click="download('payable')">
        <view class="ios-tile" style="background-color: rgba(255,149,0,.16)">
          <uni-icons type="wallet-filled" size="18" color="#FF9500"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title">应付账款</view>
          <view class="ios-cell-sub">应付、已付、未付、到期日</view>
        </view>
        <view class="ios-chevron"></view>
      </view>
    </view>

    <view class="ios-section-title">打印</view>
    <view class="ios-group">
      <view class="ios-cell">
        <view class="ios-tile" style="background-color: rgba(120,120,128,.12)">
          <uni-icons type="paperplane-filled" size="18" color="#8E8E93"></uni-icons>
        </view>
        <view class="ios-cell-main">
          <view class="ios-cell-title">采购单 PDF</view>
          <view class="ios-cell-sub">在采购单详情页右上角“打印”生成并转发</view>
        </view>
      </view>
    </view>

    <view class="ios-section-footer">导出文件为 .xlsx，下载后可直接用系统应用打开或转发。</view>
    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { buildExportUrl } from '@/api/business'

  export default {
    methods: {
      download(type) {
        const url = buildExportUrl(type)
        uni.showLoading({ title: '生成中…' })
        uni.downloadFile({
          url,
          success: res => {
            if (res.statusCode !== 200) {
              this.$modal.msgError('导出失败，请稍后重试')
              return
            }
            uni.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              fail: () => this.$modal.msgError('文件已下载，但无法预览')
            })
          },
          fail: () => this.$modal.msgError('下载失败，请检查网络'),
          complete: () => uni.hideLoading()
        })
      }
    }
  }
</script>
