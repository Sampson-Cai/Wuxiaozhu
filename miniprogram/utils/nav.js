/**
 * 自定义导航栏（navigationStyle: custom）顶部留白计算
 *
 * 微信小程序右上角有原生胶囊按钮，固定悬浮在状态栏下方，
 * 页面内容若只留 statusBarHeight，会被胶囊压住。
 * 这里按胶囊的实际位置算：内容从胶囊底部再往下 8px 开始。
 */

const FALLBACK_NAV_HEIGHT = 44 // iOS 导航栏标准高度
const GAP = 8 // 胶囊与内容之间的呼吸间距

export function getStatusBarHeight() {
  try {
    const sys = uni.getSystemInfoSync()
    return sys.statusBarHeight || 20
  } catch (e) {
    return 20
  }
}

/**
 * 胶囊按钮位置，非小程序端或取不到时返回 null
 */
export function getMenuButtonRect() {
  try {
    if (typeof uni.getMenuButtonBoundingClientRect !== 'function') {
      return null
    }
    const rect = uni.getMenuButtonBoundingClientRect()
    if (rect && rect.height > 0 && rect.bottom > 0) {
      return rect
    }
  } catch (e) {
    // ignore
  }
  return null
}

/**
 * 内容区起始位置（单位 px，从屏幕顶部算）
 * 用它替换原来的 statusBarHeight 占位，即可整体下移避开胶囊
 */
export function getNavTopOffset() {
  const statusBar = getStatusBarHeight()
  const capsule = getMenuButtonRect()
  if (capsule) {
    return capsule.bottom + GAP
  }
  return statusBar + FALLBACK_NAV_HEIGHT + GAP
}

/**
 * 导航栏高度（不含状态栏），用于自定义标题栏垂直居中
 */
export function getNavBarHeight() {
  const statusBar = getStatusBarHeight()
  const capsule = getMenuButtonRect()
  if (capsule) {
    return (capsule.top - statusBar) * 2 + capsule.height
  }
  return FALLBACK_NAV_HEIGHT
}

export default {
  getStatusBarHeight,
  getMenuButtonRect,
  getNavTopOffset,
  getNavBarHeight
}
