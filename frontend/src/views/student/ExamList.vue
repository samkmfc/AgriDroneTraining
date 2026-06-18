<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>场景化测试</div>
        <div class="page-subtitle">围绕法规、安全与植保作业的场景化在线测评，检验你的学习成果</div>
      </div>
      <el-button text :icon="Tickets" @click="$router.push('/student/exam-records')">考试记录</el-button>
    </div>

    <div v-loading="loading">
      <el-row v-if="list.length" :gutter="18">
        <el-col v-for="exam in list" :key="exam.id" :xs="24" :sm="12" :md="8" style="margin-bottom: 18px">
          <div class="exam-card">
            <div class="exam-top">
              <div class="exam-icon" :class="bannerClass(exam.id)">
                <el-icon :size="22"><EditPen /></el-icon>
              </div>
              <el-tag :type="categoryType(exam.category)" effect="light" size="small">{{ exam.category || '综合' }}</el-tag>
            </div>

            <div class="exam-title">{{ exam.title }}</div>
            <div class="exam-desc">{{ exam.description || '暂无说明，点击开始测试即可作答。' }}</div>

            <div class="exam-meta">
              <div class="meta-cell"><span class="m-val">{{ exam.questionCount ?? 0 }}</span><span class="m-key">题目数</span></div>
              <div class="meta-cell"><span class="m-val">{{ exam.totalScore ?? 0 }}</span><span class="m-key">总分</span></div>
              <div class="meta-cell"><span class="m-val">{{ exam.passScore ?? 0 }}</span><span class="m-key">及格</span></div>
              <div class="meta-cell"><span class="m-val">{{ exam.duration ?? 0 }}</span><span class="m-key">分钟</span></div>
            </div>

            <el-button type="primary" :icon="VideoPlay" class="exam-btn" @click="start(exam.id)">开始测试</el-button>
          </div>
        </el-col>
      </el-row>

      <el-empty v-else-if="!loading" description="暂无可参加的测试" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Tickets } from '@element-plus/icons-vue'
import { examApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const list = ref([])

const banners = ['bg-green', 'bg-blue', 'bg-orange', 'bg-purple', 'bg-red']
const bannerClass = (id) => banners[(Number(id) || 0) % banners.length]
const categoryType = (c) => ({ 法规: 'primary', 安全: 'danger', 植保: 'warning', 飞行操作: 'success' }[c] || 'info')

function start(id) {
  router.push(`/student/exams/${id}/take`)
}

async function load() {
  loading.value = true
  try {
    const res = await examApi.list()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.exam-card {
  background: #fff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all .25s ease;
  &:hover { transform: translateY(-4px); box-shadow: var(--card-shadow-hover); }
}
.exam-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.exam-icon {
  width: 46px; height: 46px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.exam-title {
  font-size: 16px; font-weight: 700; color: #1d2b24; margin-bottom: 6px;
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;
}
.exam-desc {
  font-size: 13px; color: #8a9b92; line-height: 1.6; flex: 1; min-height: 42px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.exam-meta {
  display: flex; margin: 14px 0; padding: 12px 0;
  border-top: 1px solid #f0f4f2; border-bottom: 1px solid #f0f4f2;
}
.meta-cell {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 2px;
  border-right: 1px solid #f0f4f2;
  &:last-child { border-right: none; }
}
.m-val { font-size: 18px; font-weight: 700; color: var(--brand-primary); }
.m-key { font-size: 12px; color: #9bb0a5; }
.exam-btn { width: 100%; }
</style>
