<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>试卷管理</div>
        <div class="page-subtitle">组卷配置 · 选题 / 分值 / 时长</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增试卷</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索试卷标题" clearable style="width: 240px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="title" label="试卷标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="130" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="90" align="center" />
        <el-table-column prop="passScore" label="及格分" width="90" align="center" />
        <el-table-column label="时长" width="100" align="center">
          <template #default="{ row }">{{ row.duration }} 分钟</template>
        </el-table-column>
        <el-table-column label="题目数" width="90" align="center">
          <template #default="{ row }">{{ countQuestions(row.questionIds) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无试卷数据" /></template>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑试卷' : '新增试卷'"
      width="720px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="试卷标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入试卷标题" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如 理论考核" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="form.totalScore" :min="0" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number v-model="form.passScore" :min="0" :step="5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时长(分)" prop="duration">
              <el-input-number v-model="form.duration" :min="1" :step="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="试卷说明" />
        </el-form-item>
        <el-form-item label="选择题目" prop="questionIds">
          <el-select
            v-model="form.questionIds"
            multiple
            filterable
            placeholder="搜索并选择题目"
            style="width: 100%"
            :loading="questionsLoading"
          >
            <el-option
              v-for="q in questionPool"
              :key="q.id"
              :label="`[${questionTypeMap[q.type] || q.type}] ${truncate(q.content)}`"
              :value="q.id"
            />
          </el-select>
        </el-form-item>
        <div class="picker-tip">已选 {{ form.questionIds.length }} 道题目</div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examApi } from '@/api'
import { questionTypeMap, parseJSON } from '@/utils/dict'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const questionPool = ref([])
const questionsLoading = ref(false)

const defaultForm = () => ({
  title: '', category: '', description: '',
  totalScore: 100, passScore: 60, duration: 60, status: 1, questionIds: []
})
const form = reactive(defaultForm())

const rules = {
  title: [{ required: true, message: '请输入试卷标题', trigger: 'blur' }],
  totalScore: [{ required: true, message: '请输入总分', trigger: 'blur' }],
  passScore: [{ required: true, message: '请输入及格分', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'blur' }],
  questionIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一道题目', trigger: 'change' }]
}

function truncate(s, n = 30) {
  if (!s) return ''
  return s.length > n ? s.slice(0, n) + '…' : s
}
function countQuestions(ids) {
  const arr = parseJSON(ids, [])
  return Array.isArray(arr) ? arr.length : 0
}

async function loadList() {
  loading.value = true
  try {
    const res = await examApi.adminExamList({
      pageNum: query.pageNum, pageSize: query.pageSize, keyword: query.keyword || undefined
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadQuestionPool() {
  if (questionPool.value.length) return
  questionsLoading.value = true
  try {
    const res = await examApi.questionList({ pageNum: 1, pageSize: 1000 })
    questionPool.value = res.data?.records || []
  } finally {
    questionsLoading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadList()
}
function handleReset() {
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
}
async function openCreate() {
  resetForm()
  dialogVisible.value = true
  loadQuestionPool()
}
async function openEdit(row) {
  resetForm()
  editingId.value = row.id
  const ids = parseJSON(row.questionIds, [])
  Object.assign(form, defaultForm(), {
    title: row.title, category: row.category, description: row.description,
    totalScore: row.totalScore, passScore: row.passScore, duration: row.duration,
    status: row.status, questionIds: Array.isArray(ids) ? ids : []
  })
  dialogVisible.value = true
  loadQuestionPool()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      title: form.title, category: form.category, description: form.description,
      totalScore: form.totalScore, passScore: form.passScore, duration: form.duration,
      status: form.status, questionIds: form.questionIds
    }
    if (editingId.value) {
      await examApi.adminExamUpdate(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await examApi.adminExamCreate(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除试卷「${row.title}」吗？`, '删除确认', {
    type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
  }).then(async () => {
    await examApi.adminExamDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    loadList()
  }).catch(() => {})
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.picker-tip { font-size: 13px; color: #8a9b92; padding-left: 90px; margin-top: -6px; }
</style>
