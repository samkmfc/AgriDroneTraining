<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>法规资讯管理</div>
        <div class="page-subtitle">发布和维护无人机相关法规、政策与行业资讯</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增文章</el-button>
    </div>

    <!-- 工具条 -->
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="标题关键字"
        clearable
        style="width: 220px"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.type" placeholder="类型" clearable style="width: 150px">
        <el-option label="法规" value="REGULATION" />
        <el-option label="政策" value="POLICY" />
        <el-option label="资讯" value="NEWS" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeMeta(row).type" effect="light">{{ typeMeta(row).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120">
          <template #default="{ row }">{{ row.author || '—' }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center">
          <template #default="{ row }">{{ row.viewCount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template #default="{ row }">{{ row.publishTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" effect="light">已发布</el-tag>
            <el-tag v-else type="info" effect="light">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无法规资讯数据" /></template>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="load"
          @current-change="load"
        />
      </div>
    </div>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑文章' : '新增文章'"
      width="720px"
      top="6vh"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="18">
          <el-col :span="16">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="类型" prop="type">
                  <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                    <el-option label="法规" value="REGULATION" />
                    <el-option label="政策" value="POLICY" />
                    <el-option label="资讯" value="NEWS" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                    <el-option label="发布" :value="1" />
                    <el-option label="草稿" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者 / 来源" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="封面" prop="cover" label-position="top">
              <ImageUpload v-model="form.cover" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="9" placeholder="请输入文章正文内容（支持文本 / HTML）" />
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
import { reactive, ref, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { regulationApi } from '@/api'
import { regulationTypeMap } from '@/utils/dict'
import ImageUpload from '@/components/ImageUpload.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, type: '', keyword: '' })

const typeMeta = (row) => regulationTypeMap[row.type] || { text: row.type || '未知', type: 'info' }

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const blankForm = () => ({
  title: '',
  type: 'REGULATION',
  cover: '',
  summary: '',
  content: '',
  author: '',
  status: 1
})
const form = reactive(blankForm())

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const res = await regulationApi.adminList({ ...query })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
function handleSearch() {
  query.pageNum = 1
  load()
}
function handleReset() {
  query.keyword = ''
  query.type = ''
  query.pageNum = 1
  load()
}

function resetForm() {
  Object.assign(form, blankForm())
  editingId.value = null
  formRef.value?.clearValidate()
}
function openCreate() {
  resetForm()
  dialogVisible.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, blankForm(), {
    title: row.title,
    type: row.type || 'REGULATION',
    cover: row.cover,
    summary: row.summary,
    content: row.content,
    author: row.author,
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate(async (ok) => {
    if (!ok) return
    submitting.value = true
    try {
      const payload = { ...form }
      if (editingId.value) {
        await regulationApi.adminUpdate(editingId.value, payload)
        ElMessage.success('修改成功')
      } else {
        await regulationApi.adminCreate(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      load()
    } finally {
      submitting.value = false
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除文章「${row.title}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await regulationApi.adminDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    load()
  }).catch(() => {})
}

onMounted(load)
</script>
