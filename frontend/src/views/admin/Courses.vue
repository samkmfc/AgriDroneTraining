<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>课程管理</div>
        <div class="page-subtitle">维护在线培训课程，管理课程内容、讲师与上下架状态</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增课程</el-button>
    </div>

    <!-- 工具条 -->
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="课程标题"
        clearable
        style="width: 220px"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.category" placeholder="分类" clearable style="width: 150px">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="封面" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.cover"
              :src="row.cover"
              fit="cover"
              class="cover-img"
              :preview-src-list="[row.cover]"
              preview-teleported
            />
            <div v-else class="cover-fallback"><el-icon><Picture /></el-icon></div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="课程标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.category" effect="plain">{{ row.category }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="instructor" label="讲师" width="110">
          <template #default="{ row }">{{ row.instructor || '—' }}</template>
        </el-table-column>
        <el-table-column label="时长" width="100" align="center">
          <template #default="{ row }">{{ row.duration ? row.duration + ' 分钟' : '—' }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="学习人次" width="100" align="center">
          <template #default="{ row }">{{ row.viewCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="row._statusLoading"
              @change="(val) => onToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center">
          <template #default="{ row }">{{ row.sort ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无课程数据" /></template>
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
      :title="editingId ? '编辑课程' : '新增课程'"
      width="720px"
      top="6vh"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="18">
          <el-col :span="16">
            <el-form-item label="课程标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入课程标题" />
            </el-form-item>
            <el-form-item label="课程分类" prop="category">
              <el-select
                v-model="form.category"
                placeholder="请选择或输入分类"
                filterable
                allow-create
                default-first-option
                style="width: 100%"
              >
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
            <el-form-item label="授课讲师" prop="instructor">
              <el-input v-model="form.instructor" placeholder="请输入讲师姓名" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="时长(分钟)" prop="duration">
                  <el-input-number v-model="form.duration" :min="0" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="排序" prop="sort">
                  <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
          <el-col :span="8">
            <el-form-item label="课程封面" prop="cover" label-position="top">
              <ImageUpload v-model="form.cover" />
            </el-form-item>
            <el-form-item label="上架状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
              <span class="switch-hint">{{ form.status === 1 ? '已上架' : '已下架' }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="视频地址" prop="videoUrl">
          <div class="video-field">
            <el-input v-model="form.videoUrl" placeholder="课程视频播放地址 URL，或点击右侧上传视频" clearable />
            <el-upload
              :action="uploadAction"
              :headers="uploadHeaders"
              name="file"
              accept="video/*"
              :show-file-list="false"
              :before-upload="beforeVideoUpload"
              :on-success="onVideoSuccess"
              :on-error="() => (uploading = false)"
            >
              <el-button :icon="UploadFilled" :loading="uploading">上传视频</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item v-if="form.videoUrl" label="视频预览">
          <video :src="form.videoUrl" controls class="video-preview" preload="metadata"></video>
        </el-form-item>
        <el-form-item label="课程简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="2" placeholder="请输入课程简介" />
        </el-form-item>
        <el-form-item label="课程内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入课程详细内容（支持文本 / HTML）" />
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
import { reactive, ref, computed, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete, Picture, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { courseApi, fileApi } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'

const categories = ['法规', '飞行操作', '植保技术', '安全']

const store = useStore()
const uploadAction = fileApi.uploadUrl
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + store.state.token }))
const uploading = ref(false)

function beforeVideoUpload(file) {
  const ok = file.size / 1024 / 1024 < 200
  if (!ok) ElMessage.error('视频不能超过 200MB')
  else uploading.value = true
  return ok
}
function onVideoSuccess(res) {
  uploading.value = false
  if (res.code === 200) {
    form.videoUrl = res.data.url
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '' })

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const blankForm = () => ({
  title: '',
  category: '',
  cover: '',
  instructor: '',
  duration: 0,
  viewCount: 0,
  videoUrl: '',
  sort: 0,
  status: 1,
  intro: '',
  content: ''
})
const form = reactive(blankForm())

const rules = {
  title: [{ required: true, message: '请输入课程标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择课程分类', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const res = await courseApi.adminList({ ...query })
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
  query.category = ''
  query.pageNum = 1
  load()
}

async function onToggleStatus(row, val) {
  row._statusLoading = true
  try {
    await courseApi.adminUpdate(row.id, { ...row, status: val, _statusLoading: undefined })
    row.status = val
    ElMessage.success(val === 1 ? '已上架' : '已下架')
  } finally {
    row._statusLoading = false
  }
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
    category: row.category,
    cover: row.cover,
    instructor: row.instructor,
    duration: row.duration || 0,
    viewCount: row.viewCount || 0,
    videoUrl: row.videoUrl,
    sort: row.sort ?? 0,
    status: row.status ?? 1,
    intro: row.intro,
    content: row.content
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
        await courseApi.adminUpdate(editingId.value, payload)
        ElMessage.success('修改成功')
      } else {
        await courseApi.adminCreate(payload)
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
  ElMessageBox.confirm(`确定删除课程「${row.title}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await courseApi.adminDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    load()
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped lang="scss">
.cover-img { width: 56px; height: 40px; border-radius: 6px; display: block; }
.cover-fallback {
  width: 56px; height: 40px; border-radius: 6px;
  background: #f0f5f2; color: #b6c8be;
  display: flex; align-items: center; justify-content: center; font-size: 18px;
  margin: 0 auto;
}
.switch-hint { margin-left: 10px; font-size: 13px; color: #8a9b92; }
.video-field { display: flex; gap: 10px; width: 100%; }
.video-preview { width: 100%; max-height: 260px; border-radius: 8px; background: #000; }
</style>
