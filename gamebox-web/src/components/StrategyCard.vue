<template>
  <div class="strategy-card gb-card" @click="$router.push(`/strategy/${item.id}`)">
    <div class="strategy-cover cover-fallback">
      <img v-if="item.cover" :src="item.cover" :alt="item.title" />
      <span v-else>{{ (item.game?.name || '攻略').slice(0, 6) }}</span>
    </div>
    <div class="strategy-body">
      <div class="strategy-tags">
        <span class="gb-tag-game" v-if="item.game?.name">{{ item.game.name }}</span>
        <el-tag size="small" :type="categoryTagType" effect="plain">{{ item.category }}</el-tag>
      </div>
      <h3 class="strategy-title">{{ item.title }}</h3>
      <p class="strategy-summary gb-muted">{{ item.summary || '作者很懒，没有写摘要...' }}</p>
      <div class="strategy-footer">
        <UserTag :user="item.author" :size="22" />
        <div class="strategy-stats gb-muted">
          <span><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
          <span><el-icon><Star /></el-icon>{{ item.likeCount || 0 }}</span>
          <span><el-icon><ChatDotRound /></el-icon>{{ item.commentCount || 0 }}</span>
          <span class="strategy-time">{{ relativeTime(item.createdAt) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { View, Star, ChatDotRound } from '@element-plus/icons-vue'
import UserTag from './UserTag.vue'
import { relativeTime } from '@/utils/format'

const props = defineProps({
  item: { type: Object, required: true }
})

const categoryTagType = computed(() => {
  const map = { 攻略: 'primary', 评测: 'success', 资讯: 'info', 心得: 'warning' }
  return map[props.item.category] || 'info'
})
</script>

<style scoped>
.strategy-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  cursor: pointer;
}

.strategy-cover {
  flex-shrink: 0;
  width: 160px;
  height: 96px;
  border-radius: 4px;
  overflow: hidden;
}

.strategy-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.strategy-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.strategy-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 6px;
}

.strategy-title {
  font-size: 16px;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.strategy-card:hover .strategy-title {
  color: var(--gb-accent);
}

.strategy-summary {
  font-size: 13px;
  line-height: 1.5;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.strategy-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.strategy-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.strategy-stats span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
</style>
