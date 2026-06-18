<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>智慧农情 AI 助手</div>
        <div class="page-subtitle">基于 VisualGLM 图像识别大模型，智能识别病虫害并推荐农药配比与飞行参数</div>
      </div>
    </div>

    <el-row :gutter="18">
      <!-- 诊断输入 -->
      <el-col :lg="10" :md="24">
        <div class="ai-panel">
          <div class="ai-panel-head">
            <div class="ai-spark"><el-icon :size="22"><MagicStick /></el-icon></div>
            <div>
              <div class="ap-title">农情智能诊断</div>
              <div class="ap-sub">上传田间照片，描述症状，AI 为你出具诊断报告</div>
            </div>
          </div>

          <div class="ai-form">
            <div class="field-label">田间照片</div>
            <image-upload v-model="form.imageUrl" />

            <div class="field-label" style="margin-top: 16px">作物类型</div>
            <el-select v-model="form.cropType" placeholder="选择作物" style="width: 100%">
              <el-option v-for="c in cropOptions" :key="c" :label="c" :value="c" />
            </el-select>

            <div class="field-label" style="margin-top: 16px">描述症状 / 提问</div>
            <el-input
              v-model="form.question"
              type="textarea"
              :rows="3"
              placeholder="如：叶片有褐斑是什么病？该如何防治？"
            />

            <el-button
              type="primary"
              size="large"
              :icon="MagicStick"
              :loading="diagnosing"
              style="width: 100%; margin-top: 18px"
              @click="onDiagnose"
            >
              开始诊断
            </el-button>
            <div class="ai-note">
              <el-icon><InfoFilled /></el-icon>
              本助手由 VisualGLM 第三方大模型提供病虫害识别与农药配比推荐，结果仅供参考。
            </div>
          </div>
        </div>
      </el-col>

      <!-- 诊断报告 -->
      <el-col :lg="14" :md="24">
        <div class="report-card" v-loading="diagnosing" element-loading-text="AI 正在分析图像...">
          <template v-if="result">
            <div class="rc-head">
              <div class="rc-title"><el-icon><Opportunity /></el-icon> 诊断报告</div>
              <el-tag type="success" effect="dark" size="small">{{ result.cropType || '—' }}</el-tag>
            </div>

            <div class="rc-body">
              <div class="rc-img">
                <img v-if="result.imageUrl" :src="result.imageUrl" alt="field" />
                <div v-else class="img-fallback"><el-icon :size="40"><Picture /></el-icon></div>
              </div>

              <div class="rc-info">
                <div class="ri-block">
                  <div class="ri-label">识别结果</div>
                  <div class="ri-value">{{ result.recognitionResult || '—' }}</div>
                </div>
                <div class="ri-block">
                  <div class="ri-label">推荐用药</div>
                  <div class="ri-value pesticide">{{ result.recommendPesticide || '—' }}</div>
                </div>
                <div class="ri-block">
                  <div class="ri-label">识别置信度</div>
                  <el-progress
                    :percentage="confidencePct"
                    :stroke-width="14"
                    :color="confColor"
                    text-inside
                    style="margin-top: 4px"
                  />
                </div>
              </div>
            </div>

            <div class="ri-label" style="margin: 18px 0 10px">推荐飞行参数</div>
            <div class="param-grid">
              <div class="param-chip bg-green">
                <el-icon><Odometer /></el-icon>
                <div><div class="pc-val">{{ params.speed ?? '—' }}</div><div class="pc-lbl">速度 (m/s)</div></div>
              </div>
              <div class="param-chip bg-blue">
                <el-icon><Top /></el-icon>
                <div><div class="pc-val">{{ params.height ?? '—' }}</div><div class="pc-lbl">高度 (m)</div></div>
              </div>
              <div class="param-chip bg-orange">
                <el-icon><Drizzling /></el-icon>
                <div><div class="pc-val">{{ params.flow ?? '—' }}</div><div class="pc-lbl">流量 (L/亩)</div></div>
              </div>
            </div>
          </template>

          <el-empty v-else description="上传照片并开始诊断，AI 报告将在此呈现" />
        </div>
      </el-col>
    </el-row>

    <!-- 诊断历史 -->
    <div class="chart-card" style="margin-top: 18px" v-loading="hisLoading">
      <div class="chart-title"><span class="bar"></span>诊断历史</div>
      <div v-if="history.length" class="history-list">
        <div v-for="h in history" :key="h.id" class="history-item">
          <div class="hi-thumb">
            <img v-if="h.imageUrl" :src="h.imageUrl" alt="thumb" />
            <div v-else class="img-fallback sm"><el-icon><Picture /></el-icon></div>
          </div>
          <div class="hi-main">
            <div class="hi-top">
              <el-tag type="success" effect="light" size="small">{{ h.cropType || '—' }}</el-tag>
              <span class="hi-result">{{ h.recognitionResult || '—' }}</span>
            </div>
            <div class="hi-meta">
              <span><el-icon><Calendar /></el-icon>{{ h.createTime || '—' }}</span>
              <span><el-icon><MagicStick /></el-icon>置信度 {{ Math.round(Number(h.confidence) || 0) }}%</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!hisLoading" description="暂无诊断记录" />

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
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { aiApi } from '@/api'
import { parseJSON } from '@/utils/dict'

const cropOptions = ['水稻', '小麦', '玉米', '柑橘', '棉花', '蔬菜', '其他']

const form = reactive({ imageUrl: '', cropType: '', question: '' })
const result = ref(null)
const diagnosing = ref(false)

const confidencePct = computed(() => Math.max(0, Math.min(100, Math.round(Number(result.value?.confidence) || 0))))
const confColor = computed(() => (confidencePct.value >= 80 ? '#15a06b' : confidencePct.value >= 50 ? '#f0a020' : '#e8504f'))
const params = computed(() => parseJSON(result.value?.recommendParams, {}) || {})

async function onDiagnose() {
  if (!form.imageUrl) return ElMessage.warning('请先上传田间照片')
  if (!form.cropType) return ElMessage.warning('请选择作物类型')
  diagnosing.value = true
  try {
    const res = await aiApi.diagnose({ ...form })
    result.value = res.data || null
    ElMessage.success('诊断完成')
    loadHistory()
  } finally {
    diagnosing.value = false
  }
}

/* ---- 历史 ---- */
const history = ref([])
const hisTotal = ref(0)
const hisLoading = ref(false)
const hisQuery = reactive({ pageNum: 1, pageSize: 6 })

async function loadHistory() {
  hisLoading.value = true
  try {
    const res = await aiApi.history({ ...hisQuery })
    history.value = res.data?.records || []
    hisTotal.value = res.data?.total || 0
  } finally {
    hisLoading.value = false
  }
}

onMounted(loadHistory)
</script>

<style scoped lang="scss">
/* 输入面板 */
.ai-panel {
  background: linear-gradient(165deg, #0f3d2e 0%, #14724f 100%);
  border-radius: var(--card-radius);
  box-shadow: 0 8px 28px rgba(15, 61, 46, .22);
  padding: 22px;
  color: #fff;
  height: 100%;
}
.ai-panel-head { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; }
.ai-spark {
  width: 46px; height: 46px; border-radius: 14px; flex-shrink: 0;
  background: linear-gradient(135deg, #a78bfa, #7c5cf0);
  display: flex; align-items: center; justify-content: center; color: #fff;
  box-shadow: 0 4px 14px rgba(124, 92, 240, .5);
}
.ap-title { font-size: 18px; font-weight: 700; }
.ap-sub { font-size: 12px; color: #b6e3cf; margin-top: 3px; }
.ai-form { background: rgba(255, 255, 255, .96); border-radius: 12px; padding: 18px; }
.field-label { font-size: 13px; font-weight: 600; color: #44574e; margin-bottom: 8px; }
.ai-note {
  display: flex; align-items: flex-start; gap: 6px;
  font-size: 12px; color: #8a9b92; margin-top: 12px; line-height: 1.6;
  .el-icon { margin-top: 2px; flex-shrink: 0; color: var(--brand-primary); }
}

/* 报告 */
.report-card {
  background: #fff; border-radius: var(--card-radius); box-shadow: var(--card-shadow);
  padding: 22px; height: 100%; min-height: 320px;
  display: flex; flex-direction: column; justify-content: center;
}
.rc-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
.rc-title { font-size: 17px; font-weight: 700; color: #1d2b24; display: flex; align-items: center; gap: 8px;
  .el-icon { color: var(--brand-primary); } }
.rc-body { display: flex; gap: 20px; }
.rc-img {
  width: 180px; height: 180px; border-radius: 12px; overflow: hidden; flex-shrink: 0; background: #f3f6f4;
  img { width: 100%; height: 100%; object-fit: cover; }
}
.img-fallback {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #c5d6cd;
  &.sm { border-radius: 10px; }
}
.rc-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 14px; justify-content: center; }
.ri-block .ri-label { font-size: 12px; color: #9bb0a5; margin-bottom: 5px; }
.ri-block .ri-value { font-size: 15px; font-weight: 600; color: #1d2b24; line-height: 1.6; }
.ri-value.pesticide { color: var(--brand-primary); }
.ri-label { font-size: 12px; color: #9bb0a5; }

.param-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.param-chip {
  border-radius: 12px; padding: 16px; color: #fff; display: flex; align-items: center; gap: 12px;
  .el-icon { font-size: 26px; opacity: .9; }
  .pc-val { font-size: 22px; font-weight: 700; line-height: 1.1; }
  .pc-lbl { font-size: 12px; opacity: .9; margin-top: 3px; }
}

/* 历史 */
.history-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; margin-top: 8px; }
.history-item {
  display: flex; gap: 14px; padding: 12px; border-radius: 12px; border: 1px solid #eef3f0;
  transition: all .2s ease; cursor: default;
  &:hover { box-shadow: var(--card-shadow-hover); transform: translateY(-2px); }
}
.hi-thumb {
  width: 72px; height: 72px; border-radius: 10px; overflow: hidden; flex-shrink: 0; background: #f3f6f4;
  img { width: 100%; height: 100%; object-fit: cover; }
}
.hi-main { flex: 1; min-width: 0; }
.hi-top { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.hi-result {
  font-size: 14px; font-weight: 600; color: #1d2b24;
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;
}
.hi-meta {
  display: flex; flex-wrap: wrap; gap: 14px; font-size: 12px; color: #9bb0a5;
  span { display: flex; align-items: center; gap: 4px; }
}
</style>
