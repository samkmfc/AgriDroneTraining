<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>植保知识库</div>
        <div class="page-subtitle">作物识别图谱与农药配比参考，助力科学植保作业</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="knowledge-tabs">
      <!-- 作物识别图谱 -->
      <el-tab-pane name="crop">
        <template #label><span class="tab-label"><el-icon><Sunny /></el-icon> 作物识别图谱</span></template>

        <div class="toolbar">
          <el-input
            v-model="cropFilters.keyword"
            placeholder="搜索作物名称"
            clearable
            style="width: 240px"
            :prefix-icon="Search"
            @keyup.enter="loadCrops(true)"
            @clear="loadCrops(true)"
          />
          <el-input
            v-model="cropFilters.category"
            placeholder="作物类别 (如 粮食/经济作物)"
            clearable
            style="width: 220px"
            @keyup.enter="loadCrops(true)"
            @clear="loadCrops(true)"
          />
          <el-button type="primary" :icon="Search" @click="loadCrops(true)">查询</el-button>
        </div>

        <div v-loading="cropLoading">
          <el-row v-if="crops.length" :gutter="18">
            <el-col v-for="c in crops" :key="c.id" :xs="24" :sm="12" :md="6" style="margin-bottom: 18px">
              <div class="crop-card" @click="openCrop(c)">
                <div class="crop-cover">
                  <img v-if="c.image" :src="c.image" alt="crop" />
                  <div v-else class="cover-fallback bg-green"><el-icon :size="32"><Sunny /></el-icon></div>
                  <el-tag v-if="c.category" class="crop-tag" type="success" effect="dark" size="small">{{ c.category }}</el-tag>
                </div>
                <div class="crop-body">
                  <div class="crop-name">{{ c.cropName }}</div>
                  <div class="crop-line"><span class="lbl">生长周期</span>{{ c.growthCycle || '—' }}</div>
                  <div class="crop-line"><span class="lbl">常见病虫</span>
                    <span class="clamp">{{ c.commonPests || '—' }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else-if="!cropLoading" description="暂无作物数据" />
        </div>

        <div v-if="cropTotal > 0" class="pagination-wrap">
          <el-pagination
            layout="total, prev, pager, next, jumper"
            :total="cropTotal"
            :current-page="cropFilters.pageNum"
            :page-size="cropFilters.pageSize"
            @current-change="(p) => { cropFilters.pageNum = p; loadCrops() }"
          />
        </div>
      </el-tab-pane>

      <!-- 农药配比库 -->
      <el-tab-pane name="pesticide">
        <template #label><span class="tab-label"><el-icon><FirstAidKit /></el-icon> 农药配比库</span></template>

        <div class="toolbar">
          <el-input
            v-model="pestFilters.keyword"
            placeholder="搜索农药 / 病虫害"
            clearable
            style="width: 240px"
            :prefix-icon="Search"
            @keyup.enter="loadPesticides(true)"
            @clear="loadPesticides(true)"
          />
          <el-input
            v-model="pestFilters.cropName"
            placeholder="适用作物"
            clearable
            style="width: 200px"
            @keyup.enter="loadPesticides(true)"
            @clear="loadPesticides(true)"
          />
          <el-button type="primary" :icon="Search" @click="loadPesticides(true)">查询</el-button>
        </div>

        <div class="table-card">
          <el-table :data="pesticides" v-loading="pestLoading" stripe>
            <el-table-column prop="pesticideName" label="农药名称" min-width="130" show-overflow-tooltip />
            <el-table-column prop="cropName" label="适用作物" width="110" show-overflow-tooltip />
            <el-table-column prop="pestName" label="防治对象" min-width="120" show-overflow-tooltip />
            <el-table-column prop="dosage" label="用药剂量" min-width="120" show-overflow-tooltip />
            <el-table-column prop="waterRatio" label="兑水比例" width="110" show-overflow-tooltip />
            <el-table-column prop="sprayMethod" label="喷施方式" min-width="120" show-overflow-tooltip />
            <el-table-column prop="safetyInterval" label="安全间隔期" width="110" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="warning" effect="light">{{ row.safetyInterval || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="notes" label="注意事项" min-width="160" show-overflow-tooltip />
            <template #empty><el-empty description="暂无配比数据" /></template>
          </el-table>

          <div v-if="pestTotal > 0" class="pagination-wrap">
            <el-pagination
              layout="total, prev, pager, next, jumper"
              :total="pestTotal"
              :current-page="pestFilters.pageNum"
              :page-size="pestFilters.pageSize"
              @current-change="(p) => { pestFilters.pageNum = p; loadPesticides() }"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 配比计算器 -->
      <el-tab-pane name="calc">
        <template #label><span class="tab-label"><el-icon><Histogram /></el-icon> 配比计算器</span></template>

        <div class="calc-wrap" v-loading="calcLoading">
          <div class="app-card calc-input">
            <div class="chart-title"><span class="bar"></span>用药量估算</div>
            <el-form label-position="top" class="calc-form">
              <el-form-item label="选择农药配比方案">
                <el-select
                  v-model="calcPlanId"
                  filterable
                  clearable
                  placeholder="请选择农药 / 防治对象"
                  style="width: 100%"
                  @change="onPlanChange"
                >
                  <el-option
                    v-for="p in calcPlans"
                    :key="p.id"
                    :value="p.id"
                    :label="`${p.pesticideName}（防治${p.pestName || '—'}）`"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="作业面积（亩）">
                <el-input-number v-model="area" :min="0" :step="1" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-form>

            <div v-if="currentPlan" class="plan-meta">
              <div class="pm-line"><span class="lbl">亩用药量</span>{{ currentPlan.dosage || '—' }}</div>
              <div class="pm-line"><span class="lbl">亩喷液量</span>{{ currentPlan.waterRatio || '—' }}</div>
              <div class="pm-line"><span class="lbl">施药方式</span>{{ currentPlan.sprayMethod || '—' }}</div>
              <div class="pm-line"><span class="lbl">安全间隔期</span>
                <el-tag size="small" type="warning" effect="light">{{ currentPlan.safetyInterval || '—' }}</el-tag>
              </div>
            </div>
          </div>

          <div class="calc-result">
            <template v-if="currentPlan && area > 0">
              <el-row :gutter="16">
                <el-col :xs="24" :sm="12">
                  <div class="stat-card">
                    <div class="stat-icon bg-green"><el-icon><MagicStick /></el-icon></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ dosageResult }}</div>
                      <div class="stat-label">预计总用药量</div>
                    </div>
                  </div>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <div class="stat-card">
                    <div class="stat-icon bg-blue"><el-icon><Coffee /></el-icon></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ waterResult }}</div>
                      <div class="stat-label">预计总喷液量</div>
                    </div>
                  </div>
                </el-col>
              </el-row>

              <el-alert
                type="warning"
                :closable="false"
                show-icon
                class="calc-tip"
                title="安全提示"
                description="以上为按亩用量线性估算的参考值，实际用药请以农药产品标签说明及当地植保部门指导为准，并严格遵守安全间隔期。"
              />
            </template>

            <el-empty v-else description="请选择配比方案并输入作业面积，开始估算" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 作物详情弹窗 -->
    <el-dialog v-model="cropDialog" :title="current.cropName" width="640px" top="8vh">
      <div class="crop-detail">
        <img v-if="current.image" :src="current.image" alt="crop" class="cd-img" />
        <div class="cd-tags">
          <el-tag v-if="current.category" type="success" effect="light">{{ current.category }}</el-tag>
          <el-tag v-if="current.growthCycle" type="info" effect="light">生长周期：{{ current.growthCycle }}</el-tag>
        </div>
        <div class="cd-block">
          <div class="cd-label"><span class="bar"></span>常见病虫害</div>
          <p>{{ current.commonPests || '暂无' }}</p>
        </div>
        <div class="cd-block">
          <div class="cd-label"><span class="bar"></span>详细介绍</div>
          <p class="cd-desc">{{ current.description || '暂无详细介绍' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api'

const activeTab = ref('crop')

/* ---- 作物图谱 ---- */
const crops = ref([])
const cropTotal = ref(0)
const cropLoading = ref(false)
const cropFilters = reactive({ pageNum: 1, pageSize: 8, keyword: '', category: '' })

async function loadCrops(reset) {
  if (reset) cropFilters.pageNum = 1
  cropLoading.value = true
  try {
    const res = await knowledgeApi.crops({ ...cropFilters })
    crops.value = res.data?.records || []
    cropTotal.value = res.data?.total || 0
  } finally {
    cropLoading.value = false
  }
}

const cropDialog = ref(false)
const current = ref({})
function openCrop(c) {
  current.value = c
  cropDialog.value = true
}

/* ---- 农药配比 ---- */
const pesticides = ref([])
const pestTotal = ref(0)
const pestLoading = ref(false)
const pestFilters = reactive({ pageNum: 1, pageSize: 10, keyword: '', cropName: '' })
let pestLoaded = false

async function loadPesticides(reset) {
  if (reset) pestFilters.pageNum = 1
  pestLoading.value = true
  try {
    const res = await knowledgeApi.pesticides({ ...pestFilters })
    pesticides.value = res.data?.records || []
    pestTotal.value = res.data?.total || 0
    pestLoaded = true
  } finally {
    pestLoading.value = false
  }
}

onMounted(() => {
  loadCrops()
})

// 农药数据在首次切换 tab 时按需加载
watch(activeTab, (v) => {
  if (v === 'pesticide' && !pestLoaded) loadPesticides()
  if (v === 'calc' && !calcLoaded) loadCalcPlans()
})

/* ---- 配比计算器 ---- */
const calcPlans = ref([])
const calcPlanId = ref(null)
const area = ref(1)
const calcLoading = ref(false)
let calcLoaded = false

const currentPlan = computed(() => calcPlans.value.find((p) => p.id === calcPlanId.value) || null)

async function loadCalcPlans() {
  calcLoading.value = true
  try {
    const res = await knowledgeApi.pesticides({ pageNum: 1, pageSize: 200 })
    calcPlans.value = res.data?.records || []
    calcLoaded = true
  } finally {
    calcLoading.value = false
  }
}

function onPlanChange() {
  // 仅用于触发响应，计算由 computed 完成
}

// 从带单位文本中提取数值范围与单位，并按面积换算
function estimate(text, factor) {
  if (!text || !factor || factor <= 0) return '请参考标签'
  // 匹配 a-b 或单值，及紧随其后的单位
  const m = String(text).match(/(\d+(?:\.\d+)?)\s*(?:[-~～—]\s*(\d+(?:\.\d+)?))?\s*([^\d\s/、，,]+)?/)
  if (!m) return '请参考标签'
  const low = parseFloat(m[1])
  if (isNaN(low)) return '请参考标签'
  const high = m[2] != null ? parseFloat(m[2]) : low
  let unit = m[3] || ''
  // 去除“/亩”之类后缀里的“亩”等残留
  unit = unit.replace(/[\/／].*$/, '').trim()
  const fmt = (n) => Number((n * factor).toFixed(2)).toString()
  const lo = fmt(low)
  const hi = fmt(high)
  return lo === hi ? `${lo}${unit}` : `${lo}-${hi}${unit}`
}

const dosageResult = computed(() => estimate(currentPlan.value?.dosage, area.value))
const waterResult = computed(() => estimate(currentPlan.value?.waterRatio, area.value))
</script>

<style scoped lang="scss">
.knowledge-tabs :deep(.el-tabs__item) { font-size: 15px; }
.tab-label { display: inline-flex; align-items: center; gap: 6px; }

.crop-card {
  background: #fff; border-radius: var(--card-radius); box-shadow: var(--card-shadow);
  overflow: hidden; cursor: pointer; transition: all .25s ease; height: 100%;
  &:hover { transform: translateY(-4px); box-shadow: var(--card-shadow-hover); }
}
.crop-cover {
  position: relative; height: 140px; overflow: hidden;
  img { width: 100%; height: 100%; object-fit: cover; }
}
.cover-fallback { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #fff; }
.crop-tag { position: absolute; top: 10px; left: 10px; border: none; }
.crop-body { padding: 14px 16px; }
.crop-name { font-size: 16px; font-weight: 700; color: #1d2b24; margin-bottom: 10px; }
.crop-line {
  font-size: 13px; color: #6b7d73; margin-bottom: 6px; display: flex; gap: 8px;
  .lbl { color: #9bb0a5; flex-shrink: 0; }
  .clamp { display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
}

.crop-detail .cd-img { width: 100%; max-height: 280px; object-fit: cover; border-radius: 10px; margin-bottom: 14px; }
.cd-tags { display: flex; gap: 10px; margin-bottom: 16px; }
.cd-block { margin-bottom: 16px; }
.cd-label {
  font-weight: 700; color: #1d2b24; display: flex; align-items: center; gap: 8px; margin-bottom: 8px;
  .bar { width: 4px; height: 15px; background: var(--brand-gradient); border-radius: 3px; }
}
.cd-block p { margin: 0; color: #4a5e54; line-height: 1.8; font-size: 14px; }
.cd-desc { white-space: pre-wrap; }

/* 配比计算器 */
.calc-wrap { display: grid; grid-template-columns: 380px 1fr; gap: 18px; align-items: start; }
@media (max-width: 820px) { .calc-wrap { grid-template-columns: 1fr; } }

.calc-input { padding: 20px 22px; }
.calc-input .chart-title {
  font-weight: 700; color: #1d2b24; font-size: 15px;
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
  .bar { width: 4px; height: 16px; background: var(--brand-gradient); border-radius: 3px; }
}
.calc-form :deep(.el-form-item__label) { font-weight: 600; color: #4a5e54; padding-bottom: 4px; }

.plan-meta {
  margin-top: 6px; padding: 14px 16px;
  background: #f7faf8; border-radius: 10px; border: 1px solid #eef1f0;
}
.plan-meta .pm-line {
  display: flex; align-items: center; gap: 10px;
  font-size: 13px; color: #3a4a41; padding: 5px 0;
  .lbl { width: 80px; flex-shrink: 0; color: #9bb0a5; }
}

.calc-result .stat-card { margin-bottom: 16px; }
.calc-result .stat-card .stat-value { font-size: 22px; }
.calc-tip { border-radius: 10px; }
</style>
