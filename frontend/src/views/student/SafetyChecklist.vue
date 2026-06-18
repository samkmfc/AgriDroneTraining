<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>飞前安全检查</div>
        <div class="page-subtitle">每次作业前必须完成飞前安全检查并全部通过，方可填报飞行作业日志</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="sc-tabs">
      <!-- 新建检查单 -->
      <el-tab-pane name="new">
        <template #label><span class="tab-label"><el-icon><CircleCheck /></el-icon> 新建检查单</span></template>

        <el-alert
          type="error"
          show-icon
          :closable="false"
          title="强制填报 · 安全第一"
          description="飞前安全检查是无人机植保作业的强制环节。请如实逐项核查机身、电池、空域与天气状况，全部通过后方可作业。"
          style="margin-bottom: 18px; border-radius: 12px"
        />

        <el-row :gutter="18">
          <el-col :lg="14" :md="24">
            <div class="app-card form-card">
              <div class="section-label"><span class="bar"></span>作业基础信息</div>
              <el-form :model="form" label-width="92px" label-position="top">
                <el-row :gutter="16">
                  <el-col :sm="12">
                    <el-form-item label="作业地点">
                      <el-input v-model="form.location" placeholder="如：XX镇XX村稻田" :prefix-icon="Location" />
                    </el-form-item>
                  </el-col>
                  <el-col :sm="12">
                    <el-form-item label="天气">
                      <el-select v-model="form.weather" placeholder="选择天气" style="width: 100%">
                        <el-option v-for="w in weatherOptions" :key="w" :label="w" :value="w" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :sm="12">
                    <el-form-item label="风速">
                      <el-input v-model="form.windSpeed" placeholder="如：2级 / 3.5m/s" />
                    </el-form-item>
                  </el-col>
                  <el-col :sm="12">
                    <el-form-item label="备注">
                      <el-input v-model="form.remark" placeholder="其他需要说明的情况" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>

              <div class="section-label" style="margin-top: 8px"><span class="bar"></span>安全检查项</div>
              <div class="check-grid">
                <div
                  v-for="item in checkItems"
                  :key="item.key"
                  class="check-item"
                  :class="{ ok: form[item.key] === 1 }"
                >
                  <div class="ci-icon"><el-icon><component :is="item.icon" /></el-icon></div>
                  <div class="ci-text">
                    <div class="ci-title">{{ item.label }}</div>
                    <div class="ci-status">{{ form[item.key] === 1 ? '已确认通过' : '待确认' }}</div>
                  </div>
                  <el-switch
                    v-model="form[item.key]"
                    :active-value="1"
                    :inactive-value="0"
                    active-color="#15a06b"
                  />
                </div>
              </div>
            </div>
          </el-col>

          <el-col :lg="10" :md="24">
            <div class="app-card result-panel" :class="passed ? 'pass' : 'fail'">
              <div class="rp-icon">
                <el-icon :size="46"><component :is="passed ? 'SuccessFilled' : 'WarningFilled'" /></el-icon>
              </div>
              <div class="rp-title">{{ passed ? '检查通过' : '存在未通过项' }}</div>
              <div class="rp-count">{{ checkedCount }} / {{ checkItems.length }} 项已确认</div>
              <el-progress
                :percentage="Math.round((checkedCount / checkItems.length) * 100)"
                :stroke-width="12"
                :color="passed ? '#15a06b' : '#f0a020'"
                :show-text="false"
                style="margin: 14px 0"
              />
              <div class="rp-tip">
                {{ passed
                  ? '所有检查项均已通过，可安全开展作业并填报飞行日志。'
                  : '存在未通过的检查项，请完成全部检查后再作业。' }}
              </div>
              <el-button
                type="primary"
                size="large"
                :icon="Promotion"
                :loading="submitting"
                style="width: 100%; margin-top: 18px"
                @click="onSubmit"
              >
                提交检查单
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 历史记录 -->
      <el-tab-pane name="history">
        <template #label><span class="tab-label"><el-icon><Tickets /></el-icon> 历史记录</span></template>

        <div class="table-card">
          <el-table :data="history" v-loading="hisLoading" stripe>
            <el-table-column prop="checkTime" label="检查时间" min-width="170" />
            <el-table-column prop="location" label="作业地点" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">{{ row.location || '—' }}</template>
            </el-table-column>
            <el-table-column prop="weather" label="天气" width="90" align="center">
              <template #default="{ row }">{{ row.weather || '—' }}</template>
            </el-table-column>
            <el-table-column prop="windSpeed" label="风速" width="100" align="center">
              <template #default="{ row }">{{ row.windSpeed || '—' }}</template>
            </el-table-column>
            <el-table-column label="结论" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="isPassed(row) ? 'success' : 'danger'" effect="dark" size="small">
                  {{ isPassed(row) ? '通过' : '未通过' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="openDetail(row)">明细</el-button>
              </template>
            </el-table-column>
            <template #empty><el-empty description="暂无检查记录" /></template>
          </el-table>

          <div v-if="hisTotal > 0" class="pagination-wrap">
            <el-pagination
              layout="total, prev, pager, next, jumper"
              :total="hisTotal"
              :current-page="hisQuery.pageNum"
              :page-size="hisQuery.pageSize"
              @current-change="(p) => { hisQuery.pageNum = p; loadHistory() }"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 明细弹窗 -->
    <el-dialog v-model="detailVisible" title="飞前安全检查明细" width="560px">
      <div v-if="current" class="detail-body">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="检查时间">{{ current.checkTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="结论">
            <el-tag :type="isPassed(current) ? 'success' : 'danger'" size="small">
              {{ isPassed(current) ? '通过' : '未通过' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="作业地点">{{ current.location || '—' }}</el-descriptions-item>
          <el-descriptions-item label="天气">{{ current.weather || '—' }}</el-descriptions-item>
          <el-descriptions-item label="风速">{{ current.windSpeed || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ current.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div class="section-label" style="margin: 16px 0 10px"><span class="bar"></span>检查项明细</div>
        <div class="detail-grid">
          <div v-for="item in checkItems" :key="item.key" class="dg-item">
            <span class="dg-label">{{ item.label }}</span>
            <el-tag :type="current[item.key] === 1 ? 'success' : 'danger'" size="small" effect="light">
              {{ current[item.key] === 1 ? '通过' : '未通过' }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Promotion, View } from '@element-plus/icons-vue'
import { checklistApi } from '@/api'

const activeTab = ref('new')
const weatherOptions = ['晴', '多云', '阴', '小雨']

const checkItems = [
  { key: 'batteryCheck', label: '电池电量充足', icon: 'Lightning' },
  { key: 'propellerCheck', label: '螺旋桨完好', icon: 'Refresh' },
  { key: 'nozzleCheck', label: '喷头无堵塞', icon: 'Drizzling' },
  { key: 'weatherCheck', label: '天气适航', icon: 'Sunny' },
  { key: 'airspaceCheck', label: '空域合规非禁飞区', icon: 'Location' },
  { key: 'bodyCheck', label: '机身桨叶完整', icon: 'Cpu' },
  { key: 'signalCheck', label: '信号 / GPS 正常', icon: 'Position' }
]

function emptyForm() {
  const f = { location: '', weather: '', windSpeed: '', remark: '' }
  checkItems.forEach((i) => (f[i.key] = 0))
  return f
}
const form = reactive(emptyForm())

const checkedCount = computed(() => checkItems.filter((i) => form[i.key] === 1).length)
const passed = computed(() => checkedCount.value === checkItems.length)

const submitting = ref(false)
async function onSubmit() {
  if (!form.location) return ElMessage.warning('请填写作业地点')
  if (!form.weather) return ElMessage.warning('请选择天气')
  submitting.value = true
  try {
    await checklistApi.submit({ ...form })
    ElMessage.success(passed.value ? '检查单已提交，检查通过' : '检查单已提交（存在未通过项）')
    Object.assign(form, emptyForm())
    activeTab.value = 'history'
    loadHistory()
  } finally {
    submitting.value = false
  }
}

/* ---- 历史记录 ---- */
const history = ref([])
const hisTotal = ref(0)
const hisLoading = ref(false)
const hisQuery = reactive({ pageNum: 1, pageSize: 10 })

function isPassed(row) {
  return checkItems.every((i) => row[i.key] === 1)
}

async function loadHistory() {
  hisLoading.value = true
  try {
    const res = await checklistApi.my({ ...hisQuery })
    history.value = res.data?.records || []
    hisTotal.value = res.data?.total || 0
  } finally {
    hisLoading.value = false
  }
}

const detailVisible = ref(false)
const current = ref(null)
function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

onMounted(loadHistory)
</script>

<style scoped lang="scss">
.sc-tabs :deep(.el-tabs__item) { font-size: 15px; }
.tab-label { display: inline-flex; align-items: center; gap: 6px; }

.section-label {
  font-weight: 700; color: #1d2b24; display: flex; align-items: center; gap: 8px; margin-bottom: 14px;
  .bar { width: 4px; height: 15px; background: var(--brand-gradient); border-radius: 3px; }
}

.form-card { min-height: 100%; }

.check-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.check-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; border-radius: 12px; border: 1px solid #eaf1ed; background: #fafdfb;
  transition: all .2s ease;
  &.ok { border-color: #b2e1cb; background: var(--brand-primary-light); }
}
.ci-icon {
  width: 38px; height: 38px; border-radius: 10px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: #eef4f1; color: #9bb0a5; font-size: 19px; transition: all .2s;
}
.check-item.ok .ci-icon { background: var(--brand-gradient); color: #fff; }
.ci-text { flex: 1; min-width: 0; }
.ci-title { font-size: 14px; font-weight: 600; color: #2c3e36; }
.ci-status { font-size: 12px; color: #9bb0a5; margin-top: 2px; }
.check-item.ok .ci-status { color: var(--brand-primary); }

.result-panel {
  text-align: center; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border: 1px solid #eaf1ed;
  &.pass { background: linear-gradient(160deg, #e6f7ef 0%, #f4fbf8 100%); }
  &.fail { background: linear-gradient(160deg, #fff6e8 0%, #fffaf2 100%); }
}
.result-panel .rp-icon { line-height: 1; }
.result-panel.pass .rp-icon { color: var(--brand-primary); }
.result-panel.fail .rp-icon { color: #f0a020; }
.rp-title { font-size: 22px; font-weight: 700; color: #1d2b24; margin-top: 12px; }
.rp-count { font-size: 14px; color: #6b7d73; margin-top: 6px; }
.rp-tip { font-size: 13px; color: #6b7d73; line-height: 1.7; padding: 0 8px; }

.detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.dg-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px; border-radius: 8px; background: #f7faf8;
}
.dg-label { font-size: 13px; color: #4a5e54; }
</style>
