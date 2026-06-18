<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>学习记录</div>
        <div class="page-subtitle">查看你的课程学习进度，随时继续未完成的学习</div>
      </div>
      <el-button type="primary" :icon="Reading" plain @click="$router.push('/student/courses')">浏览课程</el-button>
    </div>

    <div class="table-card" v-loading="loading">
      <template v-if="list.length">
        <div v-for="item in list" :key="item.courseId" class="learn-item">
          <div class="learn-cover">
            <img v-if="item.cover" :src="item.cover" alt="cover" />
            <div v-else class="cover-fallback bg-green"><el-icon :size="26"><VideoCamera /></el-icon></div>
          </div>
          <div class="learn-main">
            <div class="learn-top">
              <span class="learn-title">{{ item.title }}</span>
              <el-tag :type="categoryType(item.category)" size="small" effect="light">{{ item.category || '课程' }}</el-tag>
              <el-tag v-if="item.finished" type="success" size="small" effect="dark">已完成</el-tag>
            </div>
            <el-progress
              :percentage="item.progress || 0"
              :stroke-width="8"
              :color="progressColor"
              style="margin: 10px 0 6px"
            />
            <div class="learn-time">
              <el-icon><Clock /></el-icon>
              最近学习：{{ item.lastStudyTime || '—' }}
            </div>
          </div>
          <div class="learn-action">
            <el-button type="primary" plain :icon="VideoPlay" @click="$router.push(`/student/courses/${item.courseId}`)">
              {{ item.finished ? '回顾课程' : '继续学习' }}
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="还没有学习记录，快去学习第一门课程吧">
        <el-button type="primary" @click="$router.push('/student/courses')">前往课程中心</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Reading } from '@element-plus/icons-vue'
import { courseApi } from '@/api'

const progressColor = '#15a06b'
const loading = ref(false)
const list = ref([])

const categoryType = (c) => ({ 法规: 'primary', 飞行操作: 'success', 植保技术: 'warning', 安全: 'danger' }[c] || 'info')

async function load() {
  loading.value = true
  try {
    const res = await courseApi.myLearning()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.learn-item {
  display: flex; align-items: center; gap: 18px;
  padding: 16px 8px; border-bottom: 1px solid #f0f4f2;
  transition: background .2s;
  &:last-child { border-bottom: none; }
  &:hover { background: #f7fbf9; }
}
.learn-cover {
  width: 110px; height: 70px; border-radius: 10px; overflow: hidden; flex-shrink: 0;
  img { width: 100%; height: 100%; object-fit: cover; }
}
.cover-fallback { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #fff; }
.learn-main { flex: 1; min-width: 0; }
.learn-top { display: flex; align-items: center; gap: 10px; }
.learn-title { font-size: 15px; font-weight: 700; color: #1d2b24; }
.learn-time { font-size: 12px; color: #9bb0a5; display: flex; align-items: center; gap: 5px; }
.learn-action { flex-shrink: 0; }
</style>
