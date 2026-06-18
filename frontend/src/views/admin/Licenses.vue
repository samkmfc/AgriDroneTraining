<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>执照管理与临期预警</div>
        <div class="page-subtitle">登记学员无人机操作执照，动态监控有效期并预警临期续期</div>
      </div>
      <el-button v-if="activeTab === 'list'" type="primary" :icon="Plus" @click="openCreate">新增执照</el-button>
    </div>

    <el-tabs v-model="activeTab" class="lic-tabs">
      <!-- ============ 执照列表 ============ -->
      <el-tab-pane label="执照列表" name="list">
        <div class="toolbar">
          <el-input
            v-model="query.keyword"
            placeholder="执照号 / 机型"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="临期预警" value="EXPIRING" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <div class="spacer"></div>
          <el-button :icon="Download" @click="handleExport">导出CSV</el-button>
        </div>

        <div class="table-card">
          <el-table v-loading="loading" :data="list" stripe>
            <el-table-column prop="realName" label="持证人" min-width="100">
              <template #default="{ row }">{{ row.realName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="licenseNo" label="执照编号" min-width="160" />
            <el-table-column prop="licenseType" label="执照类型" min-width="130" />
            <el-table-column prop="droneModel" label="适用机型" min-width="120" />
            <el-table-column prop="issuingAuthority" label="发证机关" min-width="150" show-overflow-tooltip />
            <el-table-column prop="issueDate" label="发证日期" width="120" />
            <el-table-column prop="expiryDate" label="有效期至" width="120" />
            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="statusMeta(row).type" effect="light">{{ statusMeta(row).text }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="210" fixed="right">
              <template #default="{ row }">
                <el-button link type="success" :icon="RefreshRight" @click="openRenew(row)">续期</el-button>
                <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
            <template #empty><el-empty description="暂无执照记录" /></template>
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
      </el-tab-pane>

      <!-- ============ 临期预警 ============ -->
      <el-tab-pane name="expiring">
        <template #label>
          临期预警
          <el-badge
            v-if="expiringList.length"
            :value="expiringList.length"
            :max="99"
            class="warn-badge"
          />
        </template>

        <div v-loading="expiringLoading">
          <el-alert
            :title="`共 ${expiringList.length} 张执照临期或已过期，请及时提醒学员续期`"
            type="warning"
            show-icon
            :closable="false"
            style="margin-bottom: 18px; border-radius: 12px"
          />

          <el-row v-if="expiringList.length" :gutter="18">
            <el-col
              v-for="lic in expiringList"
              :key="lic.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              style="margin-bottom: 18px"
            >
              <div class="warn-card" :class="lic.status === 'EXPIRED' ? 'is-expired' : 'is-expiring'">
                <div class="warn-card-head">
                  <el-icon :size="20">
                    <CircleCloseFilled v-if="lic.status === 'EXPIRED'" />
                    <WarningFilled v-else />
                  </el-icon>
                  <span class="warn-name">{{ lic.realName || '—' }}</span>
                  <el-tag
                    :type="lic.status === 'EXPIRED' ? 'danger' : 'warning'"
                    effect="dark"
                    size="small"
                  >{{ lic.status === 'EXPIRED' ? '已过期' : '临期' }}</el-tag>
                </div>
                <div class="warn-no">{{ lic.licenseNo || '—' }}</div>
                <div class="warn-meta">
                  <span>{{ lic.droneModel || '—' }}</span>
                  <span>有效期至 {{ lic.expiryDate || '—' }}</span>
                </div>
                <div class="warn-days" :class="lic.status === 'EXPIRED' ? 'days-red' : 'days-orange'">
                  {{ lic.status === 'EXPIRED' ? '已过期' : `剩余 ${lic.remainingDays} 天` }}
                </div>
                <el-button
                  type="success"
                  :icon="RefreshRight"
                  size="small"
                  style="width: 100%; margin-top: 12px"
                  :loading="lic._renewing"
                  @click="quickRenew(lic)"
                >一键续期（24个月）</el-button>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else-if="!expiringLoading" description="暂无临期或过期执照，状态良好" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑执照' : '新增执照'"
      width="560px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="持证学员" prop="userId">
          <el-select
            v-model="form.userId"
            placeholder="请选择持证学员"
            filterable
            style="width: 100%"
            :loading="studentLoading"
          >
            <el-option
              v-for="s in students"
              :key="s.id"
              :label="`${s.realName || s.username}（${s.username}）`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="执照编号" prop="licenseNo">
          <el-input v-model="form.licenseNo" placeholder="请输入执照编号" />
        </el-form-item>
        <el-form-item label="执照类型" prop="licenseType">
          <el-input v-model="form.licenseType" placeholder="如：多旋翼视距内驾驶员" />
        </el-form-item>
        <el-form-item label="适用机型" prop="droneModel">
          <el-input v-model="form.droneModel" placeholder="如：多旋翼 / 植保无人机" />
        </el-form-item>
        <el-form-item label="发证机关" prop="issuingAuthority">
          <el-input v-model="form.issuingAuthority" placeholder="如：民航局 AOPA" />
        </el-form-item>
        <el-form-item label="发证日期" prop="issueDate">
          <el-date-picker
            v-model="form.issueDate"
            type="date"
            placeholder="选择发证日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="有效期至" prop="expiryDate">
          <el-date-picker
            v-model="form.expiryDate"
            type="date"
            placeholder="选择到期日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 续期 -->
    <el-dialog v-model="renewVisible" title="执照续期" width="420px" @closed="renewTarget = null">
      <div v-if="renewTarget" class="renew-summary">
        <span><b>持证人：</b>{{ renewTarget.realName || '—' }}</span>
        <span><b>执照编号：</b>{{ renewTarget.licenseNo || '—' }}</span>
        <span><b>当前有效期至：</b>{{ renewTarget.expiryDate || '—' }}</span>
      </div>
      <el-form label-width="90px" style="margin-top: 10px">
        <el-form-item label="续期时长">
          <el-select v-model="renewMonths" style="width: 100%">
            <el-option label="12 个月" :value="12" />
            <el-option label="24 个月" :value="24" />
            <el-option label="36 个月" :value="36" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewVisible = false">取消</el-button>
        <el-button type="success" :loading="renewing" @click="handleRenew">确认续期</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete, Download, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { licenseApi, userApi } from '@/api'
import { licenseStatusMap } from '@/utils/dict'
import { exportCsv } from '@/utils/export'

const activeTab = ref('list')

/* ---- 执照列表 ---- */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const statusMeta = (row) => licenseStatusMap[row.status] || { text: '未知', type: 'info' }

async function load() {
  loading.value = true
  try {
    const res = await licenseApi.adminList({ ...query })
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
  query.status = ''
  query.pageNum = 1
  load()
}

/* ---- 临期预警 ---- */
const expiringLoading = ref(false)
const expiringList = ref([])
async function loadExpiring() {
  expiringLoading.value = true
  try {
    const res = await licenseApi.expiring()
    expiringList.value = res.data || []
  } finally {
    expiringLoading.value = false
  }
}

/* ---- 学员下拉 ---- */
const students = ref([])
const studentLoading = ref(false)
async function loadStudents() {
  if (students.value.length) return
  studentLoading.value = true
  try {
    const res = await userApi.adminList({ role: 'STUDENT', pageNum: 1, pageSize: 1000 })
    students.value = res.data?.records || []
  } finally {
    studentLoading.value = false
  }
}

/* ---- 新增 / 编辑 ---- */
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref()

const blankForm = () => ({
  userId: null,
  licenseNo: '',
  licenseType: '',
  droneModel: '',
  issuingAuthority: '',
  issueDate: '',
  expiryDate: ''
})
const form = reactive(blankForm())

const rules = {
  userId: [{ required: true, message: '请选择持证学员', trigger: 'change' }],
  licenseNo: [{ required: true, message: '请输入执照编号', trigger: 'blur' }],
  licenseType: [{ required: true, message: '请输入执照类型', trigger: 'blur' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, blankForm())
  editingId.value = null
  formRef.value?.clearValidate()
}
function openCreate() {
  resetForm()
  loadStudents()
  dialogVisible.value = true
}
function openEdit(row) {
  editingId.value = row.id
  loadStudents()
  Object.assign(form, {
    userId: row.userId,
    licenseNo: row.licenseNo,
    licenseType: row.licenseType,
    droneModel: row.droneModel,
    issuingAuthority: row.issuingAuthority,
    issueDate: row.issueDate,
    expiryDate: row.expiryDate
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate(async (ok) => {
    if (!ok) return
    submitting.value = true
    try {
      if (editingId.value) {
        await licenseApi.adminUpdate(editingId.value, { ...form })
        ElMessage.success('修改成功')
      } else {
        await licenseApi.adminCreate({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      load()
      if (activeTab.value === 'expiring') loadExpiring()
    } finally {
      submitting.value = false
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除执照「${row.licenseNo}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await licenseApi.adminDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    load()
  }).catch(() => {})
}

/* ---- 续期 ---- */
const renewVisible = ref(false)
const renewTarget = ref(null)
const renewMonths = ref(24)
const renewing = ref(false)

function openRenew(row) {
  renewTarget.value = row
  renewMonths.value = 24
  renewVisible.value = true
}

async function handleRenew() {
  if (!renewTarget.value) return
  renewing.value = true
  try {
    await licenseApi.renew(renewTarget.value.id, renewMonths.value)
    ElMessage.success(`续期成功（${renewMonths.value} 个月）`)
    renewVisible.value = false
    load()
    loadExpiring()
  } finally {
    renewing.value = false
  }
}

async function quickRenew(lic) {
  lic._renewing = true
  try {
    await licenseApi.renew(lic.id, 24)
    ElMessage.success('续期成功（24 个月）')
    loadExpiring()
    load()
  } finally {
    lic._renewing = false
  }
}

/* ---- 导出 ---- */
function handleExport() {
  if (!list.value.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  const columns = [
    { label: '持证人', prop: 'realName' },
    { label: '执照编号', prop: 'licenseNo' },
    { label: '执照类型', prop: 'licenseType' },
    { label: '适用机型', prop: 'droneModel' },
    { label: '发证机关', prop: 'issuingAuthority' },
    { label: '发证日期', prop: 'issueDate' },
    { label: '有效期至', prop: 'expiryDate' },
    { label: '状态', prop: 'status', formatter: (v) => licenseStatusMap[v]?.text || v }
  ]
  exportCsv('执照列表', columns, list.value)
}

watch(activeTab, (tab) => {
  if (tab === 'expiring') loadExpiring()
})

onMounted(() => {
  load()
  loadExpiring()
})
</script>

<style scoped lang="scss">
.lic-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 600; }
.warn-badge { margin-left: 6px; }

.renew-summary {
  display: flex; flex-direction: column; gap: 6px;
  background: #f5f9f7; border-radius: 10px; padding: 14px 16px;
  font-size: 13px; color: #44574e;
  b { color: #1d2b24; }
}

.warn-card {
  position: relative;
  background: #fff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  padding: 18px;
  height: 100%;
  border-left: 4px solid transparent;
  transition: all .25s ease;
  &:hover { transform: translateY(-3px); box-shadow: var(--card-shadow-hover); }
  &.is-expiring { border-left-color: #f0a020; .warn-card-head .el-icon { color: #f0a020; } }
  &.is-expired { border-left-color: #e8504f; .warn-card-head .el-icon { color: #e8504f; } }
}
.warn-card-head {
  display: flex; align-items: center; gap: 8px; margin-bottom: 10px;
  .warn-name { flex: 1; font-size: 15px; font-weight: 700; color: #1d2b24; }
}
.warn-no {
  font-size: 16px; font-weight: 700; color: #2c3e36;
  letter-spacing: 1px; margin-bottom: 8px;
}
.warn-meta {
  display: flex; flex-direction: column; gap: 4px;
  font-size: 12px; color: #8a9b92; margin-bottom: 12px;
}
.warn-days {
  font-size: 15px; font-weight: 700;
  padding: 8px 12px; border-radius: 10px; text-align: center;
  &.days-orange { background: #fdf4e6; color: #d98509; }
  &.days-red { background: #fdecec; color: #e8504f; }
}
</style>
