<template>
  <div class="team-card gb-card" @click="$router.push(`/team/${item.id}`)">
    <div class="team-head">
      <span class="gb-tag-game" v-if="item.game?.name">{{ item.game.name }}</span>
      <el-tag size="small" :type="statusType" effect="plain">{{ statusText }}</el-tag>
    </div>
    <h3 class="team-title">{{ item.title }}</h3>
    <div class="team-meta gb-muted">
      <span class="meta-item">
        <el-icon><User /></el-icon>
        {{ item.memberCount }}/{{ item.memberLimit }} 人
      </span>
      <span class="meta-item" v-if="item.needVoice">
        <el-icon><Microphone /></el-icon>
        需要开麦
      </span>
      <span class="meta-item" v-if="item.playTime">
        <el-icon><Clock /></el-icon>
        {{ item.playTime }}
      </span>
    </div>
    <div class="team-footer">
      <UserTag :user="item.leader" :size="22" />
      <span class="gb-muted team-time">{{ relativeTime(item.createdAt) }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { User, Microphone, Clock } from '@element-plus/icons-vue'
import UserTag from './UserTag.vue'
import { relativeTime } from '@/utils/format'

const props = defineProps({
  item: { type: Object, required: true }
})

const statusMap = { 1: ['招募中', 'success'], 2: ['已满员', 'info'], 3: ['已关闭', 'danger'] }
const statusText = computed(() => statusMap[props.item.status]?.[0] || '未知')
const statusType = computed(() => statusMap[props.item.status]?.[1] || 'info')
</script>

<style scoped>
.team-card {
  padding: 16px;
  cursor: pointer;
}

.team-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.team-title {
  font-size: 16px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.team-card:hover .team-title {
  color: var(--gb-accent);
}

.team-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  margin-bottom: 12px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.team-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.team-time {
  font-size: 12px;
}
</style>
