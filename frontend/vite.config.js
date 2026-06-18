import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 农业无人机培训管理系统 前端构建配置
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      // 后端 context-path 为 /api, 直接透传
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
