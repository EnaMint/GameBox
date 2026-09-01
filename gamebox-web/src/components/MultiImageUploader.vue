<template>
  <el-upload
    list-type="picture-card"
    :show-file-list="false"
    multiple
    accept="image/jpeg,image/png,image/gif,image/webp"
    :http-request="customUpload"
  >
    <template v-if="modelValue.length < limit">
      <el-icon :size="22"><Plus /></el-icon>
      <div class="upload-text">{{ modelValue.length }}/{{ limit }}</div>
    </template>
  </el-upload>

  <div v-if="modelValue.length" class="image-list">
    <div v-for="(url, index) in modelValue" :key="url" class="image-item">
      <img :src="url" />
      <span class="remove-btn" @click="removeAt(index)">
        <el-icon><Close /></el-icon>
      </span>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/upload'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  type: { type: String, default: 'record' },
  limit: { type: Number, default: 9 }
})
const emit = defineEmits(['update:modelValue'])

async function customUpload({ file }) {
  if (props.modelValue.length >= props.limit) {
    ElMessage.warning(`最多上传 ${props.limit} 张图片`)
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片不能超过 10MB')
    return
  }
  try {
    const data = await uploadFile(file, props.type)
    emit('update:modelValue', [...props.modelValue, data.url])
  } catch {
    // 错误提示已由 request 拦截器统一处理
  }
}

function removeAt(index) {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}
</script>

<style scoped>
.upload-text {
  font-size: 12px;
  color: var(--gb-text-muted);
  margin-top: 4px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.image-item {
  position: relative;
  width: 104px;
  height: 104px;
  border-radius: 4px;
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
}
</style>
