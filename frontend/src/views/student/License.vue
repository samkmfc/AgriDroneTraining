<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>我的执照</div>
        <div class="page-subtitle">查看你持有的无人机操作执照及有效期状态</div>
      </div>
    </div>

    <!-- 状态汇总提醒 -->
    <el-alert
      v-if="summaryAlert"
      :title="summaryAlert.title"
      :type="summaryAlert.type"
      :description="summaryAlert.desc"
      show-icon
      :closable="false"
      style="margin-bottom: 18px; border-radius: 12px"
    />

    <div v-loading="loading">
      <el-row v-if="list.length" :gutter="18">
        <el-col v-for="lic in list" :key="lic.id" :xs="24" :sm="24" :md="12" style="margin-bottom: 18px">
          <div class="license-card">
            <!-- 证件头部 -->
            <div class="lic-head bg-green">
              <div class="lic-head-left">
                <el-icon :size="22"><Postcard /></el-icon>
                <div>
                  <div class="lic-head-title">{{ lic.licenseType || '无人机操作执照' }}</div>
                  <div class="lic-head-sub">CIVIL UAV PILOT LICENSE</div>
                </div>
              </div>
              <el-tag :type="statusMeta(lic).type" effect="dark" size="small" class="lic-status-tag">
                {{ statusMeta(lic).text }}
              </el-tag>
            </div>

            <!-- 证件正文 -->
            <div class="lic-body">
              <div class="lic-no-label">执照编号</div>
              <div class="lic-no">{{ lic.licenseNo || '—' }}</div>

              <el-row :gutter="12" class="lic-fields">
                <el-col :span="12">
                  <div class="field-label">适用机型</div>
                  <div class="field-value">{{ lic.droneModel || '—' }}</div>
                </el-col>
                <el-col :span="12">
                  <div class="field-label">发证机关</div>
                  <div class="field-value">{{ lic.issuingAuthority || '—' }}</div>
                </el-col>
                <el-col :span="12">
                  <div class="field-label">发证日期</div>
                  <div class="field-value">{{ lic.issueDate || '—' }}</div>
                </el-col>
                <el-col :span="12">
                  <div class="field-label">有效期至</div>
                  <div class="field-value">{{ lic.expiryDate || '—' }}</div>
                </el-col>
              </el-row>

              <!-- 到期提示条 -->
              <div v-if="lic.status === 'EXPIRING'" class="lic-tip tip-warn">
                <el-icon><WarningFilled /></el-icon>
                即将到期 · 剩余 {{ lic.remainingDays }} 天，请及时续期
              </div>
              <div v-else-if="lic.status === 'EXPIRED'" class="lic-tip tip-danger">
                <el-icon><CircleCloseFilled /></el-icon>
                已过期，请勿违规作业，尽快联系机构续期
              </div>
              <div v-else class="lic-tip tip-ok">
                <el-icon><CircleCheckFilled /></el-icon>
                执照状态正常，可合规作业
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-else-if="!loading" description="暂无执照记录，请联系培训机构登记">
        <el-button type="primary" @click="$router.push('/student/dashboard')">返回首页</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { licenseApi } from '@/api'
import { licenseStatusMap } from '@/utils/dict'

const loading = ref(false)
const list = ref([])

const statusMeta = (lic) => licenseStatusMap[lic.status] || { text: '未知', type: 'info' }

const summaryAlert = computed(() => {
  const expired = list.value.filter((l) => l.status === 'EXPIRED').length
  const expiring = list.value.filter((l) => l.status === 'EXPIRING').length
  if (expired) {
    return { type: 'error', title: `有 ${expired} 张执照已过期`, desc: '已过期执照不可用于合规作业，请尽快联系培训机构办理续期。' }
  }
  if (expiring) {
    return { type: 'warning', title: `有 ${expiring} 张执照即将到期`, desc: '请关注执照有效期，及时办理续期以免影响飞行作业。' }
  }
  return null
})

async function load() {
  loading.value = true
  try {
    const res = await licenseApi.my()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.license-card {
  background: #fff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  overflow: hidden;
  height: 100%;
  transition: all .25s ease;
  &:hover { transform: translateY(-4px); box-shadow: var(--card-shadow-hover); }
}
.lic-head {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  color: #fff;
}
.lic-head::after {
  content: '';
  position: absolute;
  right: -30px; top: -40px;
  width: 130px; height: 130px;
  background: rgba(255, 255, 255, .12);
  border-radius: 50%;
}
.lic-head-left { display: flex; align-items: center; gap: 12px; z-index: 1; }
.lic-head-title { font-size: 17px; font-weight: 700; }
.lic-head-sub { font-size: 11px; letter-spacing: 1px; opacity: .8; margin-top: 2px; }
.lic-status-tag { z-index: 1; border: none; }

.lic-body { padding: 20px 22px 22px; }
.lic-no-label { font-size: 12px; color: #9bb0a5; }
.lic-no {
  font-size: 24px; font-weight: 700; color: #1d2b24;
  letter-spacing: 2px; font-family: 'Segoe UI', monospace; margin: 2px 0 16px;
}
.lic-fields .el-col { margin-bottom: 14px; }
.field-label { font-size: 12px; color: #9bb0a5; margin-bottom: 3px; }
.field-value { font-size: 14px; font-weight: 600; color: #2c3e36; }

.lic-tip {
  display: flex; align-items: center; gap: 6px;
  margin-top: 6px; padding: 10px 14px; border-radius: 10px;
  font-size: 13px; font-weight: 600;
}
.tip-ok { background: #e6f7ef; color: #0e8a5a; }
.tip-warn { background: #fdf4e6; color: #d98509; }
.tip-danger { background: #fdecec; color: #e8504f; }
</style>
