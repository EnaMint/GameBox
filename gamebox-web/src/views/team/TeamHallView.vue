<template>
  <div class="page-container">
    <div class="hall-toolbar">
      <el-select
        v-model="filterGameId"
        class="game-select"
        placeholder="筛选游戏"
        filterable
        clearable
        @change="resetAndLoad"
      >
        <el-option v-for="game in gameOptions" :key="game.id" :label="game.name" :value="game.id" />
      </el-select>

      <el-select v-model="filterStatus" class="status-select" @change="resetAndLoad">
        <el-option label="全部状态" value="" />
        <el-option label="招募中" :value="1" />
        <el-option label="已满员" :value="2" />
        <el-option label="已关闭" :value="3" />
      </el-select>

      <el-button @click="resetFilters">重置</el-button>

      <el-button class="create-btn" type="primary" :icon="Plus" @click="router.push('/team/create')">发布组队</el-button>
    </div>

    <div v-loading="loading" class="team-grid">
      <TeamCard v-for="item in list" :key="item.id" :item="item" />
    </div>
    <EmptyTip v-if="!list.length && !loading" text="暂无组队帖，点击右上角发布一个吧" />

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
import { Plus } from '@element-plus/icons-vue'
import { getGameList } from '@/api/game'
import { getTeamList } from '@/api/team'
import TeamCard from '@/components/TeamCard.vue'
import EmptyTip from '@/components/EmptyTip.vue'

const router = useRouter()

const gameOptions = ref([])
const filterGameId = ref('')
const filterStatus = ref(1)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

async function loadGameOptions() {
  const data = await getGameList({ page: 1, size: 100 })
  gameOptions.value = data.records || []
}

async function loadList() {
  loading.value = true
  try {
    const data = await getTeamList({
      gameId: filterGameId.value || undefined,
      status: filterStatus.value || undefined,
      page: page.value,
      size
    })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function resetAndLoad() {
  page.value = 1
  loadList()
}

function resetFilters() {
  filterGameId.value = ''
  filterStatus.value = 1
  resetAndLoad()
}

onMounted(() => {
  loadGameOptions()
  loadList()
})
</script>

<style scoped>
.hall-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.game-select {
  width: 240px;
}

.status-select {
  width: 140px;
}

.create-btn {
  margin-left: auto;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  min-height: 120px;
}

.pager {
  margin-top: 24px;
  justify-content: center;
}

@media (max-width: 800px) {
  .team-grid {
    grid-template-columns: 1fr;
  }
}
</style>
