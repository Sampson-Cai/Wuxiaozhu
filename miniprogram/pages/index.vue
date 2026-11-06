<template>
  <view class="ios-page home">
    <!-- 顶部留白：按胶囊实际位置下移，避免被右上角胶囊遮挡 -->
    <view :style="{ height: topOffset + 'px' }"></view>

    <!-- 大标题 -->
    <view class="home-header">
      <view class="ios-flex-1">
        <view class="home-greeting">{{ greeting }}，{{ userName || '库管员' }}</view>
        <view class="home-title">物小助</view>
      </view>
      <view class="home-avatar" hover-class="ios-cell-hover" @click="go('/pages/mine')">
        <image class="home-avatar-img" src="/static/images/logo.png" mode="aspectFill"></image>
      </view>
    </view>

    <!-- 预警概览 -->
    <view class="stat-grid">
      <view class="stat-card" hover-class="ios-cell-hover" @click="goStock('low')">
        <view class="stat-value" :class="{ 'stat-warn': summary.lowStock > 0 }">{{ summary.lowStock }}</view>
        <view class="stat-label">库存预警</view>
        <view class="stat-dot stat-dot-orange"></view>
      </view>
      <view class="stat-card" hover-class="ios-cell-hover" @click="goPurchase('1')">
        <view class="stat-value" :class="{ 'stat-warn': summary.pendingApproval > 0 }">{{ summary.pendingApproval }}</view>
        <view class="stat-label">待审批</view>
        <view class="stat-dot stat-dot-blue"></view>
      </view>
      <view class="stat-card" hover-class="ios-cell-hover" @click="goPayable('due')">
        <view class="stat-value">{{ summary.payableDue }}</view>
        <view class="stat-label">7 天内到期</view>
        <view class="stat-dot stat-dot-yellow"></view>
      </view>
      <view class="stat-card" hover-class="ios-cell-hover" @click="goPayable('overdue')">
        <view class="stat-value" :class="{ 'stat-danger': summary.payableOverdue > 0 }">{{ summary.payableOverdue }}</view>
        <view class="stat-label">已逾期应付</view>
        <view class="stat-dot stat-dot-red"></view>
      </view>
    </view>

    <!-- 助手对话 -->
    <view class="ios-section-title">助手</view>
    <view class="chat-card">
      <view class="chat-head">
        <image class="chat-avatar" src="/static/images/logo.png" mode="aspectFill"></image>
        <view class="ios-flex-1">
          <view class="chat-name">物小助助手</view>
          <view class="chat-sub">{{ aiEnabled ? '知识库 + 实时业务数据' : '实时业务数据问答' }}</view>
        </view>
        <view class="chat-clear" hover-class="ios-cell-hover" @click="clearChat">清空</view>
      </view>

      <scroll-view class="chat-body" scroll-y :scroll-into-view="scrollTo" :scroll-with-animation="true"
        :enhanced="true" :bounces="false">
        <view v-for="(msg, index) in messages" :id="'m' + index" :key="msg.id" class="msg" :class="msg.role">
          <view class="bubble">
            <view v-if="msg.streaming && !msg.text" class="bubble-typing">
              <text class="status-text">{{ statusLabel(msg) }}</text>
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>
            <text v-else class="bubble-text">{{ msg.text }}</text>
            <text v-if="msg.streaming && msg.text" class="stream-cursor">▍</text>
            <view v-if="msg.sources && msg.sources.length" class="bubble-sources">
              <text class="source-label">参考</text>
              <text v-for="(src, si) in msg.sources" :key="si" class="source-chip">{{ src.title }}</text>
            </view>
            <view v-if="msg.items && msg.items.length" class="bubble-list">
              <view v-for="(item, idx) in msg.items" :key="idx" class="bubble-item" hover-class="bubble-item-hover"
                @click="openItem(item)">
                <view class="bubble-item-main">
                  <view class="bubble-item-title ios-ellipsis">{{ item.title }}</view>
                  <view class="bubble-item-sub ios-ellipsis">{{ item.sub }}</view>
                </view>
                <view class="bubble-item-value">{{ item.value }}</view>
              </view>
            </view>
            <view v-if="msg.link" class="bubble-link" hover-class="bubble-link-hover" @click="go(msg.link.path)">
              {{ msg.link.label }} ›
            </view>
          </view>
        </view>
        <view v-if="thinking" id="thinking" class="msg bot">
          <view class="bubble typing">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </scroll-view>

      <scroll-view class="chat-chips" scroll-x :show-scrollbar="false">
        <view v-for="item in suggestions" :key="item" class="chip" hover-class="chip-hover" @click="send(item)">
          {{ item }}
        </view>
      </scroll-view>

      <view class="chat-input">
        <input v-model="draft" class="chat-input-field" placeholder="问点什么，比如 库存预警" placeholder-class="ios-placeholder"
          confirm-type="send" :cursor-spacing="24" :hold-keyboard="true" :adjust-position="true" @confirm="send()" />
        <view class="chat-send" :class="{ 'chat-send-active': draft.length > 0 }" @click="send()">
          <uni-icons type="paperplane-filled" size="17" color="#FFFFFF"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 常用操作 -->
    <view class="ios-section-title">常用操作</view>
    <view class="ios-group quick-grid">
      <view v-for="action in quickActions" :key="action.label" class="quick-item" hover-class="quick-item-hover"
        @click="go(action.path)">
        <view class="ios-tile ios-tile-lg" :style="{ backgroundColor: action.bg }">
          <uni-icons :type="action.icon" size="22" :color="action.color"></uni-icons>
        </view>
        <text class="quick-label">{{ action.label }}</text>
      </view>
    </view>

    <view class="ios-safe-bottom"></view>
  </view>
</template>

<script>
  import { dashboardSummary, aiChat, aiStatus } from '@/api/business'
  import { getToken } from '@/utils/auth'
  import { getNavTopOffset } from '@/utils/nav'
  import { ask, SUGGESTIONS } from '@/utils/assistant'
  import { streamChat } from '@/utils/aiStream'

  export default {
    data() {
      return {
        topOffset: 64,
        userName: '',
        summary: {
          lowStock: 0,
          pendingApproval: 0,
          payableDue: 0,
          payableOverdue: 0
        },
        suggestions: SUGGESTIONS,
        messages: [],
        draft: '',
        thinking: false,
        scrollTo: '',
        seq: 0,
        sessionId: null,
        aiEnabled: false,
        streamTask: null,
        lastScrollAt: 0,
        typeTimer: null,
        typeQueue: '',
        typeEnded: false,
        typeMsg: null,
        quickActions: [
          { label: '采购单', icon: 'cart-filled', color: '#007AFF', bg: 'rgba(0,122,255,.12)', path: '/pages/purchase/index' },
          { label: '库存', icon: 'shop-filled', color: '#34C759', bg: 'rgba(52,199,89,.14)', path: '/pages/stock/index' },
          { label: '商品', icon: 'gift-filled', color: '#AF52DE', bg: 'rgba(175,82,222,.14)', path: '/pages/product/index' },
          { label: '应付', icon: 'wallet-filled', color: '#FF9500', bg: 'rgba(255,149,0,.16)', path: '/pages/payable/index' }
        ]
      }
    },
    computed: {
      greeting() {
        const hour = new Date().getHours()
        if (hour < 6) return '夜深了'
        if (hour < 11) return '早上好'
        if (hour < 14) return '中午好'
        if (hour < 18) return '下午好'
        return '晚上好'
      }
    },
    onLoad() {
      this.topOffset = getNavTopOffset()
      this.userName = this.$store.state.user.name || ''
      this.pushBot({
        text: '你好，我是物小助助手。可以问库存、采购、应付，也能问物料规格与标准，答案来自知识库与实时业务数据。'
      })
    },
    onShow() {
      if (!getToken()) {
        this.$tab.reLaunch('/pages/login')
        return
      }
      this.userName = this.$store.state.user.name || this.userName
      this.loadData()
      this.checkAi()
    },
    onPullDownRefresh() {
      this.loadData(() => uni.stopPullDownRefresh())
    },
    methods: {
      loadData(done) {
        dashboardSummary().then(res => {
          this.summary = {
            lowStock: res.lowStock || 0,
            pendingApproval: res.pendingApproval || 0,
            payableDue: res.payableDue || 0,
            payableOverdue: res.payableOverdue || 0
          }
        }).catch(() => {}).finally(() => {
          if (done) done()
        })
      },
      nextId() {
        this.seq += 1
        return 'msg-' + this.seq
      },
      /**
       * 探测后端 AI 是否可用，不可用则走本地数据问答
       */
      checkAi() {
        aiStatus().then(res => {
          const data = res.data || {}
          this.aiEnabled = !!data.enabled
        }).catch(() => {
          this.aiEnabled = false
        })
      },
      pushBot(payload) {
        this.messages.push({
          id: this.nextId(),
          role: 'bot',
          text: payload.text,
          items: payload.items || null,
          link: payload.link || null,
          sources: payload.sources || null,
          local: !!payload.local
        })
        this.scrollToBottom()
      },
      scrollToBottom() {
        // 每条消息 id 唯一，直接赋值即可触发滚动；不要先清空再赋值，会抖一下
        this.$nextTick(() => {
          this.scrollTo = this.thinking ? 'thinking' : 'm' + (this.messages.length - 1)
        })
      },
      send(text) {
        const question = (text || this.draft || '').trim()
        if (!question) return
        if (this.thinking || this.streamTask) return
        this.messages.push({ id: this.nextId(), role: 'user', text: question })
        this.draft = ''
        this.thinking = true
        this.scrollToBottom()

        // 后端 AI 不可用时，回落到本地数据问答（同样只查真实接口，不编造）
        const localAnswer = () => {
          ask(question).then(answer => {
            answer.local = true
            this.thinking = false
            this.pushBot(answer)
          }).catch(() => {
            this.thinking = false
            this.pushBot({ text: '查询失败了，请检查网络或稍后再试。' })
          })
        }

        if (!this.aiEnabled) {
          localAnswer()
          return
        }

        // 流式：先出会话与来源，再逐字追加
        let bot = null
        const ensureBot = () => {
          if (!bot) {
            bot = { id: this.nextId(), role: 'bot', text: '', sources: null, streaming: true, phase: 'searching' }
            this.messages.push(bot)
            this.thinking = false
            this.startTypewriter(bot)
          }
          return bot
        }

        this.streamTask = streamChat({
          sessionId: this.sessionId,
          question,
          onMeta: meta => {
            this.sessionId = (meta && meta.sessionId) || this.sessionId
            ensureBot()
            this.scrollToBottom()
          },
          onSources: list => {
            const item = ensureBot()
            item.sources = list || []
            item.phase = 'thinking'
            this.scrollToBottom()
          },
          onDelta: piece => {
            const item = ensureBot()
            if (item.phase !== 'typing') item.phase = 'typing'
            // 先进队列，由打字机按节奏吐出，避免整段瞬间刷屏
            this.typeQueue += piece
          },
          onDone: () => {
            this.streamTask = null
            this.thinking = false
            this.typeEnded = true
          },
          onError: msg => {
            this.streamTask = null
            this.thinking = false
            // 已经出了一部分字：保留内容，标注中断
            if (bot && (bot.text || this.typeQueue)) {
              this.typeQueue += '\n\n（回答中断：' + msg + '）'
              this.typeEnded = true
              return
            }
            // 一个字都没收到：回落到非流式接口
            this.stopTypewriter()
            if (bot) {
              const index = this.messages.indexOf(bot)
              if (index > -1) this.messages.splice(index, 1)
            }
            aiChat(this.sessionId, question).then(res => {
              const data = res.data || {}
              this.sessionId = data.sessionId || this.sessionId
              this.pushBot({
                text: data.answer || '没有拿到回答，换个问法试试。',
                sources: data.sources || []
              })
            }).catch(() => {
              localAnswer()
            })
          }
        })
      },

      /** 首字到达前的状态文案，让等待有明确反馈 */
      statusLabel(msg) {
        if (msg.phase === 'searching') return '正在检索知识库'
        if (msg.phase === 'thinking') return '正在组织答案'
        return '正在输入'
      },

      /** 启动打字机：按固定节奏把队列里的字吐到气泡上 */
      startTypewriter(bot) {
        this.stopTypewriter()
        this.typeMsg = bot
        this.typeQueue = ''
        this.typeEnded = false
        this.typeTimer = setInterval(() => this.pumpTypewriter(), 33)
      },

      stopTypewriter() {
        if (this.typeTimer) {
          clearInterval(this.typeTimer)
          this.typeTimer = null
        }
        this.typeQueue = ''
        this.typeEnded = false
        this.typeMsg = null
      },

      pumpTypewriter() {
        const bot = this.typeMsg
        if (!bot) return
        if (!this.typeQueue) {
          if (this.typeEnded) {
            bot.streaming = false
            this.stopTypewriter()
            this.scrollToBottom()
          }
          return
        }
        // 基础 2 字/33ms（约 60 字/秒），积压越多逐步加速，上限 8 字/33ms（约 240 字/秒）
        // 模型实际出字速度能到 300+ 字/秒，不压一下就会整段刷屏
        const step = Math.max(2, Math.min(8, 2 + Math.floor(this.typeQueue.length / 60)))
        bot.text += this.typeQueue.slice(0, step)
        this.typeQueue = this.typeQueue.slice(step)
        this.scheduleScroll()
      },

      /** 流式输出时限制滚动频率，避免每个字都触发一次 */
      scheduleScroll() {
        const now = Date.now()
        if (now - this.lastScrollAt < 250) return
        this.lastScrollAt = now
        this.scrollToBottom()
      },
      clearChat() {
        if (this.streamTask) {
          this.streamTask.abort()
          this.streamTask = null
        }
        this.stopTypewriter()
        this.thinking = false
        this.messages = []
        this.sessionId = null
        this.pushBot({ text: '已清空。可以继续问我库存、采购、应付或物料规格。' })
      },
      openItem(item) {
        if (item && item.path) this.go(item.path)
      },
      go(path) {
        if (!path) return
        const clean = path.split('?')[0]
        // tabBar 页面只能用 switchTab，navigateTo 在真机上会失败
        if (['/pages/index', '/pages/work/index', '/pages/mine'].indexOf(clean) > -1) {
          this.$tab.switchTab(clean)
          return
        }
        this.$tab.navigateTo(path)
      },
      goStock(filter) {
        this.$tab.navigateTo('/pages/stock/index?lowStockOnly=' + (filter === 'low' ? '1' : '0'))
      },
      goPurchase(status) {
        this.$tab.navigateTo('/pages/purchase/index?status=' + status)
      },
      goPayable(filter) {
        this.$tab.navigateTo('/pages/payable/index?filter=' + filter)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .home-header {
    display: flex;
    align-items: center;
    padding: 12rpx 32rpx 24rpx;
  }

  .home-greeting {
    font-size: 28rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .home-title {
    margin-top: 4rpx;
    font-size: 68rpx;
    line-height: 82rpx;
    font-weight: 700;
    letter-spacing: -1rpx;
  }

  .home-avatar {
    width: 84rpx;
    height: 84rpx;
    border-radius: 42rpx;
    background-color: #fff;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }

  .home-avatar-img {
    width: 84rpx;
    height: 84rpx;
  }

  .stat-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 0 18rpx;
  }

  .stat-card {
    position: relative;
    width: calc(50% - 12rpx);
    margin: 0 6rpx 12rpx;
    padding: 26rpx 28rpx;
    box-sizing: border-box;
    background-color: #fff;
    border-radius: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  }

  .stat-value {
    font-size: 52rpx;
    line-height: 60rpx;
    font-weight: 700;
    letter-spacing: -1rpx;
  }

  .stat-warn {
    color: #ff9500;
  }

  .stat-danger {
    color: #ff3b30;
  }

  .stat-label {
    margin-top: 6rpx;
    font-size: 26rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .stat-dot {
    position: absolute;
    top: 30rpx;
    right: 28rpx;
    width: 12rpx;
    height: 12rpx;
    border-radius: 6rpx;
  }

  .stat-dot-orange { background-color: #ff9500; }
  .stat-dot-blue { background-color: #007aff; }
  .stat-dot-yellow { background-color: #ffcc00; }
  .stat-dot-red { background-color: #ff3b30; }

  /* ---------------- 助手对话 ---------------- */
  .chat-card {
    margin: 0 24rpx;
    padding: 22rpx 22rpx 18rpx;
    background-color: #fff;
    border-radius: 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  }

  .chat-head {
    display: flex;
    align-items: center;
    padding-bottom: 16rpx;
  }

  .chat-avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 20rpx;
    margin-right: 16rpx;
    background-color: #f2f2f7;
  }

  .chat-name {
    font-size: 28rpx;
    font-weight: 600;
    color: #000;
  }

  .chat-sub {
    margin-top: 2rpx;
    font-size: 22rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .chat-clear {
    padding: 8rpx 18rpx;
    border-radius: 999rpx;
    font-size: 22rpx;
    color: rgba(60, 60, 67, 0.6);
    background-color: rgba(120, 120, 128, 0.08);
  }

  .chat-body {
    height: 440rpx;
    padding: 8rpx 2rpx 0;
    box-sizing: border-box;
  }

  .msg {
    display: flex;
    margin-bottom: 16rpx;
  }

  .msg.user {
    justify-content: flex-end;
  }

  .bubble {
    max-width: 86%;
    padding: 18rpx 22rpx;
    border-radius: 24rpx;
    box-sizing: border-box;
  }

  .msg.bot .bubble {
    background-color: #f2f2f7;
    border-top-left-radius: 8rpx;
  }

  .msg.user .bubble {
    background-color: #007aff;
    border-top-right-radius: 8rpx;
  }

  .bubble-text {
    font-size: 27rpx;
    line-height: 40rpx;
    color: #000;
    word-break: break-all;
  }

  .msg.user .bubble-text {
    color: #fff;
  }

  .bubble-list {
    margin-top: 12rpx;
    border-radius: 16rpx;
    background-color: #fff;
    overflow: hidden;
  }

  .bubble-typing {
    display: flex;
    align-items: center;
    height: 40rpx;
  }

  .status-text {
    margin-right: 12rpx;
    font-size: 24rpx;
    color: rgba(60, 60, 67, 0.55);
  }

  .stream-cursor {
    margin-left: 2rpx;
    color: #007aff;
    font-size: 26rpx;
    animation: blink 1s infinite ease-in-out;
  }

  .bubble-sources {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    margin-top: 12rpx;
  }

  .source-label {
    margin-right: 10rpx;
    font-size: 20rpx;
    color: rgba(60, 60, 67, 0.45);
  }

  .source-chip {
    margin: 4rpx 8rpx 4rpx 0;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
    font-size: 20rpx;
    color: #5856d6;
    background-color: rgba(88, 86, 214, 0.1);
  }

  .bubble-item {
    display: flex;
    align-items: center;
    padding: 16rpx 18rpx;
  }

  .bubble-item + .bubble-item {
    border-top: 1rpx solid rgba(60, 60, 67, 0.12);
  }

  .bubble-item-hover {
    background-color: rgba(120, 120, 128, 0.08);
  }

  .bubble-item-main {
    flex: 1;
    min-width: 0;
  }

  .bubble-item-title {
    font-size: 26rpx;
    font-weight: 600;
    color: #000;
  }

  .bubble-item-sub {
    margin-top: 4rpx;
    font-size: 22rpx;
    color: rgba(60, 60, 67, 0.6);
  }

  .bubble-item-value {
    margin-left: 16rpx;
    font-size: 24rpx;
    font-weight: 600;
    color: #007aff;
    flex-shrink: 0;
  }

  .bubble-link {
    margin-top: 14rpx;
    font-size: 24rpx;
    font-weight: 600;
    color: #007aff;
  }

  .bubble-link-hover {
    opacity: 0.6;
  }

  .typing {
    display: flex;
    align-items: center;
    padding: 22rpx;
  }

  .dot {
    width: 12rpx;
    height: 12rpx;
    margin-right: 10rpx;
    border-radius: 6rpx;
    background-color: rgba(60, 60, 67, 0.4);
    animation: blink 1.2s infinite ease-in-out;
  }

  .dot:nth-child(2) { animation-delay: 0.2s; }
  .dot:nth-child(3) { animation-delay: 0.4s; margin-right: 0; }

  @keyframes blink {
    0%, 80%, 100% { opacity: 0.25; }
    40% { opacity: 1; }
  }

  .chat-chips {
    margin-top: 16rpx;
    white-space: nowrap;
  }

  .chip {
    display: inline-block;
    padding: 10rpx 22rpx;
    margin-right: 12rpx;
    border-radius: 999rpx;
    font-size: 24rpx;
    color: #007aff;
    background-color: rgba(0, 122, 255, 0.1);
  }

  .chip-hover {
    background-color: rgba(0, 122, 255, 0.2);
  }

  .chat-input {
    display: flex;
    align-items: center;
    margin-top: 16rpx;
    padding: 8rpx 8rpx 8rpx 26rpx;
    border-radius: 999rpx;
    background-color: #f2f2f7;
  }

  .chat-input-field {
    flex: 1;
    height: 56rpx;
    font-size: 27rpx;
    color: #000;
  }

  .chat-send {
    width: 72rpx;
    height: 72rpx;
    margin-left: 12rpx;
    border-radius: 36rpx;
    background-color: rgba(0, 122, 255, 0.35);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .chat-send-active {
    background-color: #007aff;
  }

  .quick-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 28rpx 0 12rpx;
  }

  .quick-item {
    width: 25%;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-sizing: border-box;
    padding: 12rpx 6rpx 20rpx;
    border-radius: 24rpx;
  }

  .quick-item-hover {
    background-color: rgba(120, 120, 128, 0.08);
  }

  .quick-label {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #000;
  }
</style>
