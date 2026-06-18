<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>学员与用户管理</div>
        <div class="page-subtitle">管理平台学员与管理员账号、权限及启用状态</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
    </div>

    <!-- 工具条 -->
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="账号 / 姓名 / 手机"
        clearable
        style="width: 240px"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 140px">
        <el-option label="学员" value="STUDENT" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button :icon="Download" @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="头像" width="72" align="center">
          <template #default="{ row }">
            <el-avatar :size="38" :src="row.avatar">
              {{ (row.realName || row.username || '?').charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100">
          <template #default="{ row }">{{ row.realName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '—' }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'ADMIN'" type="danger" effect="light">管理员</el-tag>
            <el-tag v-else type="success" effect="light">学员</el-tag>
          </template>
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
        <el-table-column prop="lastLoginTime" label="最后登录" width="170">
          <template #default="{ row }">{{ row.lastLoginTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role === 'STUDENT'"
              link
              type="primary"
              :icon="Folder"
              @click="openArchive(row)"
            >档案</el-button>
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无用户数据" /></template>
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
      :title="editingId ? '编辑用户' : '新增用户'"
      width="560px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学员" value="STUDENT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="!editingId"
          type="info"
          :closable="false"
          show-icon
          title="新建用户默认初始密码为 123456，请提醒用户首次登录后及时修改。"
          style="border-radius: 8px"
        />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 学员档案 -->
    <el-dialog v-model="archiveVisible" title="学员档案" width="560px">
      <div v-loading="archiveLoading">
        <el-descriptions v-if="archive" :column="2" border>
          <el-descriptions-item label="账号">{{ archive.username || '—' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ archive.realName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderText(archive.gender) }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ archive.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">{{ archive.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号" :span="2">{{ archive.idCard || '—' }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ archive.education || '—' }}</el-descriptions-item>
          <el-descriptions-item label="入学时间">{{ archive.enrollDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="培训状态">
            <el-tag v-if="trainingTag" :type="trainingTag.type" effect="light">{{ trainingTag.text }}</el-tag>
            <span v-else>—</span>
          </el-descriptions-item>
          <el-descriptions-item label="联系地址" :span="2">{{ archive.address || '—' }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ archive.emergencyContact || '—' }}</el-descriptions-item>
          <el-descriptions-item label="紧急电话">{{ archive.emergencyPhone || '—' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else-if="!archiveLoading" description="暂无档案信息" />
      </div>
      <template #footer>
        <el-button type="primary" @click="archiveVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete, Folder, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import { trainingStatusMap } from '@/utils/dict'
import { exportCsv } from '@/utils/export'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  role: '',
  status: ''
})

const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const blankForm = () => ({
  username: '',
  realName: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 1,
  role: 'STUDENT'
})
const form = reactive(blankForm())

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const archiveVisible = ref(false)
const archiveLoading = ref(false)
const archive = ref(null)
const trainingTag = computed(() => (archive.value && trainingStatusMap[archive.value.trainingStatus]) || null)
const genderText = (g) => (Number(g) === 2 ? '女' : Number(g) === 1 ? '男' : '—')

async function load() {
  loading.value = true
  try {
    const res = await userApi.adminList({ ...query })
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
  query.role = ''
  query.status = ''
  query.pageNum = 1
  load()
}

function handleExport() {
  if (!list.value.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  const columns = [
    { label: '账号', prop: 'username' },
    { label: '姓名', prop: 'realName' },
    { label: '手机', prop: 'phone' },
    { label: '邮箱', prop: 'email' },
    { label: '角色', prop: 'role', formatter: (v) => (v === 'ADMIN' ? '管理员' : '学员') },
    { label: '状态', prop: 'status', formatter: (v) => (Number(v) === 1 ? '启用' : '禁用') },
    { label: '最后登录', prop: 'lastLoginTime' }
  ]
  exportCsv('用户列表', columns, list.value)
}

async function onToggleStatus(row, val) {
  row._statusLoading = true
  try {
    await userApi.adminSetStatus(row.id, val)
    row.status = val
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } catch (e) {
    /* 失败时不变更本地状态，switch 自动回退 */
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
  Object.assign(form, {
    username: row.username,
    realName: row.realName,
    nickname: row.nickname,
    phone: row.phone,
    email: row.email,
    gender: Number(row.gender) || 1,
    role: row.role || 'STUDENT'
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
        await userApi.adminUpdate(editingId.value, payload)
        ElMessage.success('修改成功')
      } else {
        await userApi.adminCreate(payload)
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
  ElMessageBox.confirm(`确定删除用户「${row.realName || row.username}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await userApi.adminDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    load()
  }).catch(() => {})
}

async function openArchive(row) {
  archiveVisible.value = true
  archiveLoading.value = true
  archive.value = null
  try {
    const res = await userApi.adminStudentDetail(row.id)
    archive.value = res.data || null
  } finally {
    archiveLoading.value = false
  }
}

onMounted(load)
</script>
