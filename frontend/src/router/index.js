import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import store from '@/store'

NProgress.configure({ showSpinner: false })

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { public: true } },

  /* ============ 学员端 (C端) ============ */
  {
    path: '/student',
    component: () => import('@/layout/StudentLayout.vue'),
    redirect: '/student/dashboard',
    meta: { role: 'STUDENT' },
    children: [
      { path: 'dashboard', component: () => import('@/views/student/Dashboard.vue'), meta: { title: '工作台', icon: 'Odometer' } },
      { path: 'profile', component: () => import('@/views/student/Profile.vue'), meta: { title: '个人档案', icon: 'User' } },
      { path: 'license', component: () => import('@/views/student/License.vue'), meta: { title: '我的执照', icon: 'Postcard' } },
      { path: 'courses', component: () => import('@/views/student/Courses.vue'), meta: { title: '在线课程', icon: 'Reading' } },
      { path: 'courses/:id', component: () => import('@/views/student/CourseDetail.vue'), meta: { title: '课程详情', activeMenu: '/student/courses', hidden: true } },
      { path: 'learning', component: () => import('@/views/student/Learning.vue'), meta: { title: '学习记录', icon: 'Notebook' } },
      { path: 'regulations', component: () => import('@/views/student/Regulations.vue'), meta: { title: '法规资讯', icon: 'Document' } },
      { path: 'regulations/:id', component: () => import('@/views/student/RegulationDetail.vue'), meta: { title: '资讯详情', activeMenu: '/student/regulations', hidden: true } },
      { path: 'no-fly-zone', component: () => import('@/views/student/NoFlyZone.vue'), meta: { title: '禁飞区地图', icon: 'Location' } },
      { path: 'knowledge', component: () => import('@/views/student/Knowledge.vue'), meta: { title: '植保知识库', icon: 'Sunny' } },
      { path: 'exams', component: () => import('@/views/student/ExamList.vue'), meta: { title: '场景化测试', icon: 'EditPen' } },
      { path: 'exams/:id/take', component: () => import('@/views/student/ExamTake.vue'), meta: { title: '在线答题', activeMenu: '/student/exams', hidden: true } },
      { path: 'exam-records', component: () => import('@/views/student/ExamRecords.vue'), meta: { title: '考试记录', icon: 'Tickets' } },
      { path: 'checklist', component: () => import('@/views/student/SafetyChecklist.vue'), meta: { title: '飞前安全检查', icon: 'CircleCheck' } },
      { path: 'flight-log', component: () => import('@/views/student/FlightLog.vue'), meta: { title: '飞行作业日志', icon: 'Position' } },
      { path: 'ai-assistant', component: () => import('@/views/student/AiAssistant.vue'), meta: { title: '智慧农情AI助手', icon: 'MagicStick' } }
    ]
  },

  /* ============ 管理端 (B端) ============ */
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { role: 'ADMIN' },
    children: [
      { path: 'dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '数据看板', icon: 'DataLine' } },
      { path: 'users', component: () => import('@/views/admin/Users.vue'), meta: { title: '学员与用户', icon: 'User' } },
      { path: 'licenses', component: () => import('@/views/admin/Licenses.vue'), meta: { title: '执照与预警', icon: 'Postcard' } },
      { path: 'courses', component: () => import('@/views/admin/Courses.vue'), meta: { title: '课程管理', icon: 'Reading' } },
      { path: 'regulations', component: () => import('@/views/admin/Regulations.vue'), meta: { title: '法规资讯', icon: 'Document' } },
      { path: 'no-fly-zones', component: () => import('@/views/admin/NoFlyZones.vue'), meta: { title: '禁飞区管理', icon: 'Location' } },
      { path: 'crops', component: () => import('@/views/admin/Crops.vue'), meta: { title: '作物图谱', icon: 'Sunny' } },
      { path: 'pesticides', component: () => import('@/views/admin/Pesticides.vue'), meta: { title: '农药配比库', icon: 'FirstAidKit' } },
      { path: 'questions', component: () => import('@/views/admin/Questions.vue'), meta: { title: '题库管理', icon: 'Collection' } },
      { path: 'exams', component: () => import('@/views/admin/Exams.vue'), meta: { title: '试卷管理', icon: 'EditPen' } },
      { path: 'exam-records', component: () => import('@/views/admin/ExamRecords.vue'), meta: { title: '考核记录', icon: 'Tickets' } },
      { path: 'flight-logs', component: () => import('@/views/admin/FlightLogAudit.vue'), meta: { title: '作业日志审核', icon: 'Stamp' } }
    ]
  },

  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFound.vue'), meta: { public: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  const token = store.state.token
  const role = store.state.user?.role

  if (to.meta.public) {
    // 已登录访问登录页 -> 跳到对应首页
    if (token && (to.path === '/login' || to.path === '/register')) {
      next(role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 角色访问控制
  if (to.meta.role && to.meta.role !== role) {
    next(role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard')
    return
  }
  next()
})

router.afterEach(() => NProgress.done())

export default router
