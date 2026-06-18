<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>作业日志审核</div>
        <div class="page-subtitle">人工审核与防伪驳回 · 飞行作业日志真实性核验</div>
      </div>
      <el-button :icon="Refresh" circle @click="loadList" />
    </div>

    <div class="toolbar">
      <el-radio-group v-model="query.auditStatus" @change="handleSearch">
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="APPROVED">审核通过</el-radio-button>
        <el-radio-button label="REJECTED">已驳回</el-radio-button>
        <el-radio-button label="">全部</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="query.keyword"
        placeholder="搜索地点 / 机型 / 作物"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="success" :icon="Select" :disabled="!selection.length" @click="handleBatchApprove">批量通过</el-button>
      <el-button type="danger" :icon="CloseBold" :disabled="!selection.length" @click="handleBatchReject">批量驳回</el-button>
      <el-button :icon="Download" @click="handleExport">导出CSV</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="46" align="center" :selectable="(row) => row.auditStatus === 'PENDING'" />
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="realName" label="学员" width="110" show-overflow-tooltip />
        <el-table-column prop="droneModel" label="机型" width="130" show-overflow-tooltip />
        <el-table-column prop="location" label="作业地点" min-width="160" show-overflow-tooltip />
        <el-table-column prop="cropType" label="作物" width="110" show-overflow-tooltip />
        <el-table-column label="面积(亩)" width="100" align="center">
          <template #default="{ row }">{{ row.area }}</template>
        </el-table-column>
        <el-table-column label="时长(分)" width="100" align="center">
          <template #default="{ row }">{{ row.flightDuration }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" />
        <el-table-column label="审核状态" width="160" align="center">
          <template #default="{ row }">
            <el-tag :type="auditStatusMap[row.auditStatus]?.type || 'info'" effect="light">
              {{ auditStatusMap[row.auditStatus]?.text || row.auditStatus }}
            </el-tag>
            <div v-if="row.auditStatus === 'REJECTED' && row.auditReason" class="reject-reason" :title="row.auditReason">
              {{ row.auditReason }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openDetail(row)">查看详情</el-button>
            <el-button type="success" link :icon="Stamp" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无作业日志" /></template>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="作业日志详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学员">{{ detail.realName }}</el-descriptions-item>
        <el-descriptions-item label="无人机机型">{{ detail.droneModel }}</el-descriptions-item>
        <el-descriptions-item label="作业地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="作物类型">{{ detail.cropType }}</el-descriptions-item>
        <el-descriptions-item label="作业面积">{{ detail.area }} 亩</el-descriptions-item>
        <el-descriptions-item label="使用农药">{{ detail.pesticideUsed || '-' }}</el-descriptions-item>
        <el-descriptions-item label="天气">{{ detail.weather || '-' }}</el-descriptions-item>
        <el-descriptions-item label="飞行时长">{{ detail.flightDuration }} 分钟</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="auditStatusMap[detail.auditStatus]?.type || 'info'" effect="light">
            {{ auditStatusMap[detail.auditStatus]?.text || detail.auditStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="作业描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.auditStatus === 'REJECTED'" label="驳回原因" :span="2">
          <span class="reason-text">{{ detail.auditReason || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="detail-images">
        <div class="img-label">现场照片</div>
        <div v-if="detailImages.length" class="img-grid">
          <el-image
            v-for="(img, i) in detailImages"
            :key="i"
            :src="img"
            :preview-src-list="detailImages"
            :initial-index="i"
            preview-teleported
            fit="cover"
            class="log-img"
          />
        </div>
        <el-empty v-else description="无现场照片" :image-size="60" />
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" :icon="Stamp" @click="auditFromDetail">前往审核</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="作业日志审核" width="560px" :close-on-click-modal="false" @closed="resetAudit">
      <div class="audit-summary">
        <span><b>学员：</b>{{ current.realName }}</span>
        <span><b>机型：</b>{{ current.droneModel }}</span>
        <span><b>地点：</b>{{ current.location }}</span>
        <span><b>作物：</b>{{ current.cropType }} · {{ current.area }}亩</span>
      </div>
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="90px">
        <el-form-item label="审核结论" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio-button label="APPROVED">通过</el-radio-button>
            <el-radio-button label="REJECTED">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          :label="auditForm.auditStatus === 'REJECTED' ? '驳回原因' : '审核意见'"
          prop="auditReason"
        >
          <el-input
            v-model="auditForm.auditReason"
            type="textarea"
            :rows="4"
            :placeholder="auditForm.auditStatus === 'REJECTED' ? '请填写驳回原因（必填）' : '审核意见（选填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button
          :type="auditForm.auditStatus === 'REJECTED' ? 'danger' : 'success'"
          :loading="submitting"
          @click="handleAudit"
        >
          {{ auditForm.auditStatus === 'REJECTED' ? '确认驳回' : '确认通过' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Refresh, Search, View, Stamp, Download, Select, CloseBold } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flightLogApi } from '@/api'
import { auditStatusMap, parseJSON } from '@/utils/dict'
import { exportCsv } from '@/utils/export'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, auditStatus: 'PENDING', keyword: '' })

const selection = ref([])
function onSelectionChange(rows) {
  selection.value = rows
}

const detailVisible = ref(false)
const detail = ref({})
const detailImages = computed(() => {
  const imgs = parseJSON(detail.value.images, [])
  return Array.isArray(imgs) ? imgs : []
})

const auditVisible = ref(false)
const current = ref({})
const auditFormRef = ref()
const auditForm = reactive({ auditStatus: 'APPROVED', auditReason: '' })

const auditRules = {
  auditStatus: [{ required: true, message: '请选择审核结论', trigger: 'change' }],
  auditReason: [{
    validator: (rule, value, callback) => {
      if (auditForm.auditStatus === 'REJECTED' && !value?.trim()) {
        callback(new Error('驳回时必须填写驳回原因'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await flightLogApi.adminList({
      pageNum: query.pageNum, pageSize: query.pageSize,
      auditStatus: query.auditStatus || undefined, keyword: query.keyword || undefined
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
  query.auditStatus = 'PENDING'
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

function openDetail(row) {
  detail.value = { ...row }
  detailVisible.value = true
}

function resetAudit() {
  auditForm.auditStatus = 'APPROVED'
  auditForm.auditReason = ''
  auditFormRef.value?.clearValidate()
}
function openAudit(row) {
  current.value = { ...row }
  resetAudit()
  auditVisible.value = true
}
function auditFromDetail() {
  detailVisible.value = false
  openAudit(detail.value)
}

async function handleAudit() {
  await auditFormRef.value.validate()
  submitting.value = true
  try {
    await flightLogApi.audit(current.value.id, {
      auditStatus: auditForm.auditStatus,
      auditReason: auditForm.auditReason?.trim() || ''
    })
    ElMessage.success(auditForm.auditStatus === 'REJECTED' ? '已驳回' : '审核通过')
    auditVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleBatch(auditStatus, auditReason) {
  const ids = selection.value.filter((r) => r.auditStatus === 'PENDING').map((r) => r.id)
  if (!ids.length) {
    ElMessage.warning('选中项中没有待审核的日志')
    return
  }
  await flightLogApi.batchAudit({ ids, auditStatus, auditReason: auditReason || '' })
  ElMessage.success(auditStatus === 'REJECTED' ? '已批量驳回' : '已批量通过')
  loadList()
}

function handleBatchApprove() {
  if (!selection.value.length) {
    ElMessage.warning('请先勾选要审核的日志')
    return
  }
  ElMessageBox.confirm(`确定将选中的 ${selection.value.length} 条日志批量通过吗？`, '批量通过', {
    type: 'warning',
    confirmButtonText: '确定通过',
    cancelButtonText: '取消'
  }).then(() => handleBatch('APPROVED')).catch(() => {})
}

function handleBatchReject() {
  if (!selection.value.length) {
    ElMessage.warning('请先勾选要审核的日志')
    return
  }
  ElMessageBox.prompt('请输入批量驳回原因', '批量驳回', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请填写驳回原因（必填）',
    inputValidator: (val) => (val && val.trim() ? true : '驳回原因不能为空')
  }).then(({ value }) => handleBatch('REJECTED', value.trim())).catch(() => {})
}

function handleExport() {
  if (!list.value.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  const columns = [
    { label: '学员', prop: 'realName' },
    { label: '机型', prop: 'droneModel' },
    { label: '作业地点', prop: 'location' },
    { label: '作物', prop: 'cropType' },
    { label: '面积(亩)', prop: 'area' },
    { label: '时长(分)', prop: 'flightDuration' },
    { label: '审核状态', prop: 'auditStatus', formatter: (v) => auditStatusMap[v]?.text || v },
    { label: '提交时间', prop: 'createTime' }
  ]
  exportCsv('作业日志', columns, list.value)
}

onMounted(loadList)

</script>

<style scoped lang="scss">
.reject-reason {
  margin-top: 4px; font-size: 12px; color: #e8504f; line-height: 1.3;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 140px;
}
.audit-summary {
  display: flex; flex-wrap: wrap; gap: 8px 24px;
  background: #f5f9f7; border-radius: 10px; padding: 14px 16px; margin-bottom: 18px;
  font-size: 13px; color: #44574e;
  b { color: #1d2b24; }
}
.detail-images { margin-top: 18px; }
.detail-images .img-label { font-weight: 600; color: #44574e; margin-bottom: 10px; }
.img-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.log-img { width: 96px; height: 96px; border-radius: 10px; }
.reason-text { color: #e8504f; }
</style>
