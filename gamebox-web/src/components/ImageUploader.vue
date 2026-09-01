<template>
  <el-upload
    list-type="picture-card"
    :show-file-list="false"
    accept="image/jpeg,image/png,image/gif,image/webp"
    :http-request="customUpload"
  >
    <template v-if="!modelValue">
      <el-icon :size="22"><Plus /></el-icon>
      <div class="upload-text">{{ text }}</div>
    </template>
    <template v-else>
      <div class="preview-wrap">
        <img :src="modelValue" class="preview-img" />
        <div class="preview-mask">
          <el-icon @click.stop="$emit('update:modelValue', '')"><Delete /></el-icon>
        </div>
      </div>
    </template>
  </el-upload>

  <el-dialog
    v-model="cropVisible"
    title="裁剪头像"
    width="500px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="crop-box">
      <VueCropper
        v-if="cropImg"
        ref="cropperRef"
        :img="cropImg"
        :auto-crop="true"
        :auto-crop-width="240"
        :auto-crop-height="240"
        :fixed="true"
        :fixed-number="[1, 1]"
        :center-box="true"
        :can-move-box="true"
        :can-scale="true"
        output-type="jpeg"
      />
    </div>
    <p class="crop-tip">拖动选框选择区域，滚轮缩放图片</p>
    <template #footer>
      <el-button @click="cancelCrop">取消</el-button>
      <el-button type="primary" :loading="cropUploading" @click="confirmCrop">确定上传</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'
import { uploadFile } from '@/api/upload'

const props = defineProps({
  modelValue: { type: String, default: '' },
  type: { type: String, default: 'cover' },
  text: { type: String, default: '上传图片' },
  crop: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const cropVisible = ref(false)
const cropImg = ref('')
const cropUploading = ref(false)
const cropperRef = ref(null)

async function customUpload({ file }) {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片不能超过 10MB')
    return
  }
  if (!props.crop) {
    await doUpload(file)
    return
  }
  try {
    cropImg.value = await readAsDataURL(file)
    cropVisible.value = true
  } catch {
    ElMessage.error('图片读取失败，请重试')
  }
}

function readAsDataURL(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

async function doUpload(file) {
  try {
    const data = await uploadFile(file, props.type)
    emit('update:modelValue', data.url)
    ElMessage.success('上传成功')
  } catch {
    // 错误提示已由 request 拦截器统一处理
  }
}

function confirmCrop() {
  cropperRef.value.getCropBlob((blob) => {
    if (!blob) {
      ElMessage.error('裁剪失败，请重试')
      return
    }
    uploadCropped(blob)
  })
}

async function uploadCropped(blob) {
  cropUploading.value = true
  try {
    const file = new File([blob], `avatar-${Date.now()}.jpg`, { type: blob.type || 'image/jpeg' })
    const data = await uploadFile(file, props.type)
    emit('update:modelValue', data.url)
    cropVisible.value = false
    cropImg.value = ''
  } catch {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    cropUploading.value = false
  }
}

function cancelCrop() {
  cropVisible.value = false
  cropImg.value = ''
}
</script>

<style scoped>
.upload-text {
  font-size: 12px;
  color: var(--gb-text-muted);
  margin-top: 4px;
}

.preview-wrap {
  position: relative;
  width: 100%;
  height: 100%;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
  font-size: 18px;
}

.preview-wrap:hover .preview-mask {
  opacity: 1;
}

.crop-box {
  width: 100%;
  height: 360px;
  background: #16202b;
}

.crop-tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--gb-text-muted);
  text-align: center;
}
</style>
