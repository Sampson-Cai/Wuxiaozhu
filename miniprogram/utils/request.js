import store from '@/store'
import config from '@/config'
import { getToken } from '@/utils/auth'
import { getBaseUrl } from '@/utils/server'
import errorCode from '@/utils/errorCode'
import { toast, showConfirm, tansParams } from '@/utils/common'

let timeout = 15000

// 幂等键：后端写接口要求 X-Request-Id，重复提交返回 409
const buildRequestId = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

// 把微信/系统的原始错误翻译成能直接定位问题的提示
const describeError = error => {
  const raw = (error && (error.errMsg || error.message)) || ''
  if (raw.indexOf('url not in domain list') > -1) return '域名未加入白名单：微信后台 → 开发设置 → 服务器域名'
  if (raw.indexOf('timeout') > -1) return '请求超时，请检查手机网络'
  if (/ssl|SSL|certificate/i.test(raw)) return 'HTTPS 证书校验失败'
  if (/ERR_CONNECTION|ERR_NAME|ERR_ADDRESS/i.test(raw)) return '无法连接服务器，请检查域名解析'
  return raw ? '请求失败：' + raw.slice(0, 60) : '后端接口连接异常'
}

const request = config => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  config.header = config.header || {}
  if (getToken() && !isToken) {
    config.header['Authorization'] = 'Bearer ' + getToken()
  }
  const method = (config.method || 'get').toLowerCase()
  if (['post', 'put', 'delete'].indexOf(method) > -1) {
    config.header['X-Request-Id'] = buildRequestId()
  }
  // get请求映射params参数
  if (config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.url = url
  }
  return new Promise((resolve, reject) => {
    const host = getBaseUrl() || config.baseUrl
    uni.request({
        method: config.method || 'get',
        timeout: config.timeout ||  timeout,
        url: host + config.url,
        data: config.data,
        header: config.header,
        dataType: 'json'
      }).then(response => {
        let [error, res] = response
        if (error) {
          console.error('[request]', host + config.url, error)
          if (!config.silent) toast(describeError(error))
          reject(describeError(error))
          return
        }
        const code = res.data.code || 200
        const msg = errorCode[code] || res.data.msg || errorCode['default']
        if (code === 401) {
          showConfirm('登录状态已过期，您可以继续留在该页面，或者重新登录?').then(res => {
            if (res.confirm) {
              store.dispatch('LogOut').then(res => {
                uni.reLaunch({ url: '/pages/login' })
              })
            }
          })
          reject('无效的会话，或者会话已过期，请重新登录。')
        } else if (code === 500) {
          if (!config.silent) toast(msg)
          reject('500')
        } else if (code !== 200) {
          if (!config.silent) toast(msg)
          reject(code)
        }
        resolve(res.data)
      })
      .catch(error => {
        let { message } = error
        if (message === 'Network Error') {
          message = '连接不到服务 ' + host.replace(/^https?:\/\//, '')
        } else if (message.includes('timeout')) {
          message = '系统接口请求超时'
        } else if (message.includes('Request failed with status code')) {
          message = '系统接口' + message.slice(-3) + '异常'
        }
        if (!config.silent) toast(message)
        reject(error)
      })
  })
}

export default request
