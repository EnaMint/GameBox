<template>
  <div class="page-container">
    <div class="library-toolbar">
      <el-input
        v-model="keyword"
        class="search-input"
        placeholder="搜索游戏名称"
        clearable
        :prefix-icon="Search"
        @keyup.enter="resetAndLoad"
        @clear="resetAndLoad"
      />
      <el-select v-model="tag" class="genre-select" placeholder="类型" clearable filterable @change="resetAndLoad">
        <el-option label="全部" value="" />
        <el-option v-for="t in tagOptions" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div v-loading="loading" class="game-grid">
      <div v-for="game in list" :key="game.id" class="game-card gb-card">
        <div class="game-cover cover-fallback">
          <img v-if="game.cover" :src="game.cover" :alt="game.name" />
          <span v-else>{{ game.name.slice(0, 4) }}</span>
        </div>
        <div class="game-body">
          <div class="game-name" :title="game.name">{{ game.name }}</div>
          <div class="game-meta">
            <span class="gb-tag-game">{{ game.genre || '未知类型' }}</span>
            <span class="gb-muted game-platform">{{ game.platform || '未知平台' }}</span>
          </div>
          <div v-if="tagsOf(game).length" class="game-tags">
            <span
              v-for="t in tagsOf(game).slice(0, 4)"
              :key="t"
              class="gb-tag-game tag-chip"
              :class="{ active: tag === t }"
              @click="pickTag(t)"
            >{{ t }}</span>
            <span v-if="tagsOf(game).length > 4" class="gb-muted tag-more">+{{ tagsOf(game).length - 4 }}</span>
          </div>
          <div class="game-actions">
            <template v-if="userStore.isLoggedIn">
              <el-tag v-if="myMap.has(game.id)" size="small" type="success" effect="plain" class="added-tag">已收录</el-tag>
              <div class="status-btns">
                <el-button
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  size="small"
                  :type="currentStatus(game.id) === opt.value ? 'primary' : 'default'"
                  :disabled="savingId === game.id"
                  @click="handleStatus(game, opt.value)"
                >{{ opt.label }}</el-button>
              </div>
            </template>
            <template v-else>
              <div class="status-btns">
                <el-button
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  size="small"
                  @click="router.push('/login')"
                >{{ opt.label }}</el-button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
    <EmptyTip v-if="!list.length && !loading" text="暂无游戏，换个条件试试" />

    <el-pagination
      v-if="total > 0"
      v-model:current-page="page"
      class="pager"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="size"
      @current-change="loadList"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getGameList, getGameTags } from '@/api/game'
import { getMyGames, addGame, updateGame } from '@/api/ugame'
import { useUserStore } from '@/stores/user'
import EmptyTip from '@/components/EmptyTip.vue'

const router = useRouter()
const userStore = useUserStore()

const tagOptions = ref([])
const statusOptions = [
  { label: '想玩', value: 1 },
  { label: '在玩', value: 2 },
  { label: '已通关', value: 3 }
]

const keyword = ref('')
const tag = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 12
const loading = ref(false)
const myMap = ref(new Map())
const savingId = ref(null)

async function loadList() {
  loading.value = true
  try {
    const data = await getGameList({ keyword: keyword.value, tag: tag.value, page: page.value, size })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    tagOptions.value = (await getGameTags()) || []
  } catch {
    tagOptions.value = []
  }
}

function tagsOf(game) {
  return (game.tags || '').split(',').map((t) => t.trim()).filter(Boolean)
}

function pickTag(t) {
  tag.value = tag.value === t ? '' : t
  resetAndLoad()
}

async function loadMyMap() {
  if (!userStore.isLoggedIn) {
    myMap.value = new Map()
    return
  }
  try {
    const data = await getMyGames({ page: 1, size: 200 })
    myMap.value = new Map((data.records || []).map((r) => [r.gameId, r]))
  } catch {
    myMap.value = new Map()
  }
}

function resetAndLoad() {
  page.value = 1
  loadList()
}

function resetFilters() {
  keyword.value = ''
  tag.value = ''
  resetAndLoad()
}

function currentStatus(gameId) {
  return myMap.value.get(gameId)?.status
}

async function handleStatus(game, status) {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const record = myMap.value.get(game.id)
  if (record && record.status === status) return
  savingId.value = game.id
  try {
    if (record) {
      await updateGame(record.id, {
        status,
        playHours: record.playHours,
        rating: record.rating,
        remark: record.remark
      })
    } else {
      await addGame({ gameId: game.id, status })
    }
    ElMessage.success(record ? '状态已更新' : '已收录到游戏库')
    await loadMyMap()
  } finally {
    savingId.value = null
  }
}

onMounted(() => {
  loadTags()
  loadList()
  loadMyMap()
})
</script>

<style scoped>
.library-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input {
  width: 320px;
}

.genre-select {
  width: 160px;
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 120px;
}

.game-card {
  overflow: hidden;
  border: 1px solid var(--gb-border);
}

.game-cover {
  height: 120px;
}

.game-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.game-body {
  padding: 12px;
}

.game-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.game-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.game-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin: -4px 0 12px;
}

.tag-chip {
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s;
}

.tag-chip:hover {
  background-color: rgba(102, 192, 244, 0.32);
}

.tag-chip.active {
  background-color: var(--gb-accent);
  color: #101821;
}

.tag-more {
  font-size: 12px;
}

.game-platform {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.game-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.added-tag {
  flex-shrink: 0;
}

.status-btns {
  display: flex;
  gap: 6px;
}

.status-btns .el-button {
  padding: 5px 8px;
}

.pager {
  margin-top: 24px;
  justify-content: center;
}

@media (max-width: 1000px) {
  .game-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 720px) {
  .game-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
