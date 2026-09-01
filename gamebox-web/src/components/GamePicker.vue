<template>
  <el-dialog v-model="visibleProxy" title="选择游戏" width="640px" :close-on-click-modal="false">
    <el-input
      v-model="keyword"
      placeholder="搜索游戏，如：法环"
      clearable
      :prefix-icon="Search"
      @keyup.enter="search"
      @clear="search"
    >
      <template #append>
        <el-button :icon="Search" @click="search" />
      </template>
    </el-input>

    <div class="game-grid">
      <div
        v-for="game in games"
        :key="game.id"
        class="game-item gb-card"
        :class="{ selected: game.id === selectedId }"
        @click="selectedId = game.id"
      >
        <div class="game-cover cover-fallback">
          <img v-if="game.cover" :src="game.cover" :alt="game.name" />
          <span v-else>{{ game.name.slice(0, 4) }}</span>
        </div>
        <div class="game-name">{{ game.name }}</div>
        <div class="game-genre gb-muted">{{ game.genre || '未知类型' }}</div>
      </div>
    </div>
    <EmptyTip v-if="!games.length && !loading" text="没有找到相关游戏" />

    <template #footer>
      <el-button @click="visibleProxy = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedId" @click="confirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getGameList } from '@/api/game'
import EmptyTip from './EmptyTip.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  selectedId: { type: Number, default: null }
})
const emit = defineEmits(['update:modelValue', 'update:selectedId', 'select'])

const visibleProxy = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const keyword = ref('')
const games = ref([])
const loading = ref(false)
const selectedId = ref(props.selectedId)

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      selectedId.value = props.selectedId
      search()
    }
  }
)

async function search() {
  loading.value = true
  try {
    const page = await getGameList({ keyword: keyword.value, page: 1, size: 18 })
    games.value = page.records || []
  } finally {
    loading.value = false
  }
}

function confirm() {
  const game = games.value.find((g) => g.id === selectedId.value)
  if (game) {
    emit('select', game)
    visibleProxy.value = false
  }
}
</script>

<style scoped>
.game-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.game-item {
  padding: 10px;
  cursor: pointer;
  border: 2px solid transparent;
}

.game-item.selected {
  border-color: var(--gb-accent);
}

.game-cover {
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.game-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.game-name {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.game-genre {
  font-size: 12px;
}
</style>
