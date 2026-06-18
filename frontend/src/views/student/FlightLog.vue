<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>飞行作业日志</div>
        <div class="page-subtitle">如实填报每次植保作业，日志将经培训机构人工审核以防伪造</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="fl-tabs">
      <!-- 填报作业日志 -->
      <el-tab-pane name="new">
        <template #label><span class="tab-label"><el-icon><EditPen /></el-icon> 填报作业日志</span></template>

        <el-alert
          type="warning"
          show-icon
          :closable="false"
          title="填报前提"
          description="作业日志需先完成并通过飞前安全检查。请先在“飞前安全检查”中提交检查单，并在下方选择对应的已通过检查单。"
          style="margin-bottom: 18px; border-radius: 12px"
        />

        <div class="app-card">
          <el-form :model="form" label-width="100px" label-position="top">
            <el-row :gutter="18">
              <el-col :sm="24">
                <el-form-item label="关联飞前安全检查">
                  <el-select
                    v-model="form.checklistId"
                    placeholder="选择已通过的飞前安全检查单"
                    style="width: 100%"
                    :no-data-text="checklistOptions.length ? '暂无数据' : '暂无已通过的检查单，请先完成飞前安全检查'"
                  >
                    <el-option
                      v-for="c in checklistOptions"
                      :key="c.id"
                      :label="`${c.checkTime || ''}  ${c.location || ''}`"
                      :value="c.id"
                    />
                  </el-select>
                  <div v-if="!checklistOptions.length" class="hint warn">
                    暂无已通过的飞前安全检查单，请先前往“飞前安全检查”完成检查。
                  </div>
                </el-form-item>
              </el-col>

              <el-col :sm="8"><el-form-item label="机型"><el-input v-model="form.droneModel" placeholder="如：T40 / P100" /></el-form-item></el-col>
              <el-col :sm="8"><el-form-item label="作业地点"><el-input v-model="form.location" placeholder="作业地块" /></el-form-item></el-col>
              <el-col :sm="8"><el-form-item label="作业作物"><el-input v-model="form.cropType" placeholder="如：水稻" /></el-form-item></el-col>

              <el-col :sm="8">
                <el-form-item label="作业面积 (亩)">
                  <el-input-number v-model="form.area" :min="0" :precision="1" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :sm="8">
                <el-form-item label="飞行时长 (分钟)">
                  <el-input-number v-model="form.flightDuration" :min="0" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :sm="8">
                <el-form-item label="天气">
                  <el-select v-model="form.weather" placeholder="选择天气" style="width: 100%">
                    <el-option v-for="w in weatherOptions" :key="w" :label="w" :value="w" />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :sm="8"><el-form-item label="使用药剂"><el-input v-model="form.pesticideUsed" placeholder="药剂名称 / 配比" /></el-form-item></el-col>
              <el-col :sm="8">
                <el-form-item label="开始时间">
                  <el-date-picker v-model="form.startTime" type="datetime" placeholder="作业开始时间" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :sm="8">
                <el-form-item label="结束时间">
                  <el-date-picker v-model="form.endTime" type="datetime" placeholder="作业结束时间" style="width: 100%" />
                </el-form-item>
              </el-col>

              <el-col :sm="16">
                <el-form-item label="作业描述">
                  <el-input v-model="form.description" type="textarea" :rows="4" placeholder="作业过程、地块情况、异常说明等" />
                </el-form-item>
              </el-col>
              <el-col :sm="8">
                <el-form-item label="作业照片">
                  <image-upload v-model="imageUrl" />
                </el-form-item>
              </el-col>
            </el-row>

            <div style="text-align: right">
              <el-button @click="resetForm">重置</el-button>
              <el-button type="primary" :icon="Promotion" :loading="submitting" @click="onSubmit">提交日志</el-button>
            </div>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 我的日志 -->
      <el-tab-pane name="list">
        <template #label><span class="tab-label"><el-icon><Notebook /></el-icon> 我的日志</span></template>

        <div class="toolbar">
          <el-select v-model="query.auditStatus" placeholder="全部审核状态" clearable style="width: 160px" @change="onSearch">
            <el-option v-for="(v, k) in auditStatusMap" :key="k" :label="v.text" :value="k" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
        </div>

        <div class="table-card">
          <el-table :data="list" v-loading="loading" stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="expand-box">
                  <el-descriptions :column="3" border size="small">
                    <el-descriptions-item label="作物">{{ row.cropType || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="面积(亩)">{{ row.area ?? '—' }}</el-descriptions-item>
                    <el-descriptions-item label="时长(分)">{{ row.flightDuration ?? '—' }}</el-descriptions-item>
                    <el-descriptions-item label="开始时间">{{ row.startTime || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="提交时间">{{ row.createTime || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="审核时间">{{ row.auditTime || '—' }}</el-descriptions-item>
                  </el-descriptions>
                  <el-alert
                    v-if="row.auditStatus === 'REJECTED'"
                    type="error"
                    :closable="false"
                    show-icon
                    style="margin-top: 10px; border-radius: 8px"
                    :title="`驳回原因：${row.auditReason || '未填写'}`"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="droneModel" label="机型" width="110" show-overflow-tooltip>
              <template #default="{ row }">{{ row.droneModel || '—' }}</template>
            </el-table-column>
            <el-table-column prop="location" label="作业地点" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">{{ row.location || '—' }}</template>
            </el-table-column>
            <el-table-column prop="cropType" label="作物" width="100" show-overflow-tooltip>
              <template #default="{ row }">{{ row.cropType || '—' }}</template>
            </el-table-column>
            <el-table-column label="面积(亩)" width="100" align="center">
              <template #default="{ row }">{{ row.area ?? '—' }}</template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.startTime || row.createTime || '—' }}</template>
            </el-table-column>
            <el-table-column label="审核状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="auditStatusMap[row.auditStatus]?.type || 'info'" effect="light" size="small">
                  {{ auditStatusMap[row.auditStatus]?.text || row.auditStatus || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核意见" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.auditStatus === 'REJECTED'" class="reject-reason">{{ row.auditReason || '未填写' }}</span>
                <span v-else style="color: #9bb0a5">—</span>
              </template>
            </el-table-column>
            <template #empty><el-empty description="暂无作业日志" /></template>
          </el-table>

          <div v-if="total > 0" class="pagination-wrap">
            <el-pagination
              layout="total, prev, pager, next, jumper"
              :total="total"
              :current-page="query.pageNum"
              :page-size="query.pageSize"
              @current-change="(p) => { query.pageNum = p; load() }"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Promotion } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { flightLogApi, checklistApi } from '@/api'
import { auditStatusMap } from '@/utils/dict'

const activeTab = ref('new')
const weatherOptions = ['晴', '多云', '阴', '小雨']

/* ---- 表单 ---- */
function emptyForm() {
  return {
    checklistId: undefined,
    droneModel: '',
    location: '',
    cropType: '',
    area: undefined,
    pesticideUsed: '',
    weather: '',
    flightDuration: undefined,
    startTime: '',
    endTime: '',
    description: ''
  }
}
const form = reactive(emptyForm())
const imageUrl = ref('')

const checklistOptions = ref([])
function isPassed(row) {
  return ['batteryCheck', 'propellerCheck', 'nozzleCheck', 'weatherCheck', 'airspaceCheck', 'bodyCheck', 'signalCheck']
    .every((k) => row[k] === 1)
}
async function loadChecklists() {
  const res = await checklistApi.my({ pageNum: 1, pageSize: 50 })
  checklistOptions.value = (res.data?.records || []).filter(isPassed)
}

const submitting = ref(false)
async function onSubmit() {
  if (!form.checklistId) return ElMessage.warning('请先选择已通过的飞前安全检查单')
  if (!form.droneModel) return ElMessage.warning('请填写机型')
  if (!form.location) return ElMessage.warning('请填写作业地点')
  submitting.value = true
  try {
    const payload = {
      ...form,
      startTime: form.startTime ? new Date(form.startTime).toISOString() : null,
      endTime: form.endTime ? new Date(form.endTime).toISOString() : null,
      images: imageUrl.value ? JSON.stringify([imageUrl.value]) : ''
    }
    await flightLogApi.submit(payload)
    ElMessage.success('作业日志已提交，等待人工审核')
    resetForm()
    activeTab.value = 'list'
    onSearch()
  } finally {
    submitting.value = false
  }
}
function resetForm() {
  Object.assign(form, emptyForm())
  imageUrl.value = ''
}

/* ---- 列表 ---- */
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, auditStatus: '' })

async function load() {
  loading.value = true
  try {
    const res = await flightLogApi.my({ ...query })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNum = 1
  load()
}

onMounted(() => {
  loadChecklists()
  load()
})
</script>

<style scoped lang="scss">
.fl-tabs :deep(.el-tabs__item) { font-size: 15px; }
.tab-label { display: inline-flex; align-items: center; gap: 6px; }

.hint { font-size: 12px; margin-top: 4px; }
.hint.warn { color: #f0a020; }

.expand-box { padding: 8px 16px 4px; }
.reject-reason { color: #e8504f; font-weight: 600; }
</style>
