<template>
  <div class="page-container" v-loading="loading">
    <template v-if="exam.id">
      <!-- 考试头部 -->
      <div class="exam-header">
        <div class="eh-left">
          <div class="eh-title">{{ exam.title }}</div>
          <div class="eh-sub">
            <span><el-icon><Collection /></el-icon>{{ questions.length }} 题</span>
            <span><el-icon><Medal /></el-icon>总分 {{ exam.totalScore }}</span>
            <span><el-icon><Flag /></el-icon>及格 {{ exam.passScore }}</span>
          </div>
        </div>
        <div class="eh-right">
          <div class="countdown" :class="{ urgent: remaining <= 60 }">
            <el-icon><Timer /></el-icon>
            <span>{{ mmss }}</span>
          </div>
          <div class="answered-tip">已作答 {{ answeredCount }} / {{ questions.length }}</div>
        </div>
      </div>

      <!-- 题目列表 -->
      <div
        v-for="(q, idx) in questions"
        :key="q.id"
        class="q-card"
      >
        <div class="q-head">
          <span class="q-index">{{ idx + 1 }}</span>
          <el-tag :type="typeTagType(q.type)" effect="light" size="small">{{ questionTypeMap[q.type] || q.type }}</el-tag>
          <span class="q-score">{{ q.score }} 分</span>
          <el-icon v-if="answers[q.id] && answers[q.id].length" class="q-done"><CircleCheckFilled /></el-icon>
        </div>
        <div class="q-content">{{ q.content }}</div>

        <!-- 单选 -->
        <el-radio-group
          v-if="q.type === 'SINGLE'"
          v-model="single[q.id]"
          class="q-options"
        >
          <el-radio v-for="opt in optionsOf(q)" :key="opt.key" :label="opt.key" class="q-opt">
            {{ opt.key }}. {{ opt.value }}
          </el-radio>
        </el-radio-group>

        <!-- 多选 -->
        <el-checkbox-group
          v-else-if="q.type === 'MULTI'"
          v-model="multi[q.id]"
          class="q-options"
        >
          <el-checkbox v-for="opt in optionsOf(q)" :key="opt.key" :label="opt.key" class="q-opt">
            {{ opt.key }}. {{ opt.value }}
          </el-checkbox>
        </el-checkbox-group>

        <!-- 判断 -->
        <el-radio-group
          v-else-if="q.type === 'JUDGE'"
          v-model="single[q.id]"
          class="q-options"
        >
          <el-radio v-for="opt in judgeOptions(q)" :key="opt.key" :label="opt.key" class="q-opt">
            {{ opt.value }}
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 提交栏 -->
      <div class="submit-bar">
        <div class="sb-info">
          <span class="sb-answered">已作答 <b>{{ answeredCount }}</b> / {{ questions.length }} 题</span>
          <span class="sb-time" :class="{ urgent: remaining <= 60 }">剩余 {{ mmss }}</span>
        </div>
        <el-button type="primary" :icon="Promotion" size="large" :loading="submitting" @click="confirmSubmit">交卷</el-button>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="试卷不存在或已下架">
      <el-button type="primary" @click="$router.push('/student/exams')">返回试卷列表</el-button>
    </el-empty>

    <!-- 结果弹窗 -->
    <el-dialog v-model="resultVisible" width="420px" :show-close="false" :close-on-click-modal="false" align-center>
      <div v-if="result" class="result-box">
        <div class="result-badge" :class="result.passed ? 'pass' : 'fail'">
          <el-icon :size="44">
            <component :is="result.passed ? 'CircleCheckFilled' : 'CircleCloseFilled'" />
          </el-icon>
        </div>
        <div class="result-title" :class="result.passed ? 'pass' : 'fail'">
          {{ result.passed ? '恭喜通过' : '未通过' }}
        </div>
        <div class="result-score">
          <span class="rs-num">{{ result.score }}</span>
          <span class="rs-total">/ {{ result.totalScore }} 分</span>
        </div>
        <div class="result-stats">
          <div class="rstat"><span>正确题数</span><b>{{ result.correctCount }} / {{ result.totalCount }}</b></div>
          <div class="rstat"><span>及格线</span><b>{{ result.passScore }} 分</b></div>
        </div>
        <div class="result-actions">
          <el-button @click="$router.push('/student/exams')">返回试卷列表</el-button>
          <el-button type="primary" @click="$router.push('/student/exam-records')">查看考试记录</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { examApi } from '@/api'
import { questionTypeMap, parseJSON } from '@/utils/dict'

const route = useRoute()
const examId = route.params.id

const loading = ref(false)
const submitting = ref(false)
const exam = reactive({ id: null, title: '', totalScore: 0, passScore: 0, duration: 0 })
const questions = ref([])

const single = reactive({})  // 单选 / 判断: questionId -> key
const multi = reactive({})   // 多选: questionId -> [keys]

let startTime = ''
const remaining = ref(0)
let timer = null
let submitted = false

const resultVisible = ref(false)
const result = ref(null)

const mmss = computed(() => {
  const s = Math.max(0, remaining.value)
  const m = String(Math.floor(s / 60)).padStart(2, '0')
  const sec = String(s % 60).padStart(2, '0')
  return `${m}:${sec}`
})

const answers = computed(() => {
  const out = {}
  questions.value.forEach((q) => {
    if (q.type === 'MULTI') out[q.id] = multi[q.id] || []
    else out[q.id] = single[q.id] ? [single[q.id]] : []
  })
  return out
})
const answeredCount = computed(() => Object.values(answers.value).filter((a) => a && a.length).length)

const typeTagType = (t) => ({ SINGLE: 'success', MULTI: 'warning', JUDGE: 'primary' }[t] || 'info')

function optionsOf(q) {
  const arr = parseJSON(q.options, [])
  return Array.isArray(arr) ? arr : []
}
function judgeOptions(q) {
  const arr = optionsOf(q)
  if (arr.length) return arr
  return [{ key: 'T', value: '正确' }, { key: 'F', value: '错误' }]
}

function buildAnswers() {
  return questions.value.map((q) => {
    let answer = ''
    if (q.type === 'MULTI') {
      answer = [...(multi[q.id] || [])].sort().join(',')
    } else {
      answer = single[q.id] || ''
    }
    return { questionId: q.id, answer }
  })
}

async function doSubmit(auto = false) {
  if (submitted) return
  submitted = true
  stopTimer()
  submitting.value = true
  try {
    const elapsed = exam.duration * 60 - Math.max(0, remaining.value)
    const res = await examApi.submit(examId, {
      answers: buildAnswers(),
      duration: elapsed,
      startTime
    })
    result.value = res.data || null
    resultVisible.value = true
    if (auto) ElMessage.warning('考试时间已到，已自动交卷')
  } catch (e) {
    submitted = false
    startTimer()
  } finally {
    submitting.value = false
  }
}

async function confirmSubmit() {
  const unanswered = questions.value.length - answeredCount.value
  try {
    await ElMessageBox.confirm(
      unanswered > 0 ? `还有 ${unanswered} 题未作答，确定要交卷吗？` : '确定要交卷吗？交卷后将无法修改答案。',
      '交卷确认',
      { confirmButtonText: '确定交卷', cancelButtonText: '继续作答', type: 'warning' }
    )
    doSubmit(false)
  } catch {
    /* cancelled */
  }
}

function startTimer() {
  timer = setInterval(() => {
    remaining.value -= 1
    if (remaining.value <= 0) {
      remaining.value = 0
      doSubmit(true)
    }
  }, 1000)
}
function stopTimer() {
  if (timer) { clearInterval(timer); timer = null }
}

async function load() {
  loading.value = true
  try {
    const res = await examApi.paper(examId)
    const data = res.data || {}
    Object.assign(exam, data.exam || {})
    questions.value = data.questions || []
    questions.value.forEach((q) => {
      if (q.type === 'MULTI') multi[q.id] = []
      else single[q.id] = ''
    })
    startTime = new Date().toISOString()
    remaining.value = (exam.duration || 0) * 60
    if (remaining.value > 0) startTimer()
  } finally {
    loading.value = false
  }
}

onMounted(load)
onBeforeUnmount(stopTimer)
</script>

<style scoped lang="scss">
.exam-header {
  background: linear-gradient(120deg, #0f3d2e 0%, #14724f 60%, #1fb574 130%);
  border-radius: 16px;
  padding: 24px 28px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  box-shadow: 0 8px 28px rgba(15, 61, 46, .22);
}
.eh-title { font-size: 22px; font-weight: 700; }
.eh-sub {
  display: flex; gap: 18px; margin-top: 10px; font-size: 13px; color: #cdeede;
  span { display: flex; align-items: center; gap: 5px; }
}
.eh-right { text-align: right; }
.countdown {
  display: inline-flex; align-items: center; gap: 8px;
  background: rgba(255, 255, 255, .16); padding: 8px 18px; border-radius: 12px;
  font-size: 24px; font-weight: 700; font-variant-numeric: tabular-nums;
  &.urgent { background: rgba(232, 80, 79, .9); animation: pulse 1s infinite; }
}
.answered-tip { font-size: 12px; color: #cdeede; margin-top: 8px; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .65; } }

.q-card {
  background: #fff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  padding: 22px 24px;
  margin-bottom: 16px;
}
.q-head { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.q-index {
  width: 26px; height: 26px; border-radius: 8px;
  background: var(--brand-gradient); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}
.q-score { font-size: 13px; color: #9bb0a5; }
.q-done { color: var(--brand-primary); font-size: 18px; margin-left: auto; }
.q-content { font-size: 15px; color: #1d2b24; line-height: 1.7; font-weight: 600; margin-bottom: 14px; }
.q-options { display: flex; flex-direction: column; gap: 12px; width: 100%; }
.q-opt {
  margin: 0; height: auto; padding: 12px 14px;
  border: 1px solid #eef3f0; border-radius: 10px; width: 100%;
  white-space: normal; line-height: 1.5; transition: all .2s;
  :deep(.el-radio__label), :deep(.el-checkbox__label) { white-space: normal; }
  &:hover { border-color: var(--brand-primary); background: #f7fbf9; }
}
.q-opt.is-checked { border-color: var(--brand-primary); background: var(--brand-primary-light); }

.submit-bar {
  position: sticky;
  bottom: 0;
  margin-top: 18px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 -4px 20px rgba(16, 78, 54, .08);
  padding: 14px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sb-info { display: flex; gap: 22px; font-size: 14px; color: #6a7c72; }
.sb-answered b { color: var(--brand-primary); font-size: 16px; }
.sb-time { font-weight: 600; &.urgent { color: #e8504f; } }

.result-box { text-align: center; padding: 8px 4px 0; }
.result-badge {
  width: 88px; height: 88px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; margin: 0 auto 14px;
  &.pass { background: #e6f7ef; color: #0e8a5a; }
  &.fail { background: #fdecec; color: #e8504f; }
}
.result-title { font-size: 22px; font-weight: 700; &.pass { color: #0e8a5a; } &.fail { color: #e8504f; } }
.result-score { margin: 10px 0 18px; }
.rs-num { font-size: 40px; font-weight: 700; color: #1d2b24; }
.rs-total { font-size: 16px; color: #9bb0a5; margin-left: 4px; }
.result-stats {
  display: flex; justify-content: center; gap: 14px; margin-bottom: 22px;
}
.rstat {
  flex: 1; background: #f5f9f7; border-radius: 10px; padding: 12px;
  span { display: block; font-size: 12px; color: #9bb0a5; margin-bottom: 4px; }
  b { font-size: 16px; color: #1d2b24; }
}
.result-actions { display: flex; gap: 12px; .el-button { flex: 1; margin: 0; } }
</style>
