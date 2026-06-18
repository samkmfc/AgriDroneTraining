<template>
  <div class="nf">
    <div class="nf-code">404</div>
    <div class="nf-text">抱歉，您访问的页面不存在</div>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
const router = useRouter()
const store = useStore()
function goHome() {
  const role = store.state.user?.role
  if (!store.state.token) return router.push('/login')
  router.push(role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard')
}
</script>

<style scoped>
.nf { height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; background: var(--app-bg); }
.nf-code { font-size: 120px; font-weight: 800; background: linear-gradient(135deg, #1fb574, #0e8a5a); -webkit-background-clip: text; background-clip: text; color: transparent; }
.nf-text { font-size: 18px; color: #7a8e84; margin-bottom: 10px; }
</style>
