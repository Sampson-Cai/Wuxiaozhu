// 应用全局配置
//
// 后端地址：
//   生产：https://demo7.ztcoding.com（nginx 443 → 本机 18080）
//   本地后端：改 DEV_HOST 为 http://127.0.0.1:18080
//   小程序后台需把 https://demo7.ztcoding.com 加入 request 与 downloadFile 合法域名
//
// 地址变了不用重新编译：「我的 → 应用设置 → 服务器地址」可直接改，
// 改完立即生效（存本地缓存，优先于这里的默认值）。

const DEV_HOST = 'https://demo7.ztcoding.com' // 开发者工具（生产域名）
const LAN_HOST = 'https://demo7.ztcoding.com' // 真机 / 体验版：生产域名
const PROD_HOST = 'https://demo7.ztcoding.com' // 正式环境（同域名）

function resolveBaseUrl() {
  try {
    const platform = (uni.getSystemInfoSync().platform || '').toLowerCase()
    // devtools / mac / windows 都是电脑端运行，可以直连本机
    if (platform === 'devtools' || platform === 'mac' || platform === 'windows') {
      return DEV_HOST
    }
    // ios / android：真机，走局域网 IP
    return LAN_HOST
  } catch (e) {
    return DEV_HOST
  }
}

module.exports = {
  baseUrl: resolveBaseUrl(),
  devBaseUrl: DEV_HOST,
  lanBaseUrl: LAN_HOST,
  prodBaseUrl: PROD_HOST,
  // 应用信息
  appInfo: {
    // 应用名称
    name: '物小助',
    // 应用版本
    version: '1.0.0',
    // 应用logo
    logo: '/static/images/logo.png',
    // 官方网站
    site_url: '',
    // 政策协议
    agreements: [
      {
        title: '隐私政策',
        url: 'https://ruoyi.vip/protocol.html'
      },
      {
        title: '用户服务协议',
        url: 'https://ruoyi.vip/protocol.html'
      }
    ]
  }
}
