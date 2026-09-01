<template>
  <div class="empty-holder">
    <el-icon :size="40"><Box /></el-icon>
    <p>{{ text }}</p>
  </div>
</template>

<script setup>
import { Box } from '@element-plus/icons-vue'

defineProps({
  text: { type: String, default: '暂无数据' }
})
</script>

<style scoped>
.empty-holder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
</style>
