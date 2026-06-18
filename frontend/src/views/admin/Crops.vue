<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>作物图谱管理</div>
        <div class="page-subtitle">作物种植图谱与病虫害知识维护</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增作物</el-button>
    </div>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索作物名称"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input
        v-model="query.category"
        placeholder="作物分类"
        clearable
        style="width: 180px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column label="图片" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              :preview-src-list="[row.image]"
              preview-teleported
              fit="cover"
              class="thumb"
            />
            <div v-else class="thumb thumb-fallback"><el-icon><Picture /></el-icon></div>
          </template>
        </el-table-column>
        <el-table-column prop="cropName" label="作物名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="130" show-overflow-tooltip />
        <el-table-column prop="growthCycle" label="生长周期" width="140" show-overflow-tooltip />
        <el-table-column prop="commonPests" label="常见病虫害" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无作物数据" /></template>
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
      :title="editingId ? '编辑作物' : '新增作物'"
      width="640px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="作物图片" prop="image">
          <image-upload v-model="form.image" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作物名称" prop="cropName">
              <el-input v-model="form.cropName" placeholder="请输入作物名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如 粮食作物 / 经济作物" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生长周期" prop="growthCycle">
              <el-input v-model="form.growthCycle" placeholder="如 90-120天" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="常见病虫害" prop="commonPests">
              <el-input v-model="form.commonPests" placeholder="如 蚜虫、稻飞虱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="作物种植与防治说明" />
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
import { Plus, Search, Refresh, Edit, Delete, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { knowledgeApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '' })

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const defaultForm = () => ({
  cropName: '', category: '', growthCycle: '', image: '', commonPests: '', description: ''
})
const form = reactive(defaultForm())

const rules = {
  cropName: [{ required: true, message: '请输入作物名称', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await knowledgeApi.adminCropList({
      pageNum: query.pageNum, pageSize: query.pageSize,
      keyword: query.keyword || undefined, category: query.category || undefined
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
  query.keyword = ''
  query.category = ''
  query.pageNum = 1
  loadList()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
}
function openCreate() {
  resetForm()
  dialogVisible.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, defaultForm(), {
    cropName: row.cropName, category: row.category, growthCycle: row.growthCycle,
    image: row.image, commonPests: row.commonPests, description: row.description
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await knowledgeApi.adminCropUpdate(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await knowledgeApi.adminCropCreate(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除作物「${row.cropName}」吗？`, '删除确认', {
    type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
  }).then(async () => {
    await knowledgeApi.adminCropDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    loadList()
  }).catch(() => {})
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.thumb {
  width: 52px; height: 52px; border-radius: 8px; display: block;
}
.thumb-fallback {
  display: flex; align-items: center; justify-content: center;
  background: #f5f9f7; color: #9bb0a5; font-size: 20px;
}
</style>
