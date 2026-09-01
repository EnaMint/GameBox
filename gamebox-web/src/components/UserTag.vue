<template>
  <span class="user-tag" :class="{ clickable: user?.id }" @click.stop="goUser">
    <el-avatar :size="size" :src="avatarUrl || undefined">
      {{ fallbackText }}
    </el-avatar>
    <span class="user-tag-name" :style="{ fontSize: size * 0.4 + 'px' }">{{ user?.nickname || '未知用户' }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  user: { type: Object, default: null },
  size: { type: Number, default: 24 }
})

const router = useRouter()

const avatarUrl = computed(() => props.user?.avatar || '')
const fallbackText = computed(() => (props.user?.nickname || '?').slice(0, 1).toUpperCase())

function goUser() {
  if (props.user?.id) {
    router.push(`/user/${props.user.id}`)
  }
}
</script>

<style scoped>
.user-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--gb-text-muted);
}

.user-tag.clickable {
  cursor: pointer;
}

.user-tag.clickable:hover .user-tag-name {
  color: var(--gb-accent);
}
</style>
