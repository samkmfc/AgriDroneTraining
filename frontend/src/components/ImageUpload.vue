<template>
  <el-upload
    class="image-uploader"
    :show-file-list="false"
    :action="action"
    :headers="headers"
    name="file"
    accept="image/*"
    :on-success="onSuccess"
    :before-upload="beforeUpload"
  >
    <div v-if="modelValue" class="img-wrap">
      <img :src="modelValue" class="preview" />
      <div class="img-mask"><el-icon><Edit /></el-icon></div>
    </div>
    <div v-else class="placeholder">
      <el-icon><Plus /></el-icon>
      <span>上传图片</span>
    </div>
  </el-upload>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { fileApi } from '@/api'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

const store = useStore()
const action = fileApi.uploadUrl
const headers = computed(() => ({ Authorization: 'Bearer ' + store.state.token }))

function beforeUpload(file) {
  const ok = file.size / 1024 / 1024 < 10
  if (!ok) ElMessage.error('图片不能超过 10MB')
  return ok
}
function onSuccess(res) {
  if (res.code === 200) {
    emit('update:modelValue', res.data.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}
</script>

<style scoped lang="scss">
.image-uploader :deep(.el-upload) {
  border: 1px dashed #c5d6cd;
  border-radius: 10px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color .2s;
  &:hover { border-color: var(--brand-primary); }
}
.placeholder, .img-wrap { width: 130px; height: 130px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.placeholder { color: #9bb0a5; gap: 8px; font-size: 13px; .el-icon { font-size: 26px; } }
.img-wrap { position: relative; }
.preview { width: 130px; height: 130px; object-fit: cover; }
.img-mask {
  position: absolute; inset: 0; background: rgba(0,0,0,.4); color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 22px;
  opacity: 0; transition: opacity .2s;
}
.img-wrap:hover .img-mask { opacity: 1; }
</style>
