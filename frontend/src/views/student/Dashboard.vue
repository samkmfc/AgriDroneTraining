<template>
  <div class="page-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>{{ greeting }}，{{ realName }} 👋</h2>
        <p>欢迎使用农业无人机培训管理系统，安全飞行，规范作业，从飞前检查开始。</p>
        <div class="welcome-actions">
          <el-button type="primary" :icon="CircleCheck" @click="$router.push('/student/checklist')">飞前安全检查</el-button>
          <el-button :icon="MagicStick" @click="$router.push('/student/ai-assistant')">AI农情诊断</el-button>
        </div>
      </div>
      <div class="welcome-illust"><el-icon :size="120"><Promotion /></el-icon></div>
    </div>

    <!-- 执照状态提醒 -->
    <el-alert
      v-if="licenseAlert"
      :title="licenseAlert.title"
      :type="licenseAlert.type"
      :description="licenseAlert.desc"
      show-icon
      :closable="false"
      style="margin: 18px 0; border-radius: 12px"
    />

    <!-- 统计卡片 -->
    <el-row :gutter="18" style="margin-top: 18px">
      <el-col v-for="c in cards" :key="c.label" :xs="12" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" :class="c.bg"><el-icon><component :is="c.icon" /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ c.value }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="18" style="margin-top: 18px">
      <el-col :md="14">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>学习进度</div>
          <div class="progress-block">
            <el-progress type="dashboard" :percentage="courseProgress" :color="progressColor" :width="150" />
            <div class="progress-meta">
              <p>已完成课程 <b>{{ dash.finishedCourseCount ?? 0 }}</b> / {{ dash.courseCount ?? 0 }} 门</p>
              <p>考核通过 <b>{{ dash.passedExamCount ?? 0 }}</b> / {{ dash.examCount ?? 0 }} 次</p>
              <el-button type="primary" plain size="small" @click="$router.push('/student/courses')">继续学习</el-button>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :md="10">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>快捷入口</div>
          <div class="quick-grid">
            <div v-for="q in quicks" :key="q.path" class="quick-item" @click="$router.push(q.path)">
              <div class="quick-icon" :class="q.bg"><el-icon><component :is="q.icon" /></el-icon></div>
              <span>{{ q.label }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 待办与通知 -->
    <el-row :gutter="18" style="margin-top: 18px">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title"><span class="bar"></span>待办与通知</div>
          <div v-if="notifyList.length" class="todo-list">
            <div
              v-for="n in notifyList"
              :key="n.id"
              class="todo-item"
              :class="{ unread: !n.isRead }"
              @click="onNotifyClick(n)"
            >
              <div class="todo-ic" :style="{ background: typeMeta(n.type).color }">
                <el-icon><component :is="typeMeta(n.type).icon" /></el-icon>
              </div>
              <div class="todo-body">
                <div class="todo-row">
                  <span class="todo-title">{{ n.title }}</span>
                  <span class="todo-time">{{ relativeTime(n.createTime) }}</span>
                </div>
                <div class="todo-content">{{ n.content }}</div>
              </div>
              <span v-if="!n.isRead" class="todo-badge">未读</span>
            </div>
          </div>
          <el-empty v-else description="暂无待办与通知" :image-size="90" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { CircleCheck, MagicStick } from '@element-plus/icons-vue'
import { statApi, notificationApi } from '@/api'

const store = useStore()
const router = useRouter()
const realName = computed(() => store.getters.realName)
const dash = ref({})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const courseProgress = computed(() => {
  const total = dash.value.courseCount || 0
  const done = dash.value.finishedCourseCount || 0
  return total ? Math.round((done / total) * 100) : 0
})
const progressColor = [
  { color: '#f0a020', percentage: 40 },
  { color: '#1fb574', percentage: 100 }
]

const licenseAlert = computed(() => {
  const s = dash.value.licenseStatus
  if (s === 'EXPIRED') return { type: 'error', title: '执照已过期', desc: '您的无人机执照已过期，请勿违规作业，请尽快联系培训机构办理续期。' }
  if (s === 'EXPIRING') return { type: 'warning', title: '执照即将到期', desc: `您的执照将在 ${dash.value.nearestExpiryDays ?? ''} 天后到期，请及时续期以免影响作业。` }
  if (s === 'NONE') return { type: 'info', title: '暂无执照记录', desc: '您还没有登记无人机执照，请联系培训机构添加。' }
  return null
})

const cards = computed(() => [
  { label: '持有执照', value: dash.value.licenseCount ?? 0, icon: 'Postcard', bg: 'bg-green' },
  { label: '作业日志', value: dash.value.flightLogCount ?? 0, icon: 'Position', bg: 'bg-blue' },
  { label: '待审核', value: dash.value.pendingLogCount ?? 0, icon: 'Stamp', bg: 'bg-orange' },
  { label: '已通过审核', value: dash.value.approvedLogCount ?? 0, icon: 'CircleCheck', bg: 'bg-purple' }
])

const quicks = [
  { label: '在线课程', path: '/student/courses', icon: 'Reading', bg: 'bg-green' },
  { label: '场景测试', path: '/student/exams', icon: 'EditPen', bg: 'bg-blue' },
  { label: '禁飞区地图', path: '/student/no-fly-zone', icon: 'Location', bg: 'bg-red' },
  { label: '植保知识库', path: '/student/knowledge', icon: 'Sunny', bg: 'bg-orange' },
  { label: '飞行日志', path: '/student/flight-log', icon: 'Notebook', bg: 'bg-purple' },
  { label: 'AI助手', path: '/student/ai-assistant', icon: 'MagicStick', bg: 'bg-green' }
]

onMounted(async () => {
  const res = await statApi.studentDashboard()
  dash.value = res.data || {}
})

/* ============ 待办与通知 ============ */
const notifyList = ref([])

const NOTIFY_META = {
  FLIGHT_APPROVED: { icon: 'CircleCheck', color: '#15a06b' },
  FLIGHT_REJECTED: { icon: 'CircleClose', color: '#e54d42' },
  LICENSE_EXPIRED: { icon: 'WarningFilled', color: '#e54d42' },
  LICENSE_EXPIRING: { icon: 'AlarmClock', color: '#f0a020' },
  SYSTEM: { icon: 'BellFilled', color: '#3a7afe' }
}
function typeMeta(type) {
  return NOTIFY_META[type] || NOTIFY_META.SYSTEM
}

function relativeTime(time) {
  if (!time) return ''
  const d = new Date(String(time).replace(/-/g, '/'))
  const diff = Date.now() - d.getTime()
  if (isNaN(diff)) return time
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour} 小时前`
  const day = Math.floor(hour / 24)
  if (day < 7) return `${day} 天前`
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${m}-${dd}`
}

async function loadNotify() {
  try {
    const res = await notificationApi.my({ pageNum: 1, pageSize: 6 })
    notifyList.value = res.data?.records ?? []
  } catch (e) {
    notifyList.value = []
  }
}

async function onNotifyClick(n) {
  if (!n.isRead) {
    try {
      await notificationApi.read(n.id)
      n.isRead = true
    } catch (e) { /* 忽略 */ }
  }
  if (n.type && n.type.startsWith('FLIGHT_')) {
    router.push('/student/flight-log')
  } else if (n.type && n.type.startsWith('LICENSE_')) {
    router.push('/student/license')
  }
}

onMounted(loadNotify)
</script>

<style scoped lang="scss">
.welcome-banner {
  position: relative;
  background: linear-gradient(120deg, #0f3d2e 0%, #14724f 60%, #1fb574 130%);
  border-radius: 16px;
  padding: 32px 36px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  box-shadow: 0 8px 28px rgba(15, 61, 46, 0.25);
  h2 { font-size: 26px; margin: 0 0 10px; }
  p { color: #cdeede; margin: 0 0 18px; font-size: 14px; }
}
.welcome-illust { opacity: .18; }
.progress-block { display: flex; align-items: center; gap: 36px; padding: 16px 8px; }
.progress-meta p { color: #5a6e64; margin: 6px 0; b { color: var(--brand-primary); font-size: 18px; } }

.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; padding: 8px 0; }
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 16px 8px; border-radius: 12px; cursor: pointer; transition: all .2s;
  &:hover { background: #f0f6f3; transform: translateY(-2px); }
  span { font-size: 13px; color: #4a5e54; }
}
.quick-icon { width: 46px; height: 46px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 22px; }

.todo-list { display: flex; flex-direction: column; padding: 6px 0; }
.todo-item {
  position: relative;
  display: flex; align-items: flex-start; gap: 12px;
  padding: 14px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background .2s, transform .2s;
  &:hover { background: #f0f6f3; transform: translateX(2px); }
  &.unread { background: #f3faf6; }
  & + .todo-item { margin-top: 2px; }
}
.todo-ic {
  flex-shrink: 0;
  width: 40px; height: 40px; border-radius: 11px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px;
}
.todo-body { flex: 1; min-width: 0; }
.todo-row { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.todo-title {
  font-size: 14px; font-weight: 600; color: #2c3e36;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.todo-time { flex-shrink: 0; font-size: 12px; color: #a4b3ab; }
.todo-content {
  margin-top: 4px;
  font-size: 13px; color: #6a7c72; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.todo-badge {
  flex-shrink: 0;
  align-self: center;
  font-size: 11px; color: var(--brand-primary);
  background: rgba(21, 160, 107, 0.12);
  padding: 2px 8px; border-radius: 10px;
}
</style>
