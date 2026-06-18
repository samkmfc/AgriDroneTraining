<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>考试记录</div>
        <div class="page-subtitle">查看你历次场景化测试的成绩与通过情况</div>
      </div>
      <el-button type="primary" :icon="EditPen" plain @click="$router.push('/student/exams')">参加测试</el-button>
    </div>

    <!-- 汇总统计 -->
    <el-row :gutter="18" style="margin-bottom: 18px">
      <el-col :xs="8" :sm="8" :md="8">
        <div class="stat-card">
          <div class="stat-icon bg-blue"><el-icon><Tickets /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ total }}</div>
            <div class="stat-label">考试次数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="8" :sm="8" :md="8">
        <div class="stat-card">
          <div class="stat-icon bg-green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ passCount }}</div>
            <div class="stat-label">通过次数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="8" :sm="8" :md="8">
        <div class="stat-card">
          <div class="stat-icon bg-orange"><el-icon><TrophyBase /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ passRate }}%</div>
            <div class="stat-label">通过率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="table-card" v-loading="loading">
      <el-table v-if="list.length" :data="list" stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="examTitle" label="试卷" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="font-weight: 600; color: #1d2b24">{{ row.examTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column label="得分" width="110" align="center">
          <template #default="{ row }">
            <span class="score-cell" :class="row.passed ? 'pass' : 'fail'">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.passed ? 'success' : 'danger'" effect="light">
              {{ row.passed ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用时" width="130" align="center">
          <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="180">
          <template #default="{ row }">
            <span style="color: #6a7c72">{{ row.submitTime || row.createTime || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openDetail(row)">查看解析</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else-if="!loading" description="还没有考试记录，快去参加第一场测试吧">
        <el-button type="primary" @click="$router.push('/student/exams')">前往测试中心</el-button>
      </el-empty>
    </div>

    <!-- 答案解析回顾 -->
    <el-dialog v-model="detailDialog" title="答案解析回顾" width="760px" top="5vh" class="analysis-dialog">
      <div v-loading="detailLoading" class="analysis-body">
        <template v-if="detail">
          <!-- 成绩概览 -->
          <div class="exam-overview">
            <div class="ov-title">{{ detail.examTitle }}</div>
            <div class="ov-stats">
              <div class="ov-score">
                <span class="ov-num" :class="detail.record?.passed ? 'pass' : 'fail'">{{ detail.record?.score ?? '—' }}</span>
                <span class="ov-total">/ {{ detail.totalScore ?? '—' }} 分</span>
              </div>
              <el-tag :type="detail.record?.passed ? 'success' : 'danger'" effect="dark" size="large">
                {{ detail.record?.passed ? '通过' : '未通过' }}
              </el-tag>
              <div class="ov-meta">
                <span>及格分 {{ detail.passScore ?? '—' }}</span>
                <span>用时 {{ formatDuration(detail.record?.duration) }}</span>
                <span v-if="detail.record?.submitTime">{{ detail.record.submitTime }}</span>
              </div>
            </div>
          </div>

          <!-- 逐题解析 -->
          <div v-for="(q, idx) in (detail.questions || [])" :key="q.id" class="q-item">
            <div class="q-head">
              <el-icon class="q-flag" :class="q.correct ? 'ok' : 'no'">
                <component :is="q.correct ? CircleCheckFilled : CircleCloseFilled" />
              </el-icon>
              <span class="q-no">第 {{ idx + 1 }} 题</span>
              <el-tag size="small" effect="plain" type="info">{{ questionTypeMap[q.type] || q.type }}</el-tag>
              <el-tag v-if="q.category" size="small" effect="plain">{{ q.category }}</el-tag>
              <span class="q-score">{{ q.score }} 分</span>
            </div>

            <div class="q-content">{{ q.content }}</div>

            <!-- 选择题选项 -->
            <ul v-if="q.type !== 'JUDGE'" class="q-options">
              <li
                v-for="opt in parseOptions(q.options)"
                :key="opt.key"
                class="q-opt"
                :class="optClass(q, opt.key)"
              >
                <span class="opt-key">{{ opt.key }}</span>
                <span class="opt-val">{{ opt.value }}</span>
                <el-tag v-if="isCorrectKey(q, opt.key)" size="small" type="success" effect="light">正确答案</el-tag>
                <el-tag v-if="isMyKey(q, opt.key)" size="small" :type="q.correct ? 'success' : 'danger'" effect="light">我的作答</el-tag>
              </li>
            </ul>

            <!-- 判断题 -->
            <div v-else class="q-judge">
              <div class="judge-line">
                <span class="lbl">正确答案</span>
                <el-tag size="small" type="success" effect="light">{{ judgeText(q.answer) }}</el-tag>
              </div>
              <div class="judge-line">
                <span class="lbl">我的作答</span>
                <el-tag
                  v-if="q.myAnswer != null && q.myAnswer !== ''"
                  size="small"
                  :type="q.correct ? 'success' : 'danger'"
                  effect="light"
                >{{ judgeText(q.myAnswer) }}</el-tag>
                <span v-else class="no-answer">未作答</span>
              </div>
            </div>

            <!-- 未作答提示(选择题) -->
            <div v-if="q.type !== 'JUDGE' && (q.myAnswer == null || q.myAnswer === '')" class="no-answer-tip">
              本题未作答
            </div>

            <div v-if="q.analysis" class="q-analysis">
              <div class="ana-label"><el-icon><Document /></el-icon> 解析</div>
              <p>{{ q.analysis }}</p>
            </div>
          </div>

          <el-empty v-if="!(detail.questions && detail.questions.length)" description="暂无题目解析" />
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { EditPen, View, CircleCheckFilled, CircleCloseFilled, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { examApi } from '@/api'
import { questionTypeMap, parseJSON } from '@/utils/dict'

const loading = ref(false)
const list = ref([])

const total = computed(() => list.value.length)
const passCount = computed(() => list.value.filter((r) => r.passed).length)
const passRate = computed(() => (total.value ? Math.round((passCount.value / total.value) * 100) : 0))

function formatDuration(sec) {
  const s = Number(sec) || 0
  const m = Math.floor(s / 60)
  const r = s % 60
  return m > 0 ? `${m}分${r}秒` : `${r}秒`
}

async function load() {
  loading.value = true
  try {
    const res = await examApi.myRecords()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

/* ---- 答案解析回顾 ---- */
const detailDialog = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

async function openDetail(row) {
  detailDialog.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await examApi.recordDetail(row.id)
    if (res.code === 0 || res.code === 200) {
      detail.value = res.data
    } else {
      detail.value = res.data || null
    }
    if (!detail.value) ElMessage.error('未获取到解析数据')
  } catch (e) {
    ElMessage.error('加载解析失败')
  } finally {
    detailLoading.value = false
  }
}

function parseOptions(options) {
  const arr = parseJSON(options, [])
  return Array.isArray(arr) ? arr : []
}

// 正确答案 / 我的作答 可能是 "A"、"A,B"、数组等，统一拆成 key 集合
function toKeys(val) {
  if (val == null || val === '') return []
  if (Array.isArray(val)) return val.map((v) => String(v).trim())
  return String(val)
    .split(/[,，;；\s]+/)
    .map((v) => v.trim())
    .filter(Boolean)
}

function isCorrectKey(q, key) {
  return toKeys(q.answer).includes(String(key))
}
function isMyKey(q, key) {
  return toKeys(q.myAnswer).includes(String(key))
}
function optClass(q, key) {
  const correct = isCorrectKey(q, key)
  const mine = isMyKey(q, key)
  return {
    'is-correct': correct,
    'is-wrong': mine && !correct && q.correct === false
  }
}

function judgeText(val) {
  const v = String(val).trim().toUpperCase()
  if (['T', 'TRUE', '1', '对', '正确'].includes(v)) return '正确'
  if (['F', 'FALSE', '0', '错', '错误'].includes(v)) return '错误'
  return val
}

onMounted(load)
</script>

<style scoped lang="scss">
.score-cell { font-size: 18px; font-weight: 700; }
.score-cell.pass { color: var(--brand-primary); }
.score-cell.fail { color: #e8504f; }

/* 答案解析回顾 */
.analysis-body { max-height: 76vh; overflow-y: auto; padding-right: 6px; }

.exam-overview {
  background: var(--brand-primary-light);
  border: 1px solid #d4f0e2;
  border-radius: 12px;
  padding: 18px 20px;
  margin-bottom: 18px;
}
.exam-overview .ov-title { font-size: 17px; font-weight: 700; color: #1d2b24; margin-bottom: 12px; }
.exam-overview .ov-stats { display: flex; align-items: center; flex-wrap: wrap; gap: 18px; }
.ov-score { display: flex; align-items: baseline; gap: 4px; }
.ov-score .ov-num { font-size: 30px; font-weight: 800; line-height: 1; }
.ov-score .ov-num.pass { color: var(--brand-primary); }
.ov-score .ov-num.fail { color: #e8504f; }
.ov-score .ov-total { font-size: 14px; color: #6a7c72; }
.ov-meta { display: flex; gap: 16px; font-size: 13px; color: #6a7c72; flex-wrap: wrap; }

.q-item {
  border: 1px solid #eef1f0;
  border-radius: 12px;
  padding: 16px 18px;
  margin-bottom: 14px;
  background: #fff;
}
.q-head { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.q-head .q-flag { font-size: 20px; }
.q-head .q-flag.ok { color: var(--brand-primary); }
.q-head .q-flag.no { color: #e8504f; }
.q-head .q-no { font-weight: 700; color: #1d2b24; }
.q-head .q-score { margin-left: auto; font-size: 13px; color: #f08a24; font-weight: 600; }

.q-content { font-size: 14px; color: #2c3a32; line-height: 1.7; margin-bottom: 12px; }

.q-options { list-style: none; padding: 0; margin: 0; }
.q-opt {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 12px; margin-bottom: 8px;
  border: 1px solid #eef1f0; border-radius: 8px;
  font-size: 14px; color: #3a4a41;
}
.q-opt .opt-key {
  width: 22px; height: 22px; flex-shrink: 0;
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 50%; background: #f0f3f1; color: #6a7c72; font-size: 12px; font-weight: 700;
}
.q-opt .opt-val { flex: 1; }
.q-opt.is-correct {
  background: var(--brand-primary-light); border-color: #b8e6d1;
  .opt-key { background: var(--brand-primary); color: #fff; }
  .opt-val { color: var(--brand-primary-dark); font-weight: 600; }
}
.q-opt.is-wrong {
  background: #fdeceb; border-color: #f7c5c3;
  .opt-key { background: #e8504f; color: #fff; }
  .opt-val { color: #c0392b; }
}

.q-judge { display: flex; flex-direction: column; gap: 8px; }
.q-judge .judge-line { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.q-judge .judge-line .lbl { width: 64px; color: #9bb0a5; }
.no-answer { color: #e8504f; font-size: 13px; }
.no-answer-tip { margin-top: 6px; font-size: 13px; color: #e8504f; }

.q-analysis {
  margin-top: 12px; padding: 12px 14px;
  background: #f7faf8; border-radius: 8px; border-left: 3px solid var(--brand-primary);
}
.q-analysis .ana-label {
  display: flex; align-items: center; gap: 6px;
  font-weight: 700; color: #1d2b24; font-size: 13px; margin-bottom: 6px;
}
.q-analysis p { margin: 0; color: #4a5e54; line-height: 1.7; font-size: 13px; white-space: pre-wrap; }
</style>
