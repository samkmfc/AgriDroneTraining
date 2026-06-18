<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>个人档案</div>
        <div class="page-subtitle">维护你的基本信息与培训档案，便于机构联系与管理</div>
      </div>
    </div>

    <el-row :gutter="18" v-loading="loading">
      <!-- 左侧：头像卡片 -->
      <el-col :md="8" :sm="24" style="margin-bottom: 18px">
        <div class="profile-side">
          <div class="side-banner bg-green"></div>
          <div class="side-body">
            <el-avatar :size="92" :src="form.avatar" class="side-avatar">
              {{ (form.realName || form.username || '?').charAt(0) }}
            </el-avatar>
            <div class="side-name">{{ form.realName || form.username || '未命名' }}</div>
            <div class="side-username">@{{ form.username }}</div>
            <el-tag
              v-if="trainingTag"
              :type="trainingTag.type"
              effect="light"
              size="small"
              style="margin-top: 8px"
            >{{ trainingTag.text }}</el-tag>

            <el-divider />

            <div class="side-upload">
              <div class="upload-label">更换头像</div>
              <ImageUpload v-model="form.avatar" />
            </div>

            <div class="side-meta">
              <div class="meta-row"><el-icon><Calendar /></el-icon><span>入学时间</span><b>{{ form.enrollDate || '—' }}</b></div>
              <div class="meta-row"><el-icon><Iphone /></el-icon><span>联系电话</span><b>{{ form.phone || '—' }}</b></div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：编辑表单 -->
      <el-col :md="16" :sm="24">
        <div class="app-card" style="margin-bottom: 18px">
          <div class="chart-title"><span class="bar"></span>基本信息</div>
          <el-form ref="baseRef" :model="form" :rules="baseRules" label-width="92px" style="margin-top: 16px">
            <el-row :gutter="18">
              <el-col :sm="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="form.realName" placeholder="请输入真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :sm="12">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="form.nickname" placeholder="请输入昵称" />
                </el-form-item>
              </el-col>
              <el-col :sm="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="form.gender">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :sm="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" />
                </el-form-item>
              </el-col>
              <el-col :sm="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入邮箱" :prefix-icon="Message" />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="form-actions">
              <el-button type="primary" :icon="Check" :loading="savingBase" @click="saveBase">保存基本信息</el-button>
            </div>
          </el-form>
        </div>

        <div class="app-card">
          <div class="chart-title"><span class="bar"></span>档案信息</div>
          <el-form ref="archiveRef" :model="form" :rules="archiveRules" label-width="92px" style="margin-top: 16px">
            <el-row :gutter="18">
              <el-col :sm="12">
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="form.idCard" placeholder="请输入身份证号" />
                </el-form-item>
              </el-col>
              <el-col :sm="12">
                <el-form-item label="学历" prop="education">
                  <el-select v-model="form.education" placeholder="请选择学历" style="width: 100%">
                    <el-option v-for="e in educations" :key="e" :label="e" :value="e" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :sm="24">
                <el-form-item label="联系地址" prop="address">
                  <el-input v-model="form.address" placeholder="请输入联系地址" />
                </el-form-item>
              </el-col>
              <el-col :sm="12">
                <el-form-item label="紧急联系人" prop="emergencyContact">
                  <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" />
                </el-form-item>
              </el-col>
              <el-col :sm="12">
                <el-form-item label="紧急电话" prop="emergencyPhone">
                  <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="form-actions">
              <el-button type="primary" :icon="Check" :loading="savingArchive" @click="saveArchive">保存档案信息</el-button>
            </div>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { Check, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { trainingStatusMap } from '@/utils/dict'
import ImageUpload from '@/components/ImageUpload.vue'

const loading = ref(false)
const savingBase = ref(false)
const savingArchive = ref(false)
const baseRef = ref()
const archiveRef = ref()

const educations = ['初中', '高中/中专', '大专', '本科', '硕士及以上']

const form = reactive({
  userId: null,
  username: '',
  realName: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 1,
  avatar: '',
  idCard: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  education: '',
  trainingStatus: '',
  enrollDate: ''
})

const trainingTag = computed(() => trainingStatusMap[form.trainingStatus] || null)

const phoneRule = { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
const baseRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [phoneRule],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}
const archiveRules = {
  idCard: [{ pattern: /^\d{17}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }],
  emergencyPhone: [phoneRule]
}

async function load() {
  loading.value = true
  try {
    const res = await userApi.getStudentProfile()
    const d = res.data || {}
    Object.keys(form).forEach((k) => {
      if (d[k] != null) form[k] = d[k]
    })
    if (d.gender != null) form.gender = Number(d.gender) || 1
  } finally {
    loading.value = false
  }
}

async function saveBase() {
  await baseRef.value.validate(async (ok) => {
    if (!ok) return
    savingBase.value = true
    try {
      await userApi.updateProfile({
        realName: form.realName,
        nickname: form.nickname,
        phone: form.phone,
        email: form.email,
        gender: form.gender,
        avatar: form.avatar
      })
      ElMessage.success('基本信息已保存')
    } finally {
      savingBase.value = false
    }
  })
}

async function saveArchive() {
  await archiveRef.value.validate(async (ok) => {
    if (!ok) return
    savingArchive.value = true
    try {
      await userApi.updateStudentProfile({
        idCard: form.idCard,
        address: form.address,
        emergencyContact: form.emergencyContact,
        emergencyPhone: form.emergencyPhone,
        education: form.education
      })
      ElMessage.success('档案信息已保存')
    } finally {
      savingArchive.value = false
    }
  })
}

onMounted(load)
</script>

<style scoped lang="scss">
.profile-side {
  background: #fff;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  overflow: hidden;
}
.side-banner { height: 92px; }
.side-body {
  padding: 0 22px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.side-avatar {
  margin-top: -46px;
  border: 4px solid #fff;
  box-shadow: 0 4px 14px rgba(16, 78, 54, .18);
  font-size: 34px;
  font-weight: 700;
  background: var(--brand-gradient);
  color: #fff;
}
.side-name { font-size: 19px; font-weight: 700; color: #1d2b24; margin-top: 12px; }
.side-username { font-size: 13px; color: #9bb0a5; margin-top: 2px; }
.side-upload { width: 100%; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.upload-label { font-size: 13px; color: #8a9b92; font-weight: 600; align-self: flex-start; }
.side-meta { width: 100%; margin-top: 18px; }
.meta-row {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 0; border-top: 1px dashed #eef3f0; font-size: 13px; color: #6a7c72;
  span { flex: 1; text-align: left; }
  b { color: #1d2b24; font-weight: 600; }
  .el-icon { color: var(--brand-primary); }
}
.form-actions { text-align: right; margin-top: 4px; }
</style>
