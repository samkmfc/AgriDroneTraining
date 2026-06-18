import AMapLoader from '@amap/amap-jsapi-loader'
import { AMAP_KEY, AMAP_SECURITY } from '@/config/map'

let loaderPromise = null

/**
 * 加载高德地图 JS API 2.0 (全局缓存, 只加载一次)
 * @returns {Promise<AMap>}
 */
export function loadAMap() {
  if (!loaderPromise) {
    // 安全密钥配置, 必须在 load 之前
    window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY }
    loaderPromise = AMapLoader.load({
      key: AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.HawkEye', 'AMap.HeatMap']
    })
  }
  return loaderPromise
}

// 级别 -> 颜色
export const ZONE_COLOR = {
  STRICT: '#e8504f',
  LIMIT: '#f0a020'
}
