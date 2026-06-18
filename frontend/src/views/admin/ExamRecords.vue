<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>考核记录</div>
        <div class="page-subtitle">学员考试成绩与通过情况查询</div>
      </div>
      <el-button :icon="Refresh" circle @click="loadList" />
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="18" style="margin-bottom: 18px">
      <el-col :xs="12" :sm="8" :md="6">
        <div class="stat-card">
          <div class="stat-icon bg-blue"><el-icon><Tickets /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ total }}</div>
            <div class="stat-label">总考核次数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <div class="stat-card">
          <div class="stat-icon bg-green"><el-icon><TrophyBase /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ passRate }}%</div>
            <div class="stat-label">本页通过率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="toolbar">
      <el-select v-model="query.examId" placeholder="筛选试卷" clearable filterable style="width: 240px" @change="handleSearch">
        <el-option v-for="e in exams" :key="e.id" :label="e.title" :value="e.id" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button :icon="Download" @click="handleExport">导出CSV</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="realName" label="学员" width="130" show-overflow-tooltip />
        <el-table-column prop="examTitle" label="试卷" min-width="200" show-overflow-tooltip />
        <el-table-column prop="score" label="得分" width="100" align="center">
          <template #default="{ row }">
            <span class="score-val">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.passed ? 'success' : 'danger'" effect="light">
              {{ row.passed ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用时" width="120" align="center">
          <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <template #empty><el-empty description="暂无考核记录" /></template>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Refresh, Search, Tickets, TrophyBase, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { examApi } from '@/api'
import { exportCsv } from '@/utils/export'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const exams = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, examId: null })

const passRate = computed(() => {
  if (!list.value.length) return 0
  const pass = list.value.filter((r) => r.passed).length
  return Math.round((pass / list.value.length) * 100)
})

function formatDuration(sec) {
  if (sec == null) return '-'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}分${s}秒`
}

async function loadExams() {
  try {
    const res = await examApi.adminExamList({ pageNum: 1, pageSize: 1000 })
    exams.value = res.data?.records || []
  } catch {
    exams.value = []
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await examApi.adminRecords({
      pageNum: query.pageNum, pageSize: query.pageSize, examId: query.examId || undefined
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadList()
}
function handleReset() {
  query.examId = null
  query.pageNum = 1
  loadList()
}

function handleExport() {
  if (!list.value.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  const columns = [
    { label: '学员', prop: 'realName' },
    { label: '试卷', prop: 'examTitle' },
    { label: '得分', prop: 'score' },
    { label: '结果', prop: 'passed', formatter: (v) => (v ? '通过' : '未通过') },
    { label: '用时(秒)', prop: 'duration' },
    { label: '提交时间', prop: 'submitTime' }
  ]
  exportCsv('考核记录', columns, list.value)
}

onMounted(() => {
  loadExams()
  loadList()
})
</script>

<style scoped lang="scss">
.score-val { font-weight: 700; color: #1d2b24; }
</style>
