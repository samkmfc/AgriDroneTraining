<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>在线课程</div>
        <div class="page-subtitle">系统化学习无人机法规、飞行操作与植保技术</div>
      </div>
    </div>

    <!-- 工具条 -->
    <div class="toolbar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索课程名称 / 讲师"
        clearable
        style="width: 260px"
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="filters.category" placeholder="课程分类" clearable style="width: 160px" @change="onSearch">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
      <div class="spacer"></div>
      <el-button text :icon="Notebook" @click="$router.push('/student/learning')">我的学习记录</el-button>
    </div>

    <!-- 课程网格 -->
    <div v-loading="loading">
      <el-row v-if="list.length" :gutter="18">
        <el-col v-for="item in list" :key="item.id" :xs="24" :sm="12" :md="6" style="margin-bottom: 18px">
          <div class="course-card" @click="goDetail(item.id)">
            <div class="cover">
              <img v-if="item.cover" :src="item.cover" alt="cover" />
              <div v-else class="cover-fallback" :class="bannerClass(item.id)">
                <el-icon :size="34"><VideoCamera /></el-icon>
                <span class="cover-title">{{ item.title }}</span>
              </div>
              <el-tag class="cover-tag" :type="categoryType(item.category)" effect="dark" size="small">
                {{ item.category || '课程' }}
              </el-tag>
            </div>
            <div class="card-body">
              <div class="course-title">{{ item.title }}</div>
              <div class="course-intro">{{ item.intro || '暂无简介' }}</div>
              <el-progress
                v-if="item.progress != null"
                :percentage="item.progress"
                :stroke-width="6"
                :color="progressColor"
                style="margin: 6px 0 2px"
              />
              <div class="course-footer">
                <span><el-icon><Avatar /></el-icon>{{ item.instructor || '官方讲师' }}</span>
                <span><el-icon><Clock /></el-icon>{{ item.duration || 0 }}分钟</span>
                <span><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-else-if="!loading" description="暂无课程" />
    </div>

    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        layout="total, prev, pager, next, jumper"
        :total="total"
        :current-page="filters.pageNum"
        :page-size="filters.pageSize"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { courseApi } from '@/api'

const router = useRouter()
const categories = ['法规', '飞行操作', '植保技术', '安全']
const progressColor = '#15a06b'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const filters = reactive({ pageNum: 1, pageSize: 8, category: '', keyword: '' })

const banners = ['bg-green', 'bg-blue', 'bg-orange', 'bg-purple', 'bg-red']
const bannerClass = (id) => banners[(Number(id) || 0) % banners.length]

const categoryType = (c) => ({ 法规: 'primary', 飞行操作: 'success', 植保技术: 'warning', 安全: 'danger' }[c] || 'info')

async function load() {
  loading.value = true
  try {
    const res = await courseApi.list({ ...filters })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  filters.pageNum = 1
  load()
}
function onPageChange(p) {
  filters.pageNum = p
  load()
}
function goDetail(id) {
  router.push(`/student/courses/${id}`)
}

onMounted(load)
</script>

<style scoped lang="scss">
.course-card {
  background: #fff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  overflow: hidden;
  cursor: pointer;
  transition: all .25s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  &:hover { transform: translateY(-4px); box-shadow: var(--card-shadow-hover); }
}
.cover {
  position: relative;
  height: 150px;
  overflow: hidden;
  img { width: 100%; height: 100%; object-fit: cover; display: block; }
}
.cover-fallback {
  width: 100%; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; color: #fff; padding: 14px;
  .cover-title { font-size: 14px; font-weight: 600; text-align: center; opacity: .95;
    display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
}
.cover-tag { position: absolute; top: 10px; left: 10px; border: none; }
.card-body { padding: 14px 16px 16px; flex: 1; display: flex; flex-direction: column; }
.course-title {
  font-size: 15px; font-weight: 700; color: #1d2b24; margin-bottom: 6px;
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;
}
.course-intro {
  font-size: 13px; color: #8a9b92; line-height: 1.6; flex: 1; min-height: 40px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.course-footer {
  display: flex; gap: 14px; margin-top: 10px; padding-top: 10px;
  border-top: 1px solid #f0f4f2; font-size: 12px; color: #9bb0a5;
  span { display: flex; align-items: center; gap: 4px; }
}
</style>
