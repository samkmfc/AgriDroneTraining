<template>
  <el-container class="layout-root">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '230px'" class="layout-aside">
      <div class="logo" :class="{ mini: collapsed }">
        <div class="logo-mark">
          <el-icon><Promotion /></el-icon>
        </div>
        <transition name="fade">
          <div v-show="!collapsed" class="logo-text">
            <div class="logo-title">农业无人机</div>
            <div class="logo-sub">培训管理系统</div>
          </div>
        </transition>
      </div>

      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          background-color="transparent"
          router
          unique-opened
        >
          <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <template #title>{{ m.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ portalLabel }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag :type="isAdmin ? 'danger' : 'success'" effect="light" round size="small">
            {{ isAdmin ? '管理端' : '学员端' }}
          </el-tag>

          <!-- 消息通知中心 -->
          <el-popover
            placement="bottom-end"
            :width="360"
            trigger="click"
            popper-class="notify-popover"
            @show="onNotifyOpen"
          >
            <template #reference>
              <div class="notify-bell">
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                  <el-icon><Bell /></el-icon>
                </el-badge>
              </div>
            </template>

            <div class="notify-panel">
              <div class="notify-head">
                <span class="notify-head-title">消息通知</span>
                <el-button
                  link
                  type="primary"
                  size="small"
                  :disabled="unreadCount === 0"
                  @click="onReadAll"
                >全部已读</el-button>
              </div>

              <el-scrollbar max-height="360px">
                <div v-if="notifyList.length" class="notify-list">
                  <div
                    v-for="n in notifyList"
                    :key="n.id"
                    class="notify-item"
                    :class="{ unread: !n.isRead }"
                    @click="onNotifyClick(n)"
                  >
                    <span class="notify-dot" v-if="!n.isRead"></span>
                    <div class="notify-ic" :style="{ background: typeMeta(n.type).color }">
                      <el-icon><component :is="typeMeta(n.type).icon" /></el-icon>
                    </div>
                    <div class="notify-body">
                      <div class="notify-row">
                        <span class="notify-title">{{ n.title }}</span>
                        <span class="notify-time">{{ relativeTime(n.createTime) }}</span>
                      </div>
                      <div class="notify-content">{{ n.content }}</div>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无消息通知" :image-size="80" />
              </el-scrollbar>

              <div v-if="!isAdmin" class="notify-foot">
                <el-button link type="primary" size="small" @click="goAll">查看全部</el-button>
              </div>
            </div>
          </el-popover>

          <el-dropdown @command="onCommand">
            <div class="user-chip">
              <el-avatar :size="34" :src="avatar" class="user-avatar">
                {{ realName.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ realName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home"><el-icon><HomeFilled /></el-icon>首页</el-dropdown-item>
                <el-dropdown-item v-if="!isAdmin" command="profile"><el-icon><User /></el-icon>个人档案</el-dropdown-item>
                <el-dropdown-item command="password"><el-icon><Lock /></el-icon>修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主体 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 修改密码 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form :model="pwdForm" label-width="90px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="pwdForm.confirm" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi, notificationApi } from '@/api'

const props = defineProps({
  basePath: { type: String, required: true },
  portalLabel: { type: String, required: true }
})

const route = useRoute()
const router = useRouter()
const store = useStore()

const collapsed = computed({
  get: () => store.state.sidebarCollapsed,
  set: () => store.commit('TOGGLE_SIDEBAR')
})

const isAdmin = computed(() => store.getters.isAdmin)
const avatar = computed(() => store.getters.avatar)
const realName = computed(() => store.getters.realName)

// 从路由表生成菜单
const menus = computed(() => {
  const parent = router.options.routes.find((r) => r.path === props.basePath)
  if (!parent) return []
  return parent.children
    .filter((c) => !c.meta?.hidden)
    .map((c) => ({
      path: `${props.basePath}/${c.path}`,
      title: c.meta.title,
      icon: c.meta.icon
    }))
})

const activeMenu = computed(() => route.meta.activeMenu || route.path)
const currentTitle = computed(() => route.meta.title || '')

const pwdVisible = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirm: '' })

function onCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗?', '提示', { type: 'warning' }).then(() => {
      store.dispatch('logout')
      router.push('/login')
    })
  } else if (cmd === 'home') {
    router.push(`${props.basePath}/dashboard`)
  } else if (cmd === 'profile') {
    router.push('/student/profile')
  } else if (cmd === 'password') {
    pwdForm.value = { oldPassword: '', newPassword: '', confirm: '' }
    pwdVisible.value = true
  }
}

async function submitPwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    return ElMessage.warning('请填写完整')
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirm) {
    return ElMessage.warning('两次输入的新密码不一致')
  }
  await userApi.updatePassword({
    oldPassword: pwdForm.value.oldPassword,
    newPassword: pwdForm.value.newPassword
  })
  ElMessage.success('密码修改成功, 请重新登录')
  pwdVisible.value = false
  store.dispatch('logout')
  router.push('/login')
}

/* ============ 消息通知 ============ */
const unreadCount = ref(0)
const notifyList = ref([])

// 通知类型 → 图标 / 主题色
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

async function loadUnread() {
  try {
    const res = await notificationApi.unreadCount()
    unreadCount.value = res.data?.count ?? 0
  } catch (e) {
    /* 静默失败, 不打扰用户 */
  }
}

async function loadList() {
  try {
    const res = await notificationApi.my({ pageNum: 1, pageSize: 8 })
    notifyList.value = res.data?.records ?? []
  } catch (e) {
    notifyList.value = []
  }
}

function onNotifyOpen() {
  loadList()
  loadUnread()
}

async function onNotifyClick(n) {
  if (!n.isRead) {
    try {
      await notificationApi.read(n.id)
      n.isRead = true
      loadUnread()
    } catch (e) { /* 忽略 */ }
  }
  if (isAdmin.value) return
  if (n.type && n.type.startsWith('FLIGHT_')) {
    router.push('/student/flight-log')
  } else if (n.type && n.type.startsWith('LICENSE_')) {
    router.push('/student/license')
  }
}

async function onReadAll() {
  await notificationApi.readAll()
  notifyList.value.forEach((n) => { n.isRead = true })
  unreadCount.value = 0
  ElMessage.success('已全部标记为已读')
}

function goAll() {
  router.push('/student/flight-log')
}

let notifyTimer = null
onMounted(() => {
  loadUnread()
  notifyTimer = setInterval(loadUnread, 60000)
})
onUnmounted(() => {
  if (notifyTimer) clearInterval(notifyTimer)
})
</script>

<style scoped lang="scss">
.layout-root { height: 100vh; }

.layout-aside {
  background: linear-gradient(180deg, #0f3d2e 0%, #0b3527 100%);
  transition: width .28s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  &.mini { padding: 0 16px; justify-content: center; }
  .logo-mark {
    width: 36px; height: 36px; flex-shrink: 0;
    border-radius: 10px;
    background: linear-gradient(135deg, #1fb574, #0e8a5a);
    display: flex; align-items: center; justify-content: center;
    font-size: 20px;
    box-shadow: 0 4px 12px rgba(31, 181, 116, 0.4);
  }
  .logo-title { font-size: 16px; font-weight: 700; letter-spacing: 1px; }
  .logo-sub { font-size: 11px; color: #8fd9b9; letter-spacing: 3px; }
}

.menu-scroll { flex: 1; }

:deep(.el-menu) { border-right: none; padding: 8px 10px; }
:deep(.el-menu-item) {
  color: #b8d4c7;
  border-radius: 12px;
  margin-bottom: 4px;
  height: 46px;
  transition: background .2s, color .2s, transform .2s cubic-bezier(0.34,1.56,0.64,1);
  &:hover { background: rgba(255, 255, 255, 0.08) !important; color: #fff; }
  &:active { transform: scale(0.96); }
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #1fb574, #0e8a5a) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(31, 181, 116, 0.35);
}

.layout-header {
  height: 64px;
  background: rgba(255, 255, 255, 0.62);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.55);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  box-shadow: 0 4px 20px rgba(16, 78, 54, 0.05);
  z-index: 10;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn {
  font-size: 20px; cursor: pointer; color: #4a5e54;
  transition: transform .2s cubic-bezier(0.34,1.56,0.64,1), color .2s;
  &:hover { color: var(--brand-primary); }
  &:active { transform: scale(0.85); }
}

.header-right { display: flex; align-items: center; gap: 16px; }

.notify-bell {
  display: flex; align-items: center; justify-content: center;
  width: 38px; height: 38px; border-radius: 50%;
  cursor: pointer; color: #4a5e54; font-size: 19px;
  transition: background .2s, color .2s, transform .2s cubic-bezier(0.34,1.56,0.64,1);
  &:hover { background: rgba(21, 160, 107, 0.1); color: var(--brand-primary); }
  &:active { transform: scale(0.88); }
  :deep(.el-badge__content) { border: none; }
}

.user-chip {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 10px; border-radius: 22px;
  transition: background .2s, transform .2s cubic-bezier(0.34,1.56,0.64,1);
  &:hover { background: rgba(21, 160, 107, 0.1); }
  &:active { transform: scale(0.96); }
  .user-name { font-size: 14px; font-weight: 600; color: #2c3e36; }
}
.user-avatar { background: linear-gradient(135deg, #1fb574, #0e8a5a); color: #fff; font-weight: 700; }

.layout-main {
  background: transparent;
  padding: 20px;
  overflow-y: auto;
}
</style>

<style lang="scss">
/* 通知 popover 内容被 teleport 到 body, 不能用 scoped */
.notify-popover.el-popover.el-popper {
  padding: 0;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 12px 36px rgba(16, 78, 54, 0.16);
}
.notify-panel {
  .notify-head {
    display: flex; align-items: center; justify-content: space-between;
    padding: 14px 16px;
    border-bottom: 1px solid #eef3f0;
    .notify-head-title { font-size: 15px; font-weight: 700; color: #1d2b24; }
  }
  .notify-list { padding: 4px 0; }
  .notify-item {
    position: relative;
    display: flex; align-items: flex-start; gap: 10px;
    padding: 12px 16px 12px 18px;
    cursor: pointer;
    transition: background .18s;
    &:hover { background: #f4f9f6; }
    &.unread { background: #f3faf6; }
    .notify-dot {
      position: absolute; left: 7px; top: 18px;
      width: 7px; height: 7px; border-radius: 50%;
      background: var(--brand-primary);
    }
    .notify-ic {
      flex-shrink: 0;
      width: 34px; height: 34px; border-radius: 9px;
      display: flex; align-items: center; justify-content: center;
      color: #fff; font-size: 17px;
    }
    .notify-body { flex: 1; min-width: 0; }
    .notify-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
    .notify-title {
      font-size: 14px; font-weight: 600; color: #2c3e36;
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }
    .notify-time { flex-shrink: 0; font-size: 12px; color: #a4b3ab; }
    .notify-content {
      margin-top: 3px;
      font-size: 13px; color: #6a7c72; line-height: 1.5;
      display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
    }
  }
  .notify-foot {
    display: flex; justify-content: center;
    padding: 8px 0;
    border-top: 1px solid #eef3f0;
  }
}
</style>
