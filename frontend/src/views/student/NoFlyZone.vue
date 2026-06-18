<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>禁飞区热力地图</div>
        <div class="page-subtitle">作业前请务必核查空域，远离禁飞与限飞区域，安全合规飞行</div>
      </div>
    </div>

    <el-alert
      type="warning"
      show-icon
      :closable="false"
      title="空域安全提示"
      description="地图以热力图形式标注禁飞/限飞风险强度，颜色越红风险越高(机场净空/重点目标)。红色圆形为禁飞区精确净空范围，橙色为限飞区。作业前请结合本地民航/公安管制信息核查空域。"
      style="margin-bottom: 18px; border-radius: 12px"
    />

    <div class="toolbar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索区域名称 / 省 / 市"
        clearable
        style="width: 220px"
        :prefix-icon="Search"
        @keyup.enter="reload"
        @clear="reload"
      />
      <el-select v-model="filters.level" placeholder="全部级别" clearable style="width: 140px" @change="reload">
        <el-option label="禁飞区 (STRICT)" value="STRICT" />
        <el-option label="限飞区 (LIMIT)" value="LIMIT" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
      <div class="spacer"></div>
      <el-switch v-model="showHeat" active-text="热力图" inline-prompt @change="applyLayers" style="margin-right: 14px" />
      <el-switch v-model="showCircle" active-text="净空范围" inline-prompt @change="applyLayers" />
    </div>

    <!-- 高德热力地图 -->
    <div class="map-card" v-loading="loading">
      <div ref="mapEl" class="amap-container"></div>
      <!-- 热力图例 -->
      <div class="heat-legend" v-if="showHeat">
        <span class="lg-title">风险强度</span>
        <div class="lg-bar"></div>
        <div class="lg-scale"><span>低</span><span>高</span></div>
        <div class="lg-zones">
          <span><i class="dot strict"></i>禁飞 {{ strictCount }}</span>
          <span><i class="dot limit"></i>限飞 {{ limitCount }}</span>
        </div>
      </div>
      <div v-if="mapError" class="map-error">
        <el-icon :size="30"><Warning /></el-icon>
        <p>{{ mapError }}</p>
      </div>
    </div>

    <!-- 列表 -->
    <div class="table-card" style="margin-top: 18px" v-loading="loading">
      <el-table :data="list" stripe @row-click="focusZone">
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="name" label="区域名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="所在地" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ [row.province, row.city].filter(Boolean).join(' / ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="级别" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level === 'STRICT' ? 'danger' : 'warning'" effect="dark" size="small">
              {{ row.level === 'STRICT' ? '禁飞' : '限飞' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="半径(米)" width="110" align="center">
          <template #default="{ row }">{{ row.radius ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description || '—' }}</template>
        </el-table-column>
        <template #empty><el-empty description="暂无禁飞区数据" /></template>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Search, Warning } from '@element-plus/icons-vue'
import { noFlyZoneApi } from '@/api'
import { loadAMap, ZONE_COLOR } from '@/utils/amap'
import { DEFAULT_CENTER, DEFAULT_ZOOM } from '@/config/map'

const loading = ref(false)
const mapError = ref('')
const list = ref([])
const filters = reactive({ keyword: '', province: '', city: '', level: '' })
const mapEl = ref()
const showHeat = ref(true)
const showCircle = ref(true)

let AMap = null
let map = null
let heatmap = null
let overlays = []
let infoWindow = null

const strictCount = computed(() => list.value.filter((z) => z.level === 'STRICT').length)
const limitCount = computed(() => list.value.filter((z) => z.level === 'LIMIT').length)

async function initMap() {
  try {
    AMap = await loadAMap()
    map = new AMap.Map(mapEl.value, {
      zoom: DEFAULT_ZOOM,
      center: DEFAULT_CENTER,
      mapStyle: 'amap://styles/blue',
      viewMode: '2D'
    })
    map.addControl(new AMap.Scale())
    map.addControl(new AMap.ToolBar({ position: { top: '12px', right: '12px' } }))
    infoWindow = new AMap.InfoWindow({ offset: new AMap.Pixel(0, -10), anchor: 'bottom-center' })
    // 热力图层
    heatmap = new AMap.HeatMap(map, {
      radius: 70,
      opacity: [0, 0.9],
      gradient: {
        0.2: '#2fce8f',
        0.4: '#a8e05f',
        0.6: '#f5d442',
        0.8: '#f0913a',
        1.0: '#e8504f'
      }
    })
  } catch (e) {
    mapError.value = '高德地图加载失败，请检查网络或 API Key 配置：' + (e?.message || e)
  }
}

// 将一个禁飞区在其净空半径内离散为多个加权热力点，使热力覆盖整片区域而非单点
function zoneHeatPoints(z) {
  const lng = Number(z.longitude)
  const lat = Number(z.latitude)
  const r = Number(z.radius) || 1000
  if (!lng || !lat) return []
  const base = z.level === 'STRICT' ? 100 : 55
  const pts = [{ lng, lat, count: base }]
  const rings = 3
  const perRing = 10
  const mPerLat = 111000
  const mPerLng = 111000 * Math.cos((lat * Math.PI) / 180)
  for (let i = 1; i <= rings; i++) {
    const rr = (r * i) / rings
    for (let j = 0; j < perRing; j++) {
      const ang = (Math.PI * 2 * j) / perRing + i * 0.4
      const dLat = (rr * Math.cos(ang)) / mPerLat
      const dLng = (rr * Math.sin(ang)) / mPerLng
      pts.push({ lng: lng + dLng, lat: lat + dLat, count: Math.round(base * (1 - i / (rings + 1))) })
    }
  }
  return pts
}

function clearOverlays() {
  if (map && overlays.length) map.remove(overlays)
  overlays = []
}

function renderZones() {
  if (!map || !AMap) return
  clearOverlays()

  // 1) 热力图数据
  const heatData = []
  list.value.forEach((z) => heatData.push(...zoneHeatPoints(z)))
  if (heatmap) heatmap.setDataSet({ data: heatData, max: 100 })

  // 2) 净空圆形边界 + 中心标记
  const fitTargets = []
  list.value.forEach((z) => {
    const lng = Number(z.longitude)
    const lat = Number(z.latitude)
    if (!lng || !lat) return
    const color = ZONE_COLOR[z.level] || '#e8504f'
    const center = [lng, lat]

    const circle = new AMap.Circle({
      center,
      radius: Number(z.radius) || 500,
      strokeColor: color,
      strokeWeight: 2,
      strokeOpacity: 0.9,
      strokeStyle: 'dashed',
      fillColor: color,
      fillOpacity: 0.08,
      cursor: 'pointer',
      zIndex: 50
    })
    const marker = new AMap.Marker({
      position: center,
      anchor: 'center',
      content: `<div class="zone-marker" style="background:${color}"></div>`,
      zIndex: 60
    })
    const lv = z.level === 'STRICT' ? '禁飞区' : '限飞区'
    const html = `
      <div style="padding:10px 14px;min-width:200px;">
        <div style="font-weight:700;color:#1d2b24;font-size:14px;margin-bottom:6px;">${z.name}</div>
        <div style="font-size:12.5px;color:#5a6e64;line-height:1.8;">
          级别：<b style="color:${color}">${lv}</b><br/>
          所在地：${[z.province, z.city].filter(Boolean).join(' ') || '—'}<br/>
          净空半径：${z.radius ?? '—'} 米<br/>
          说明：${z.description || '—'}
        </div>
      </div>`
    const open = () => { infoWindow.setContent(html); infoWindow.open(map, center) }
    circle.on('click', open)
    marker.on('click', open)
    overlays.push(circle, marker)
    fitTargets.push(marker)
  })

  applyLayers()
  if (fitTargets.length) map.setFitView(fitTargets, false, [60, 60, 60, 60])
}

// 根据开关显示/隐藏 热力 / 圆形
function applyLayers() {
  if (heatmap) showHeat.value ? heatmap.show() : heatmap.hide()
  overlays.forEach((o) => {
    if (showCircle.value) o.show()
    else o.hide()
  })
}

function focusZone(row) {
  if (!map || !row.longitude || !row.latitude) return
  map.setZoomAndCenter(12, [Number(row.longitude), Number(row.latitude)])
}

async function fetchData() {
  loading.value = true
  try {
    const res = await noFlyZoneApi.list({ ...filters })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function reload() {
  await fetchData()
  renderZones()
}

onMounted(async () => {
  await initMap()
  await reload()
})

onBeforeUnmount(() => {
  clearOverlays()
  map && map.destroy()
  map = null
})
</script>

<style scoped lang="scss">
.map-card {
  position: relative;
  background: #fff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  padding: 8px;
  overflow: hidden;
}
.amap-container { width: 100%; height: 540px; border-radius: 10px; overflow: hidden; }
.map-error {
  position: absolute; inset: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 10px; color: #e8504f; background: #fff5f5; border-radius: 10px; text-align: center; padding: 20px;
}
.heat-legend {
  position: absolute; left: 18px; top: 18px; z-index: 20;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 10px; padding: 10px 14px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  font-size: 12px; color: #4a5e54; min-width: 150px;
  .lg-title { font-weight: 700; color: #1d2b24; }
  .lg-bar {
    height: 10px; border-radius: 5px; margin: 8px 0 2px;
    background: linear-gradient(90deg, #2fce8f, #a8e05f, #f5d442, #f0913a, #e8504f);
  }
  .lg-scale { display: flex; justify-content: space-between; color: #8a9b92; }
  .lg-zones { display: flex; gap: 14px; margin-top: 8px; padding-top: 8px; border-top: 1px dashed #e0e8e4;
    span { display: flex; align-items: center; gap: 5px; } }
  .dot { width: 9px; height: 9px; border-radius: 50%; display: inline-block; }
  .dot.strict { background: #e8504f; }
  .dot.limit { background: #f0a020; }
}
:deep(.zone-marker) {
  width: 12px; height: 12px; border-radius: 50%;
  border: 2px solid #fff; box-shadow: 0 0 0 2px rgba(0,0,0,.18);
}
:deep(.el-table__row) { cursor: pointer; }
</style>
