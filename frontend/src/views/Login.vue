<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-overlay"></div>
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="30"><Promotion /></el-icon>
          <span>农业无人机培训管理系统</span>
        </div>
        <h1 class="brand-title">智慧赋能<br />植保飞防新未来</h1>
        <p class="brand-desc">
          集法规学习、农情辅助、实操监管、考核认证、数据看板于一体，<br />
          助力农业无人机培训规范化、智能化、数据驱动。
        </p>
        <div class="brand-features">
          <div class="feature"><el-icon><CircleCheck /></el-icon><span>飞前安全自查 · 杜绝炸机隐患</span></div>
          <div class="feature"><el-icon><MagicStick /></el-icon><span>智慧农情AI · 病虫害精准识别</span></div>
          <div class="feature"><el-icon><DataAnalysis /></el-icon><span>执照临期预警 · 作业日志防伪</span></div>
        </div>
      </div>
      <div class="brand-footer">© 2025-2027 农业无人机培训管理系统 · 罗健 202308852</div>
    </div>

    <!-- 右侧登录区 -->
    <div class="form-panel">
      <div class="login-box">
        <div class="login-head">
          <h2>{{ isRegister ? '学员注册' : '欢迎登录' }}</h2>
          <p>{{ isRegister ? '创建您的学员账号开始培训' : '请使用您的账号登录管理系统' }}</p>
        </div>

        <el-form v-if="!isRegister" ref="loginRef" :model="loginForm" :rules="rules" size="large" @keyup.enter="doLogin">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="doLogin">登 录</el-button>
        </el-form>

        <el-form v-else ref="regRef" :model="regForm" :rules="regRules" size="large">
          <el-form-item prop="username">
            <el-input v-model="regForm.username" placeholder="设置账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="realName">
            <el-input v-model="regForm.realName" placeholder="真实姓名" :prefix-icon="Avatar" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="regForm.phone" placeholder="手机号" :prefix-icon="Phone" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="regForm.password" type="password" show-password placeholder="设置密码" :prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="doRegister">注 册</el-button>
        </el-form>

        <div class="switch-row">
          <span v-if="!isRegister">还没有账号? <a @click="isRegister = true">立即注册</a></span>
          <span v-else>已有账号? <a @click="isRegister = false">返回登录</a></span>
        </div>

        <div class="demo-tip">
          <el-icon><InfoFilled /></el-icon>
          <div>
            演示账号 — 管理员: <b>admin / 123456</b> ; 学员: <b>luojian / 123456</b>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar, Phone } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()
const store = useStore()

const isRegister = ref(false)
const loading = ref(false)

const loginRef = ref()
const loginForm = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const regRef = ref()
const regForm = reactive({ username: '', realName: '', phone: '', password: '' })
const regRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }]
}

async function doLogin() {
  await loginRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const user = await store.dispatch('login', { ...loginForm })
      ElMessage.success('登录成功')
      router.push(user.role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard')
    } finally {
      loading.value = false
    }
  })
}

async function doRegister() {
  await regRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authApi.register({ ...regForm })
      ElMessage.success('注册成功, 请登录')
      isRegister.value = false
      loginForm.username = regForm.username
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-page { display: flex; height: 100vh; background: #fff; }

/* 左侧品牌 */
.brand-panel {
  position: relative;
  flex: 1.1;
  background:
    radial-gradient(circle at 20% 20%, rgba(31,181,116,.35), transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(46,160,255,.25), transparent 45%),
    linear-gradient(135deg, #0f3d2e 0%, #0a2a20 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  overflow: hidden;
}
.brand-overlay {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,.04) 1px, transparent 1px);
  background-size: 38px 38px;
  mask-image: radial-gradient(circle at 50% 50%, #000, transparent 80%);
}
.brand-content { position: relative; z-index: 2; max-width: 540px; }
.brand-logo { display: flex; align-items: center; gap: 12px; font-size: 18px; font-weight: 600; letter-spacing: 1px; }
.brand-title { font-size: 42px; line-height: 1.25; margin: 36px 0 20px; font-weight: 800; }
.brand-desc { color: #a9d6c4; font-size: 15px; line-height: 1.9; }
.brand-features { margin-top: 40px; display: flex; flex-direction: column; gap: 16px; }
.feature {
  display: flex; align-items: center; gap: 12px; font-size: 15px; color: #d6efe5;
  .el-icon { color: #2fe39a; font-size: 20px; background: rgba(47,227,154,.12); padding: 8px; border-radius: 10px; }
}
.brand-footer { position: relative; z-index: 2; margin-top: auto; color: #6b9484; font-size: 12px; }

/* 右侧表单 */
.form-panel { flex: 0.9; display: flex; align-items: center; justify-content: center; padding: 40px; }
.login-box { width: 100%; max-width: 380px; }
.login-head h2 { font-size: 28px; font-weight: 800; color: #1d2b24; margin: 0 0 8px; }
.login-head p { color: #93a59b; font-size: 14px; margin: 0 0 32px; }
.submit-btn {
  width: 100%; height: 46px; font-size: 16px; font-weight: 600; letter-spacing: 4px;
  background: linear-gradient(135deg, #1fb574, #0e8a5a); border: none; margin-top: 6px;
}
.submit-btn:hover { opacity: .92; }
.switch-row { text-align: center; margin-top: 20px; color: #93a59b; font-size: 14px; a { color: var(--brand-primary); cursor: pointer; font-weight: 600; } }
.demo-tip {
  margin-top: 28px; padding: 12px 14px; border-radius: 10px;
  background: #eef7f2; color: #4a7a64; font-size: 12.5px;
  display: flex; gap: 8px; align-items: center; line-height: 1.6;
  b { color: #0e8a5a; }
}

@media (max-width: 880px) {
  .brand-panel { display: none; }
}
</style>
