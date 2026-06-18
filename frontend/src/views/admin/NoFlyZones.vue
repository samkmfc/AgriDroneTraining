<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title"><span class="bar"></span>禁飞区管理</div>
        <div class="page-subtitle">无人机禁飞 / 限飞区域维护</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增禁飞区</el-button>
    </div>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索区域名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="name" label="区域名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="province" label="省份" width="110" />
        <el-table-column prop="city" label="城市" width="120" />
        <el-table-column prop="longitude" label="经度" width="110" />
        <el-table-column prop="latitude" label="纬度" width="110" />
        <el-table-column label="半径(米)" width="110">
          <template #default="{ row }">{{ row.radius }}</template>
        </el-table-column>
        <el-table-column label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level === 'STRICT' ? 'danger' : 'warning'" effect="light">
              {{ row.level === 'STRICT' ? '禁飞' : '限飞' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无禁飞区数据" /></template>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑禁飞区' : '新增禁飞区'"
      width="760px"
      :close-on-click-modal="false"
      @opened="initPicker"
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="区域名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入区域名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级" prop="level">
              <el-select v-model="form.level" placeholder="请选择等级" style="width: 100%">
                <el-option label="禁飞" value="STRICT" />
                <el-option label="限飞" value="LIMIT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="省份" prop="province">
              <el-input v-model="form.province" placeholder="请输入省份" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="请输入城市" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="半径(米)" prop="radius">
              <el-input-number v-model="form.radius" :min="0" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="区域说明" />
        </el-form-item>
        <el-form-item label="地图选点">
          <div class="picker-tip">
            <el-icon><LocationInformation /></el-icon>
            在地图上<b>点击</b>或<b>拖动标记</b>即可设置经纬度，圆圈实时预览禁飞 / 限飞范围
          </div>
          <div ref="pickerEl" class="picker-map"></div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete, LocationInformation } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noFlyZoneApi } from '@/api'
import { loadAMap, ZONE_COLOR } from '@/utils/amap'
import { DEFAULT_CENTER } from '@/config/map'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const defaultForm = () => ({
  name: '', province: '', city: '',
  longitude: undefined, latitude: undefined, radius: 1000,
  level: 'STRICT', description: ''
})
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择等级', trigger: 'change' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  radius: [{ required: true, message: '请输入半径', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await noFlyZoneApi.adminList({
      pageNum: query.pageNum, pageSize: query.pageSize, keyword: query.keyword || undefined
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
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
}
function openCreate() {
  resetForm()
  dialogVisible.value = true
}
function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, defaultForm(), {
    name: row.name, province: row.province, city: row.city,
    longitude: row.longitude, latitude: row.latitude, radius: row.radius,
    level: row.level, description: row.description
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await noFlyZoneApi.adminUpdate(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await noFlyZoneApi.adminCreate(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除禁飞区「${row.name}」吗？`, '删除确认', {
    type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
  }).then(async () => {
    await noFlyZoneApi.adminDelete(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) query.pageNum--
    loadList()
  }).catch(() => {})
}

/* ============ 高德地图选点 ============ */
const pickerEl = ref()
let AMap = null
let pickerMap = null
let pickerMarker = null
let pickerCircle = null

async function initPicker() {
  try {
    AMap = await loadAMap()
    const hasPos = form.longitude && form.latitude
    const center = hasPos ? [Number(form.longitude), Number(form.latitude)] : DEFAULT_CENTER
    pickerMap = new AMap.Map(pickerEl.value, {
      zoom: hasPos ? 12 : 9,
      center,
      mapStyle: 'amap://styles/whitesmoke'
    })
    pickerMap.addControl(new AMap.ToolBar({ position: { top: '10px', right: '10px' } }))
    if (hasPos) placeMarker(center)
    // 点击地图设置坐标
    pickerMap.on('click', (e) => {
      const lnglat = [Number(e.lnglat.getLng().toFixed(6)), Number(e.lnglat.getLat().toFixed(6))]
      form.longitude = lnglat[0]
      form.latitude = lnglat[1]
      placeMarker(lnglat)
    })
  } catch (e) {
    ElMessage.warning('地图加载失败，可手动填写经纬度：' + (e?.message || e))
  }
}

function placeMarker(center) {
  if (!pickerMap || !AMap) return
  const color = ZONE_COLOR[form.level] || '#e8504f'
  if (!pickerMarker) {
    pickerMarker = new AMap.Marker({ position: center, draggable: true, cursor: 'move' })
    pickerMarker.on('dragend', (e) => {
      const p = e.lnglat
      form.longitude = Number(p.getLng().toFixed(6))
      form.latitude = Number(p.getLat().toFixed(6))
      updateCircle()
    })
    pickerMap.add(pickerMarker)
  } else {
    pickerMarker.setPosition(center)
  }
  updateCircle()
  pickerMap.setCenter(center)
}

function updateCircle() {
  if (!pickerMap || !AMap || !form.longitude || !form.latitude) return
  const color = ZONE_COLOR[form.level] || '#e8504f'
  const center = [Number(form.longitude), Number(form.latitude)]
  if (!pickerCircle) {
    pickerCircle = new AMap.Circle({
      center, radius: Number(form.radius) || 1000,
      strokeColor: color, strokeWeight: 2, fillColor: color, fillOpacity: 0.22
    })
    pickerMap.add(pickerCircle)
  } else {
    pickerCircle.setCenter(center)
    pickerCircle.setRadius(Number(form.radius) || 1000)
    pickerCircle.setOptions({ strokeColor: color, fillColor: color })
  }
}

// 半径 / 级别变化时更新圆形预览
watch(() => [form.radius, form.level], updateCircle)

function onDialogClosed() {
  if (pickerCircle) { pickerMap.remove(pickerCircle); pickerCircle = null }
  if (pickerMarker) { pickerMap.remove(pickerMarker); pickerMarker = null }
  if (pickerMap) { pickerMap.destroy(); pickerMap = null }
  resetForm()
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.picker-tip {
  display: flex; align-items: center; gap: 6px;
  font-size: 12.5px; color: #8a9b92; margin-bottom: 8px; width: 100%;
  b { color: var(--brand-primary); }
}
.picker-map { width: 100%; height: 300px; border-radius: 10px; overflow: hidden; }
</style>
