<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>法规资讯</div>
        <div class="page-subtitle">无人机相关法规、行业政策与最新资讯</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索标题 / 内容"
        clearable
        style="width: 260px"
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="filters.type" placeholder="类型" clearable style="width: 140px" @change="onSearch">
        <el-option v-for="(v, k) in typeMap" :key="k" :label="v.text" :value="k" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
    </div>

    <div v-loading="loading">
      <template v-if="list.length">
        <div v-for="item in list" :key="item.id" class="article-card" @click="goDetail(item.id)">
          <div class="ac-main">
            <div class="ac-head">
              <el-tag :type="typeMap[item.type]?.type || 'info'" size="small" effect="light">
                {{ typeMap[item.type]?.text || '资讯' }}
              </el-tag>
              <span class="ac-title">{{ item.title }}</span>
            </div>
            <div class="ac-summary">{{ item.summary || '点击查看详情' }}</div>
            <div class="ac-meta">
              <span><el-icon><Avatar /></el-icon>{{ item.author || '官方' }}</span>
              <span><el-icon><Calendar /></el-icon>{{ item.publishTime || '—' }}</span>
              <span><el-icon><View /></el-icon>{{ item.viewCount || 0 }} 阅读</span>
            </div>
          </div>
          <el-icon class="ac-arrow"><ArrowRight /></el-icon>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="暂无资讯" />
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
import { regulationApi } from '@/api'
import { regulationTypeMap } from '@/utils/dict'

const router = useRouter()
const typeMap = regulationTypeMap

const loading = ref(false)
const list = ref([])
const total = ref(0)
const filters = reactive({ pageNum: 1, pageSize: 10, type: '', keyword: '' })

async function load() {
  loading.value = true
  try {
    const res = await regulationApi.list({ ...filters })
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
  router.push(`/student/regulations/${id}`)
}

onMounted(load)
</script>

<style scoped lang="scss">
.article-card {
  display: flex; align-items: center; gap: 16px;
  background: #fff; border-radius: var(--card-radius); box-shadow: var(--card-shadow);
  padding: 18px 22px; margin-bottom: 14px; cursor: pointer;
  transition: all .25s ease;
  &:hover { transform: translateY(-2px); box-shadow: var(--card-shadow-hover);
    .ac-arrow { color: var(--brand-primary); transform: translateX(4px); } }
}
.ac-main { flex: 1; min-width: 0; }
.ac-head { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.ac-title {
  font-size: 16px; font-weight: 700; color: #1d2b24;
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;
}
.ac-summary {
  font-size: 13px; color: #8a9b92; line-height: 1.7; margin-bottom: 10px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.ac-meta {
  display: flex; gap: 20px; font-size: 12px; color: #9bb0a5;
  span { display: flex; align-items: center; gap: 5px; }
}
.ac-arrow { font-size: 20px; color: #c5d6cd; transition: all .25s ease; flex-shrink: 0; }
</style>
