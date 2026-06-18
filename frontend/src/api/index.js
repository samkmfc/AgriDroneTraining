import request from '@/utils/request'

/* ============ 认证 ============ */
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  me: () => request.get('/auth/me'),
  logout: () => request.post('/auth/logout')
}

/* ============ 用户 / 档案 ============ */
export const userApi = {
  updateProfile: (data) => request.put('/user/profile', data),
  updatePassword: (data) => request.put('/user/password', data),
  // 学员档案
  getStudentProfile: () => request.get('/student/profile'),
  updateStudentProfile: (data) => request.put('/student/profile', data),
  // 管理端用户管理
  adminList: (params) => request.get('/admin/users', { params }),
  adminCreate: (data) => request.post('/admin/users', data),
  adminUpdate: (id, data) => request.put(`/admin/users/${id}`, data),
  adminDelete: (id) => request.delete(`/admin/users/${id}`),
  adminSetStatus: (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } }),
  adminStudentDetail: (userId) => request.get(`/admin/students/${userId}`)
}

/* ============ 执照 ============ */
export const licenseApi = {
  my: () => request.get('/license/my'),
  adminList: (params) => request.get('/admin/licenses', { params }),
  adminCreate: (data) => request.post('/admin/licenses', data),
  adminUpdate: (id, data) => request.put(`/admin/licenses/${id}`, data),
  adminDelete: (id) => request.delete(`/admin/licenses/${id}`),
  expiring: () => request.get('/admin/licenses/expiring'),
  renew: (id, months) => request.put(`/admin/licenses/${id}/renew`, null, { params: { months } })
}

/* ============ 课程 / 学习 ============ */
export const courseApi = {
  list: (params) => request.get('/courses', { params }),
  detail: (id) => request.get(`/courses/${id}`),
  study: (id, progress) => request.post(`/courses/${id}/study`, { progress }),
  myLearning: () => request.get('/learning/my'),
  adminList: (params) => request.get('/admin/courses', { params }),
  adminCreate: (data) => request.post('/admin/courses', data),
  adminUpdate: (id, data) => request.put(`/admin/courses/${id}`, data),
  adminDelete: (id) => request.delete(`/admin/courses/${id}`)
}

/* ============ 法规 / 政策 / 资讯 ============ */
export const regulationApi = {
  list: (params) => request.get('/regulations', { params }),
  detail: (id) => request.get(`/regulations/${id}`),
  adminList: (params) => request.get('/admin/regulations', { params }),
  adminCreate: (data) => request.post('/admin/regulations', data),
  adminUpdate: (id, data) => request.put(`/admin/regulations/${id}`, data),
  adminDelete: (id) => request.delete(`/admin/regulations/${id}`)
}

/* ============ 禁飞区 ============ */
export const noFlyZoneApi = {
  list: (params) => request.get('/no-fly-zones', { params }),
  adminList: (params) => request.get('/admin/no-fly-zones', { params }),
  adminCreate: (data) => request.post('/admin/no-fly-zones', data),
  adminUpdate: (id, data) => request.put(`/admin/no-fly-zones/${id}`, data),
  adminDelete: (id) => request.delete(`/admin/no-fly-zones/${id}`)
}

/* ============ 植保知识库 (作物图谱 / 农药配比) ============ */
export const knowledgeApi = {
  crops: (params) => request.get('/crops', { params }),
  cropDetail: (id) => request.get(`/crops/${id}`),
  pesticides: (params) => request.get('/pesticides', { params }),
  adminCropList: (params) => request.get('/admin/crops', { params }),
  adminCropCreate: (data) => request.post('/admin/crops', data),
  adminCropUpdate: (id, data) => request.put(`/admin/crops/${id}`, data),
  adminCropDelete: (id) => request.delete(`/admin/crops/${id}`),
  adminPesticideList: (params) => request.get('/admin/pesticides', { params }),
  adminPesticideCreate: (data) => request.post('/admin/pesticides', data),
  adminPesticideUpdate: (id, data) => request.put(`/admin/pesticides/${id}`, data),
  adminPesticideDelete: (id) => request.delete(`/admin/pesticides/${id}`)
}

/* ============ 题库 / 考试 ============ */
export const examApi = {
  list: () => request.get('/exams'),
  paper: (id) => request.get(`/exams/${id}/paper`),
  submit: (id, data) => request.post(`/exams/${id}/submit`, data),
  myRecords: () => request.get('/exam-records/my'),
  recordDetail: (id) => request.get(`/exam-records/${id}`),
  // 管理端
  questionList: (params) => request.get('/admin/questions', { params }),
  questionCreate: (data) => request.post('/admin/questions', data),
  questionUpdate: (id, data) => request.put(`/admin/questions/${id}`, data),
  questionDelete: (id) => request.delete(`/admin/questions/${id}`),
  adminExamList: (params) => request.get('/admin/exams', { params }),
  adminExamCreate: (data) => request.post('/admin/exams', data),
  adminExamUpdate: (id, data) => request.put(`/admin/exams/${id}`, data),
  adminExamDelete: (id) => request.delete(`/admin/exams/${id}`),
  adminRecords: (params) => request.get('/admin/exam-records', { params })
}

/* ============ 飞前安全检查单 ============ */
export const checklistApi = {
  submit: (data) => request.post('/safety-checklists', data),
  my: (params) => request.get('/safety-checklists/my', { params }),
  detail: (id) => request.get(`/safety-checklists/${id}`)
}

/* ============ 飞行作业日志 ============ */
export const flightLogApi = {
  submit: (data) => request.post('/flight-logs', data),
  my: (params) => request.get('/flight-logs/my', { params }),
  detail: (id) => request.get(`/flight-logs/${id}`),
  adminList: (params) => request.get('/admin/flight-logs', { params }),
  audit: (id, data) => request.put(`/admin/flight-logs/${id}/audit`, data),
  batchAudit: (data) => request.put('/admin/flight-logs/batch-audit', data)
}

/* ============ 智慧农情 AI 助手 ============ */
export const aiApi = {
  diagnose: (data) => request.post('/ai/diagnose', data),
  history: (params) => request.get('/ai/history/my', { params })
}

/* ============ 统计看板 ============ */
export const statApi = {
  overview: () => request.get('/admin/statistics/overview'),
  examPass: () => request.get('/admin/statistics/exam-pass'),
  flightLogTrend: () => request.get('/admin/statistics/flight-log-trend'),
  visitTrend: () => request.get('/admin/statistics/visit-trend'),
  licenseStatus: () => request.get('/admin/statistics/license-status'),
  modelDistribution: () => request.get('/admin/statistics/model-distribution'),
  studentDashboard: () => request.get('/student/dashboard')
}

/* ============ 文件 / 访问日志 ============ */
export const fileApi = {
  uploadUrl: '/api/files/upload'
}
export const accessApi = {
  log: (data) => request.post('/access/log', data)
}

/* ============ 消息通知 ============ */
export const notificationApi = {
  my: (params) => request.get('/notifications/my', { params }),
  unreadCount: () => request.get('/notifications/unread-count'),
  read: (id) => request.put(`/notifications/${id}/read`),
  readAll: () => request.put('/notifications/read-all'),
  remove: (id) => request.delete(`/notifications/${id}`)
}
