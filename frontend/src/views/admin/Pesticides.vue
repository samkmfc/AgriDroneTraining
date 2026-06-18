<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>农药配比库管理</div>
        <div class="page-subtitle">作物 / 病虫害对应农药配比方案维护</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增配比</el-button>
    </div>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索农药名称"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input
        v-model="query.cropName"
        placeholder="适用作物"
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
        <el-table-column prop="pesticideName" label="农药名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="cropName" label="适用作物" width="120" show-overflow-tooltip />
        <el-table-column prop="pestName" label="防治对象" width="130" show-overflow-tooltip />
        <el-table-column prop="dosage" label="用量" width="120" show-overflow-tooltip />
        <el-table-column prop="waterRatio" label="兑水比例" width="120" show-overflow-tooltip />
        <el-table-column prop="sprayMethod" label="喷洒方式" width="130" show-overflow-tooltip />
        <el-table-column prop="safetyInterval" label="安全间隔期" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无农药配比数据" /></template>
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
      :title="editingId ? '编辑农药配比' : '新增农药配比'"
      width="640px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="农药名称" prop="pesticideName">
              <el-input v-model="form.pesticideName" placeholder="请输入农药名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用作物" prop="cropName">
              <el-input v-model="form.cropName" placeholder="请输入适用作物" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防治对象" prop="pestName">
              <el-input v-model="form.pestName" placeholder="如 蚜虫" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用量" prop="dosage">
              <el-input v-model="form.dosage" placeholder="如 30ml/亩" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="兑水比例" prop="waterRatio">
              <el-input v-model="form.waterRatio" placeholder="如 1:1000" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="喷洒方式" prop="sprayMethod">
              <el-input v-model="form.sprayMethod" placeholder="如 叶面喷雾" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全间隔期" prop="safetyInterval">
              <el-input v-model="form.safetyInterval" placeholder="如 7天" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="notes">
          <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="使用注意事项" />
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
import { knowledgeApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', cropName: '' })

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const defaultForm = () => ({
  pesticideName: '', cropName: '', pestName: '', dosage: '',
  waterRatio: '', sprayMethod: '', safetyInterval: '', notes: ''
})
const form = reactive(defaultForm())

const rules = {
  pesticideName: [{ required: true, message: '请输入农药名称', trigger: 'blur' }],
  cropName: [{ required: true, message: '请输入适用作物', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await knowledgeApi.adminPesticideList({
      pageNum: query.pageNum, pageSize: query.pageSize,
      keyword: query.keyword || undefined, cropName: query.cropName || undefined
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
  query.cropName = ''
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
    pesticideName: row.pesticideName, cropName: row.cropName, pestName: row.pestName,
    dosage: row.dosage, waterRatio: row.waterRatio, sprayMethod: row.sprayMethod,
    safetyInterval: row.safetyInterval, notes: row.notes
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await knowledgeApi.adminPesticideUpdate(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await knowledgeApi.adminPesticideCreate(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除配比「${row.pesticideName}」吗？`, '删除确认', {
    type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
  }).then(async () => {
    await knowledgeApi.adminPesticideDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    loadList()
  }).catch(() => {})
}

onMounted(loadList)
</script>
