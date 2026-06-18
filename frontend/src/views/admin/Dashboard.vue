<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>数据看板</div>
        <div class="page-subtitle">农业无人机培训管理 · 运营数据总览</div>
      </div>
      <el-button :icon="Refresh" circle @click="loadAll" />
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="18">
      <el-col v-for="c in cards" :key="c.label" :xs="12" :sm="8" :md="6" :lg="6">
        <div class="stat-card">
          <div class="stat-icon" :class="c.bg"><el-icon><component :is="c.icon" /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ c.value }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="18" style="margin-top: 18px">
      <el-col :lg="16" :md="24">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>近14天作业日志提交趋势</div>
          <chart-box :option="trendOption" height="300px" />
        </div>
      </el-col>
      <el-col :lg="8" :md="24">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>执照状态分布</div>
          <chart-box :option="licenseOption" height="300px" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="18" style="margin-top: 18px">
      <el-col :lg="8" :md="24">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>近14天平台访问量</div>
          <chart-box :option="visitOption" height="300px" />
        </div>
      </el-col>
      <el-col :lg="8" :md="24">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>各试卷考核通过率</div>
          <chart-box :option="examOption" height="300px" />
        </div>
      </el-col>
      <el-col :lg="8" :md="24">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>各机型培训人数</div>
          <chart-box :option="modelOption" height="300px" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import ChartBox from '@/components/ChartBox.vue'
import { statApi } from '@/api'

const overview = ref({})
const trend = ref([])
const visit = ref([])
const licenseStatus = ref([])
const examPass = ref([])
const modelDist = ref([])

const cards = computed(() => [
  { label: '学员总数', value: overview.value.studentCount ?? 0, icon: 'User', bg: 'bg-green' },
  { label: '课程数量', value: overview.value.courseCount ?? 0, icon: 'Reading', bg: 'bg-blue' },
  { label: '作业日志', value: overview.value.flightLogCount ?? 0, icon: 'Position', bg: 'bg-purple' },
  { label: '待审日志', value: overview.value.pendingAuditCount ?? 0, icon: 'Stamp', bg: 'bg-orange' },
  { label: '执照预警', value: overview.value.expiringLicenseCount ?? 0, icon: 'Warning', bg: 'bg-red' },
  { label: '考核通过率', value: (overview.value.examPassRate ?? 0) + '%', icon: 'TrophyBase', bg: 'bg-green' },
  { label: '平台访问量', value: overview.value.totalVisits ?? 0, icon: 'View', bg: 'bg-blue' },
  { label: 'AI诊断次数', value: overview.value.aiDiagnosisCount ?? 0, icon: 'MagicStick', bg: 'bg-purple' }
])

const lineBase = (data, color) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 36, right: 18, top: 24, bottom: 30 },
  xAxis: { type: 'category', data: data.map((d) => d.date?.slice(5) ?? d.date), axisLine: { lineStyle: { color: '#cfe0d7' } }, axisLabel: { color: '#8a9b92' } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#eef3f0' } }, axisLabel: { color: '#8a9b92' } },
  series: [{
    type: 'line', smooth: true, data: data.map((d) => d.count),
    symbol: 'circle', symbolSize: 7,
    lineStyle: { width: 3, color },
    itemStyle: { color },
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: color + '55' }, { offset: 1, color: color + '05' }] } }
  }]
})

const trendOption = computed(() => lineBase(trend.value, '#15a06b'))
const visitOption = computed(() => lineBase(visit.value, '#2f6bd6'))

const licenseOption = computed(() => {
  const map = { NORMAL: '正常', EXPIRING: '临期', EXPIRED: '已过期' }
  const colors = { NORMAL: '#15a06b', EXPIRING: '#f0a020', EXPIRED: '#e8504f' }
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#8a9b92' } },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '44%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}' },
      data: licenseStatus.value.map((d) => ({ name: map[d.status] || d.status, value: d.count, itemStyle: { color: colors[d.status] } }))
    }]
  }
})

const examOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (p) => `${p[0].name}<br/>通过率: ${p[0].value}%` },
  grid: { left: 40, right: 18, top: 24, bottom: 50 },
  xAxis: { type: 'category', data: examPass.value.map((d) => d.examTitle), axisLabel: { color: '#8a9b92', interval: 0, width: 70, overflow: 'truncate' } },
  yAxis: { type: 'value', max: 100, splitLine: { lineStyle: { color: '#eef3f0' } }, axisLabel: { color: '#8a9b92', formatter: '{value}%' } },
  series: [{
    type: 'bar', barWidth: '42%',
    data: examPass.value.map((d) => d.passRate),
    itemStyle: { borderRadius: [6, 6, 0, 0], color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#1fb574' }, { offset: 1, color: '#0e8a5a' }] } }
  }]
}))

const modelOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 80, right: 24, top: 16, bottom: 24 },
  xAxis: { type: 'value', splitLine: { lineStyle: { color: '#eef3f0' } }, axisLabel: { color: '#8a9b92' } },
  yAxis: { type: 'category', data: modelDist.value.map((d) => d.model), axisLabel: { color: '#8a9b92' } },
  series: [{
    type: 'bar', barWidth: '50%',
    data: modelDist.value.map((d) => d.count),
    itemStyle: { borderRadius: [0, 6, 6, 0], color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [{ offset: 0, color: '#4f9bff' }, { offset: 1, color: '#2f6bd6' }] } }
  }]
}))

async function loadAll() {
  const [o, t, v, l, e, m] = await Promise.all([
    statApi.overview(), statApi.flightLogTrend(), statApi.visitTrend(),
    statApi.licenseStatus(), statApi.examPass(), statApi.modelDistribution()
  ])
  overview.value = o.data || {}
  trend.value = t.data || []
  visit.value = v.data || []
  licenseStatus.value = l.data || []
  examPass.value = e.data || []
  modelDist.value = m.data || []
}

onMounted(loadAll)
</script>
