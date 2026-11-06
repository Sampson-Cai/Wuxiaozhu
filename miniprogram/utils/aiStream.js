import { getBaseUrl } from '@/utils/server'
import { getToken } from '@/utils/auth'

/**
 * AI 流式问答（SSE）
 *
 * 小程序端 uni.request 不支持读取流，需要 enableChunked + onChunkReceived，
 * 这里把二进制分片按 UTF-8 解码、按 SSE 帧切分，再回调给页面。
 */

const buildRequestId = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 有状态的 UTF-8 解码器：分片可能把一个汉字切成两半，未完成的部分留到下次
 */
function createUtf8Decoder() {
  let pending = []
  return function decode(buffer) {
    const bytes = new Uint8Array(buffer)
    let all
    if (pending.length) {
      all = new Uint8Array(pending.length + bytes.length)
      all.set(pending, 0)
      all.set(bytes, pending.length)
    } else {
      all = bytes
    }
    let out = ''
    let i = 0
    while (i < all.length) {
      const b = all[i]
      let len = 0
      if (b < 0x80) len = 1
      else if ((b & 0xe0) === 0xc0) len = 2
      else if ((b & 0xf0) === 0xe0) len = 3
      else if ((b & 0xf8) === 0xf0) len = 4
      else {
        i += 1
        continue
      }
      if (i + len > all.length) break
      let cp = 0
      if (len === 1) cp = b
      else if (len === 2) cp = ((b & 0x1f) << 6) | (all[i + 1] & 0x3f)
      else if (len === 3) cp = ((b & 0x0f) << 12) | ((all[i + 1] & 0x3f) << 6) | (all[i + 2] & 0x3f)
      else cp = ((b & 0x07) << 18) | ((all[i + 1] & 0x3f) << 12) | ((all[i + 2] & 0x3f) << 6) | (all[i + 3] & 0x3f)
      out += String.fromCodePoint(cp)
      i += len
    }
    pending = Array.prototype.slice.call(all.subarray(i))
    return out
  }
}

/**
 * 发起流式提问
 *
 * @param {Object} options
 * @param {Number} options.sessionId 会话ID，可空
 * @param {String} options.question 问题
 * @param {Function} options.onMeta  收到 sessionId（检索前）
 * @param {Function} options.onSources 收到引用来源（检索后）
 * @param {Function} options.onDelta 收到增量文本
 * @param {Function} options.onDone  结束
 * @param {Function} options.onError 失败
 * @returns {{abort: Function}} 可中断
 */
export function streamChat(options) {
  const { sessionId, question, onMeta, onSources, onDelta, onDone, onError } = options
  let finished = false
  let buffer = ''
  const decode = createUtf8Decoder()

  const finish = (fn, payload) => {
    if (finished) return
    finished = true
    if (fn) fn(payload)
  }

  const dispatch = frame => {
    let event = 'message'
    let data = ''
    frame.split('\n').forEach(line => {
      const text = line.replace(/\r$/, '')
      if (text.indexOf('event:') === 0) event = text.slice(6).trim()
      else if (text.indexOf('data:') === 0) data += text.slice(5).trim()
    })
    if (!data) return
    let payload
    try {
      payload = JSON.parse(data)
    } catch (e) {
      return
    }
    if (event === 'meta') {
      if (onMeta) onMeta(payload)
    } else if (event === 'sources') {
      if (onSources) onSources(payload)
    } else if (event === 'delta') {
      if (onDelta) onDelta(payload.text || '')
    } else if (event === 'done') {
      finish(onDone, payload)
    } else if (event === 'error') {
      finish(onError, payload.message || 'AI 服务异常')
    }
  }

  const feed = text => {
    buffer += text
    let index = buffer.indexOf('\n\n')
    while (index >= 0) {
      const frame = buffer.slice(0, index)
      buffer = buffer.slice(index + 2)
      dispatch(frame)
      index = buffer.indexOf('\n\n')
    }
  }

  const requestOptions = {
    url: getBaseUrl() + '/business/ai/chat/stream',
    method: 'POST',
    header: {
      Authorization: 'Bearer ' + getToken(),
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      'X-Request-Id': buildRequestId()
    },
    data: { sessionId, question },
    enableChunked: true,
    responseType: 'arraybuffer',
    timeout: 120000,
    success: res => {
      // 正常结束：已收到 done 就不处理；否则把整包当一次分片兜底解析
      if (finished) return
      if (res && res.data) {
        try {
          feed(decode(res.data))
        } catch (e) {
          // ignore
        }
      }
      finish(onError, '连接提前结束')
    },
    fail: err => {
      finish(onError, (err && err.errMsg) || '网络异常')
    }
  }

  // 直接调用 wx.request：uni.request 在部分版本会丢掉 enableChunked
  let task = null
  // #ifdef MP-WEIXIN
  task = wx.request(requestOptions)
  // #endif
  // #ifndef MP-WEIXIN
  task = uni.request(requestOptions)
  // #endif

  if (task && typeof task.onChunkReceived === 'function') {
    task.onChunkReceived(res => {
      try {
        feed(decode(res.data))
      } catch (e) {
        // ignore
      }
    })
  } else {
    finish(onError, '当前环境不支持流式输出')
  }

  return {
    abort() {
      try {
        task && task.abort && task.abort()
      } catch (e) {
        // ignore
      }
    }
  }
}

export default { streamChat }
