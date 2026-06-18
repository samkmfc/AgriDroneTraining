import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import store from '@/store'

// 后端 context-path 为 /api, dev 环境通过 vite 代理转发到 8080
const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截: 注入 JWT
service.interceptors.request.use(
  (config) => {
    const token = store.state.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截: 统一处理 Result
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 文件流等非标准响应直接返回
    if (response.config.responseType === 'blob') return response
    if (res.code === undefined) return res

    if (res.code === 200) {
      return res
    }
    // 401 未登录/过期
    if (res.code === 401) {
      ElMessage.error(res.message || '登录已过期, 请重新登录')
      store.dispatch('logout')
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      store.dispatch('logout')
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
