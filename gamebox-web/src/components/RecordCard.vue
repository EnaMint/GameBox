<template>
  <div class="record-card gb-card">
    <div class="record-head">
      <UserTag :user="item.user" :size="32" />
      <div class="record-head-info">
        <span class="gb-tag-game" v-if="item.game?.name">{{ item.game.name }}</span>
        <span class="gb-muted record-time">{{ relativeTime(item.createdAt) }}</span>
      </div>
      <el-button
        v-if="showDelete"
        type="danger"
        size="small"
        text
        :icon="Delete"
        @click="$emit('delete', item)"
      >删除</el-button>
    </div>

    <p v-if="item.content" class="record-content">{{ item.content }}</p>

    <div v-if="item.images?.length" class="record-images" :class="gridClass">
      <el-image
        v-for="(url, index) in item.images"
        :key="index"
        :src="url"
        :preview-src-list="item.images"
        :initial-index="index"
        fit="cover"
        class="record-img"
      />
    </div>

    <div class="record-actions">
      <span class="like-btn" :class="{ liked: item.liked }" @click="$emit('like', item)">
        <el-icon><Star /></el-icon>
        {{ item.likeCount || 0 }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Star, Delete } from '@element-plus/icons-vue'
import UserTag from './UserTag.vue'
import { relativeTime } from '@/utils/format'

const props = defineProps({
  item: { type: Object, required: true },
  showDelete: { type: Boolean, default: false }
})
defineEmits(['like', 'delete'])

const gridClass = computed(() => {
  const n = props.item.images?.length || 0
  if (n === 1) return 'single'
  return 'multi'
})
</script>

<style scoped>
.record-card {
  padding: 18px 20px;
}

.record-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.record-head-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-time {
  font-size: 12px;
}

.record-content {
  line-height: 1.7;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.record-images {
  display: grid;
  gap: 6px;
  margin-bottom: 12px;
}

.record-images.single {
  grid-template-columns: 1fr;
}

.record-images.multi {
  grid-template-columns: repeat(3, 1fr);
}

.record-images.single .record-img {
  max-width: 420px;
  max-height: 300px;
}

.record-img {
  width: 100%;
  height: 150px;
  border-radius: 4px;
  overflow: hidden;
}

.record-images.single .record-img {
  height: auto;
}

.record-actions {
  display: flex;
  gap: 16px;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--gb-text-muted);
  cursor: pointer;
  font-size: 14px;
  transition: color 0.2s;
}

.like-btn:hover,
.like-btn.liked {
  color: var(--gb-orange);
}
</style>
