import config from '@/config'

/**
 * 后端服务地址管理
 *
 * 优先级：本地缓存（用户在「应用设置」里手填的） > 按运行环境推断的默认值。
 * 真机调试时手机访问不到 127.0.0.1，必须用电脑的局域网 IP，
 * 而 IP 会随 Wi-Fi 变化，所以做成可运行时修改，不必重新编译。
 */

const STORAGE_KEY = 'cx_server_url'

function normalize(url) {
  if (!url) return ''
  let value = String(url).trim()
  if (!/^https?:\/\//i.test(value)) {
    value = 'http://' + value
  }
  return value.replace(/\/+$/, '')
}

export function getBaseUrl() {
  try {
    const saved = uni.getStorageSync(STORAGE_KEY)
    if (saved) return normalize(saved)
  } catch (e) {
    // ignore
  }
  return normalize(config.baseUrl)
}

export function getSavedBaseUrl() {
  try {
    return normalize(uni.getStorageSync(STORAGE_KEY))
  } catch (e) {
    return ''
  }
}

export function setBaseUrl(url) {
  const value = normalize(url)
  if (!value) return false
  try {
    uni.setStorageSync(STORAGE_KEY, value)
    return true
  } catch (e) {
    return false
  }
}

export function resetBaseUrl() {
  try {
    uni.removeStorageSync(STORAGE_KEY)
  } catch (e) {
    // ignore
  }
}

/**
 * 探测某个地址是否可用（不依赖登录态）
 */
export function pingBaseUrl(url) {
  const target = normalize(url)
  if (!target) return Promise.reject(new Error('地址为空'))
  return new Promise((resolve, reject) => {
    uni.request({
      url: target + '/captchaImage',
      method: 'GET',
      timeout: 6000,
      success: res => {
        if (res.statusCode === 200 && res.data && res.data.code === 200) {
          resolve(target)
        } else {
          reject(new Error('返回异常：' + res.statusCode))
        }
      },
      fail: () => reject(new Error('连接失败'))
    })
  })
}

export default {
  getBaseUrl,
  getSavedBaseUrl,
  setBaseUrl,
  resetBaseUrl,
  pingBaseUrl
}
