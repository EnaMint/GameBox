<template>
  <div class="back-bar">
    <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const props = defineProps({
  fallback: { type: String, default: '/' }
})
const router = useRouter()

function goBack() {
  if (window.history.state?.back) router.back()
  else router.push(props.fallback)
}
</script>

<style scoped>
.back-bar {
  margin-bottom: 12px;
}

.back-bar .el-button {
  color: var(--gb-text-muted);
  padding: 4px 8px;
}

.back-bar .el-button:hover {
  color: var(--gb-accent);
}
</style>
