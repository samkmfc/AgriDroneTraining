import { createStore } from 'vuex'
import { authApi } from '@/api'

const store = createStore({
  state: {
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    sidebarCollapsed: false
  },
  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    isAdmin: (state) => state.user?.role === 'ADMIN',
    avatar: (state) => state.user?.avatar || '',
    realName: (state) => state.user?.realName || state.user?.username || ''
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER(state, user) {
      state.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    CLEAR_AUTH(state) {
      state.token = ''
      state.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    TOGGLE_SIDEBAR(state) {
      state.sidebarCollapsed = !state.sidebarCollapsed
    }
  },
  actions: {
    async login({ commit }, loginData) {
      const res = await authApi.login(loginData)
      commit('SET_TOKEN', res.data.token)
      commit('SET_USER', res.data.user)
      return res.data.user
    },
    async fetchMe({ commit, state }) {
      const res = await authApi.me()
      // 合并最新用户信息
      commit('SET_USER', { ...state.user, ...res.data })
      return res.data
    },
    logout({ commit }) {
      commit('CLEAR_AUTH')
    }
  }
})

export default store
