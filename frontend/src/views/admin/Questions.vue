<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>题库管理</div>
        <div class="page-subtitle">考核试题维护 · 单选 / 多选 / 判断</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增题目</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="query.type" placeholder="题型" clearable style="width: 130px" @change="handleSearch">
        <el-option label="单选题" value="SINGLE" />
        <el-option label="多选题" value="MULTI" />
        <el-option label="判断题" value="JUDGE" />
      </el-select>
      <el-input v-model="query.category" placeholder="分类" clearable style="width: 160px" @keyup.enter="handleSearch" />
      <el-input v-model="query.keyword" placeholder="搜索题目内容" clearable style="width: 220px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="content" label="题目内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]" effect="light">{{ questionTypeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
        <el-table-column label="难度" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="difficultyMap[row.difficulty]?.type || 'info'" effect="plain">
              {{ difficultyMap[row.difficulty]?.text || row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column prop="answer" label="答案" width="100" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无题目数据" /></template>
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
      :title="editingId ? '编辑题目' : '新增题目'"
      width="720px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="题型" prop="type">
              <el-select v-model="form.type" style="width: 100%" @change="onTypeChange">
                <el-option label="单选题" value="SINGLE" />
                <el-option label="多选题" value="MULTI" />
                <el-option label="判断题" value="JUDGE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" style="width: 100%">
                <el-option label="易" :value="1" />
                <el-option label="中" :value="2" />
                <el-option label="难" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :min="1" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="如 飞行安全 / 法规" />
        </el-form-item>
        <el-form-item label="题目" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>

        <!-- 选项编辑器 (单选/多选) -->
        <el-form-item v-if="form.type !== 'JUDGE'" label="选项" required>
          <div class="opt-editor">
            <div v-for="(opt, idx) in options" :key="idx" class="opt-row">
              <span class="opt-key">{{ opt.key }}</span>
              <el-input v-model="opt.value" placeholder="请输入选项内容" />
              <el-button
                type="danger" link :icon="Delete"
                :disabled="options.length <= 2"
                @click="removeOption(idx)"
              />
            </div>
            <el-button type="primary" plain :icon="Plus" size="small" @click="addOption" :disabled="options.length >= 8">
              添加选项
            </el-button>
          </div>
        </el-form-item>

        <!-- 答案选择 -->
        <el-form-item label="答案" required>
          <el-radio-group v-if="form.type === 'SINGLE'" v-model="singleAnswer">
            <el-radio v-for="opt in options" :key="opt.key" :label="opt.key">{{ opt.key }}</el-radio>
          </el-radio-group>
          <el-checkbox-group v-else-if="form.type === 'MULTI'" v-model="multiAnswer">
            <el-checkbox v-for="opt in options" :key="opt.key" :label="opt.key">{{ opt.key }}</el-checkbox>
          </el-checkbox-group>
          <el-radio-group v-else v-model="judgeAnswer">
            <el-radio label="T">正确</el-radio>
            <el-radio label="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="解析" prop="analysis">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="答案解析(选填)" />
        </el-form-item>
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

const typeTagMap = { SINGLE: 'success', MULTI: 'warning', JUDGE: 'info' }
const difficultyMap = { 1: { text: '易', type: 'success' }, 2: { text: '中', type: 'warning' }, 3: { text: '难', type: 'danger' } }
const KEYS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, type: '', category: '', keyword: '' })

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const defaultForm = () => ({
  type: 'SINGLE', category: '', content: '', difficulty: 1, score: 5, analysis: ''
})
const form = reactive(defaultForm())

// 选项与答案
const options = ref([{ key: 'A', value: '' }, { key: 'B', value: '' }])
const singleAnswer = ref('')
const multiAnswer = ref([])
const judgeAnswer = ref('T')

const rules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }]
}

function rekey() {
  options.value.forEach((o, i) => { o.key = KEYS[i] })
}
function addOption() {
  options.value.push({ key: KEYS[options.value.length], value: '' })
}
function removeOption(idx) {
  const removedKey = options.value[idx].key
  options.value.splice(idx, 1)
  rekey()
  // 清理失效答案
  if (singleAnswer.value === removedKey) singleAnswer.value = ''
  multiAnswer.value = multiAnswer.value.filter((k) => options.value.some((o) => o.key === k))
}
function onTypeChange() {
  if (form.type === 'JUDGE') {
    judgeAnswer.value = 'T'
  } else {
    if (!options.value.length) options.value = [{ key: 'A', value: '' }, { key: 'B', value: '' }]
    rekey()
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await examApi.questionList({
      pageNum: query.pageNum, pageSize: query.pageSize,
      type: query.type || undefined, category: query.category || undefined, keyword: query.keyword || undefined
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
  query.type = ''
  query.category = ''
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, defaultForm())
  options.value = [{ key: 'A', value: '' }, { key: 'B', value: '' }]
  singleAnswer.value = ''
  multiAnswer.value = []
  judgeAnswer.value = 'T'
  formRef.value?.clearValidate()
}
function openCreate() {
  resetForm()
  dialogVisible.value = true
}
function openEdit(row) {
  resetForm()
  editingId.value = row.id
  Object.assign(form, {
    type: row.type, category: row.category, content: row.content,
    difficulty: row.difficulty, score: row.score, analysis: row.analysis
  })
  if (row.type === 'JUDGE') {
    judgeAnswer.value = row.answer === 'F' ? 'F' : 'T'
  } else {
    const parsed = parseJSON(row.options, [])
    options.value = Array.isArray(parsed) && parsed.length
      ? parsed.map((o) => ({ key: o.key, value: o.value }))
      : [{ key: 'A', value: '' }, { key: 'B', value: '' }]
    rekey()
    if (row.type === 'SINGLE') {
      singleAnswer.value = row.answer || ''
    } else {
      multiAnswer.value = (row.answer || '').split(',').map((s) => s.trim()).filter(Boolean)
    }
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()

  let optionList, answer
  if (form.type === 'JUDGE') {
    optionList = [{ key: 'T', value: '正确' }, { key: 'F', value: '错误' }]
    answer = judgeAnswer.value
  } else {
    if (options.value.some((o) => !o.value.trim())) {
      ElMessage.warning('请填写所有选项内容')
      return
    }
    optionList = options.value.map((o) => ({ key: o.key, value: o.value.trim() }))
    if (form.type === 'SINGLE') {
      if (!singleAnswer.value) { ElMessage.warning('请选择正确答案'); return }
      answer = singleAnswer.value
    } else {
      if (!multiAnswer.value.length) { ElMessage.warning('请选择至少一个正确答案'); return }
      answer = [...multiAnswer.value].sort().join(',')
    }
  }

  submitting.value = true
  try {
    const payload = {
      type: form.type, category: form.category, content: form.content,
      options: JSON.stringify(optionList), answer,
      analysis: form.analysis, difficulty: form.difficulty, score: form.score
    }
    if (editingId.value) {
      await examApi.questionUpdate(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await examApi.questionCreate(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该题目吗？', '删除确认', {
    type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
  }).then(async () => {
    await examApi.questionDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    loadList()
  }).catch(() => {})
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.opt-editor { width: 100%; }
.opt-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 10px;
}
.opt-row .opt-key {
  width: 28px; height: 28px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: var(--brand-primary-light); color: var(--brand-primary);
  font-weight: 700; border-radius: 6px; font-size: 13px;
}
</style>
