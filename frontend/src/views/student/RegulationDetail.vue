<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>资讯详情</div>
        <div class="page-subtitle">法规政策 · 行业资讯</div>
      </div>
      <el-button :icon="Back" plain @click="$router.push('/student/regulations')">返回列表</el-button>
    </div>

    <div v-if="article.id" class="app-card article-wrap">
      <h1 class="art-title">{{ article.title }}</h1>
      <div class="art-meta">
        <el-tag :type="typeMap[article.type]?.type || 'info'" size="small" effect="light">
          {{ typeMap[article.type]?.text || '资讯' }}
        </el-tag>
        <span><el-icon><Avatar /></el-icon>{{ article.author || '官方' }}</span>
        <span><el-icon><Calendar /></el-icon>{{ article.publishTime || '—' }}</span>
        <span><el-icon><View /></el-icon>{{ article.viewCount || 0 }} 阅读</span>
      </div>
      <el-divider />
      <div class="article" v-html="article.content || '<p>暂无内容</p>'"></div>
    </div>

    <el-empty v-else-if="!loading" description="资讯不存在或已删除" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Back } from '@element-plus/icons-vue'
import { regulationApi } from '@/api'
import { regulationTypeMap } from '@/utils/dict'

const route = useRoute()
const id = route.params.id
const typeMap = regulationTypeMap

const loading = ref(false)
const article = ref({})

async function load() {
  loading.value = true
  try {
    const res = await regulationApi.detail(id)
    article.value = res.data || {}
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.article-wrap { padding: 32px 40px; }
.art-title {
  font-size: 26px; font-weight: 700; color: #1d2b24; line-height: 1.4;
  max-width: 820px; margin: 0 auto 18px; text-align: center;
}
.art-meta {
  display: flex; align-items: center; justify-content: center; gap: 22px;
  font-size: 13px; color: #8a9b92;
  span { display: flex; align-items: center; gap: 5px; }
}
.article {
  max-width: 820px; margin: 0 auto; color: #3a4b42; line-height: 1.9; font-size: 16px;
  :deep(img) { max-width: 100%; border-radius: 8px; }
  :deep(p) { margin: 0 0 16px; }
  :deep(h1), :deep(h2), :deep(h3) { color: #1d2b24; margin: 22px 0 12px; }
}
</style>
