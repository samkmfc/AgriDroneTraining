<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>课程详情</div>
        <div class="page-subtitle">观看学习视频，系统将自动记录你的学习进度</div>
      </div>
      <el-button :icon="Back" plain @click="$router.push('/student/courses')">返回课程列表</el-button>
    </div>

    <template v-if="course.id">
      <!-- 课程头部 -->
      <div class="detail-header">
        <div class="header-cover">
          <img v-if="course.cover" :src="course.cover" alt="cover" />
          <div v-else class="cover-fallback bg-green">
            <el-icon :size="48"><VideoCamera /></el-icon>
          </div>
        </div>
        <div class="header-info">
          <el-tag :type="categoryType(course.category)" effect="dark" size="small">{{ course.category || '课程' }}</el-tag>
          <h2>{{ course.title }}</h2>
          <p class="intro">{{ course.intro }}</p>
          <div class="meta-row">
            <span><el-icon><Avatar /></el-icon>讲师 {{ course.instructor || '官方讲师' }}</span>
            <span><el-icon><Clock /></el-icon>时长 {{ course.duration || 0 }} 分钟</span>
            <span><el-icon><View /></el-icon>{{ course.viewCount || 0 }} 次学习</span>
          </div>
        </div>
      </div>

      <el-row :gutter="18" style="margin-top: 18px">
        <!-- 内容主体 -->
        <el-col :md="17">
          <div class="app-card">
            <!-- 视频播放器 -->
            <div v-if="course.videoUrl" class="video-wrap">
              <video
                ref="videoRef"
                :src="course.videoUrl"
                :poster="course.cover || undefined"
                controls
                controlslist="nodownload"
                preload="metadata"
                class="course-video"
                @timeupdate="onTimeUpdate"
                @ended="onEnded"
              ></video>
              <div class="video-bar">
                <el-icon><VideoPlay /></el-icon>
                <span>观看进度将自动同步至学习记录</span>
                <span class="watched">已观看 {{ watchedPercent }}%</span>
              </div>
            </div>
            <div v-else class="no-video">
              <el-icon :size="34"><VideoCamera /></el-icon>
              <span>本课程暂未上传视频，请阅读下方图文内容学习</span>
            </div>

            <div class="chart-title" style="margin: 18px 0 14px"><span class="bar"></span>课程内容</div>
            <div class="article" v-html="course.content || '<p>暂无课程内容</p>'"></div>
          </div>
        </el-col>

        <!-- 学习进度侧栏 -->
        <el-col :md="7">
          <div class="app-card progress-card">
            <div class="chart-title"><span class="bar"></span>学习进度</div>
            <div class="progress-dash">
              <el-progress type="dashboard" :percentage="progress" :color="progressColor" :width="140" />
            </div>
            <div class="progress-tip">
              <span v-if="progress >= 100" class="done"><el-icon><CircleCheckFilled /></el-icon> 已完成本课程学习</span>
              <span v-else>边看边学，进度自动保存</span>
            </div>

            <div v-if="course.videoUrl" class="play-actions">
              <el-button type="primary" :icon="VideoPlay" plain style="width: 100%" @click="playVideo">
                {{ progress > 0 && progress < 100 ? '继续观看' : '开始观看' }}
              </el-button>
            </div>

            <el-divider>手动标记</el-divider>
            <div class="mark-btns">
              <el-button
                v-for="p in [25, 50, 75, 100]"
                :key="p"
                size="small"
                :type="progress >= p ? 'primary' : 'default'"
                :plain="progress < p"
                @click="mark(p)"
              >{{ p }}%</el-button>
            </div>
            <el-button type="success" :icon="CircleCheck" style="width: 100%; margin-top: 14px" @click="mark(100)">
              完成学习 (100%)
            </el-button>
          </div>
        </el-col>
      </el-row>
    </template>

    <el-empty v-else-if="!loading" description="课程不存在或已下架" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, CircleCheck, VideoPlay } from '@element-plus/icons-vue'
import { courseApi } from '@/api'

const route = useRoute()
const id = route.params.id
const progressColor = '#15a06b'

const loading = ref(false)
const course = ref({})
const progress = ref(0)
const watchedPercent = ref(0)
const videoRef = ref()

// 已上报过的最高进度，避免频繁请求
let lastReported = 0

const categoryType = (c) => ({ 法规: 'primary', 飞行操作: 'success', 植保技术: 'warning', 安全: 'danger' }[c] || 'info')

async function load() {
  loading.value = true
  try {
    const res = await courseApi.detail(id)
    course.value = res.data || {}
    progress.value = course.value.progress || 0
    lastReported = progress.value
  } finally {
    loading.value = false
  }
}

function playVideo() {
  videoRef.value?.play()
  videoRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// 视频播放时根据已观看比例自动记录进度
function onTimeUpdate(e) {
  const v = e.target
  if (!v.duration) return
  const pct = Math.min(100, Math.round((v.currentTime / v.duration) * 100))
  watchedPercent.value = pct
  // 每提升 ≥10% 且高于已记录进度时上报一次
  if (pct >= lastReported + 10 && pct < 100) {
    silentSave(pct)
  }
}

function onEnded() {
  watchedPercent.value = 100
  if (progress.value < 100) mark(100)
}

async function silentSave(p) {
  try {
    const res = await courseApi.study(id, p)
    if (res.code === 200) {
      progress.value = Math.max(progress.value, p)
      lastReported = progress.value
    }
  } catch {
    /* 静默失败, 不打扰观看 */
  }
}

async function mark(p) {
  const res = await courseApi.study(id, p)
  if (res.code === 200) {
    progress.value = p
    lastReported = p
    ElMessage.success(p >= 100 ? '恭喜你完成本课程学习！' : `已记录学习进度 ${p}%`)
  } else {
    ElMessage.error(res.message || '保存进度失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.detail-header {
  display: flex; gap: 22px;
  background: #fff; border-radius: var(--card-radius); box-shadow: var(--card-shadow);
  padding: 20px; overflow: hidden;
}
.header-cover {
  width: 280px; height: 168px; border-radius: 12px; overflow: hidden; flex-shrink: 0;
  img { width: 100%; height: 100%; object-fit: cover; }
}
.cover-fallback { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #fff; }
.header-info {
  flex: 1; display: flex; flex-direction: column;
  h2 { font-size: 22px; color: #1d2b24; margin: 10px 0 8px; }
  .intro { color: #8a9b92; font-size: 14px; line-height: 1.7; flex: 1; }
}
.meta-row {
  display: flex; gap: 22px; color: #6b7d73; font-size: 13px; margin-top: 8px;
  span { display: flex; align-items: center; gap: 6px; }
}
.video-wrap { border-radius: 12px; overflow: hidden; }
.course-video { width: 100%; border-radius: 12px 12px 0 0; background: #000; max-height: 480px; display: block; }
.video-bar {
  display: flex; align-items: center; gap: 8px;
  background: #102a20; color: #9fe7c6; font-size: 13px;
  padding: 10px 14px; border-radius: 0 0 12px 12px;
  .watched { margin-left: auto; color: #2fe39a; font-weight: 600; }
}
.no-video {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 40px; border-radius: 12px; background: #f5f9f7; color: #9bb0a5; font-size: 14px;
}
.article {
  color: #3a4b42; line-height: 1.9; font-size: 15px;
  :deep(img) { max-width: 100%; border-radius: 8px; }
  :deep(p) { margin: 0 0 14px; }
  :deep(h1), :deep(h2), :deep(h3) { color: #1d2b24; margin: 18px 0 10px; }
  :deep(ul), :deep(ol) { padding-left: 22px; margin: 0 0 14px; }
  :deep(li) { margin: 4px 0; }
}
.progress-card { position: sticky; top: 16px; }
.progress-dash { display: flex; justify-content: center; padding: 14px 0 6px; }
.progress-tip {
  text-align: center; font-size: 13px; color: #8a9b92; margin-bottom: 14px;
  .done { color: var(--brand-primary); display: inline-flex; align-items: center; gap: 4px; font-weight: 600; }
}
.play-actions { margin-bottom: 6px; }
.mark-btns { display: flex; gap: 8px; .el-button { flex: 1; margin-left: 0; } }
</style>
