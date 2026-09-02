<template>
  <div class="page-container">
    <div class="page-head">
      <h2 class="section-title">攻略广场</h2>
      <el-button type="primary" :icon="EditPen" @click="goCreate">发布攻略</el-button>
    </div>

    <div class="filter-bar gb-card">
      <div class="filter-row">
        <span class="filter-label gb-muted">分类</span>
        <el-radio-group v-model="filter.category" @change="onFilterChange">
          <el-radio-button v-for="c in categories" :key="c" :value="c">{{ c === '' ? '全部' : c }}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-row">
        <span class="filter-label gb-muted">筛选</span>
        <el-select
          v-model="filter.gameId"
          filterable
          clearable
          placeholder="全部游戏"
          class="game-select"
          @change="onFilterChange"
        >
          <el-option v-for="g in gameOptions" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
        <el-input
          v-model="filter.keyword"
          placeholder="搜索攻略关键词"
          clearable
          class="keyword-input"
          :prefix-icon="Search"
          @keyup.enter="onFilterChange"
          @clear="onFilterChange"
        />
        <el-button type="primary" :icon="Search" @click="onFilterChange">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <div class="sort-wrap">
          <span class="filter-label gb-muted">排序</span>
          <el-radio-group v-model="filter.sort" @change="onFilterChange">
            <el-radio-button value="new">最新</el-radio-button>
            <el-radio-button value="hot">最热</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="list-wrap">
      <template v-if="list.length">
        <div class="strategy-list">
          <StrategyCard v-for="item in list" :key="item.id" :item="item" />
        </div>
      </template>
      <EmptyTip v-else-if="!loading" text="没有找到相关攻略，换个条件试试" />
    </div>

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, EditPen } from '@element-plus/icons-vue'
import { getStrategyList } from '@/api/strategy'
import { getGameList } from '@/api/game'
import { useUserStore } from '@/stores/user'
import StrategyCard from '@/components/StrategyCard.vue'
import EmptyTip from '@/components/EmptyTip.vue'

const router = useRouter()
const userStore = useUserStore()

const categories = ['', '攻略', '评测', '资讯', '心得', '其他']
const pageSize = 10

const filter = reactive({ category: '', gameId: null, keyword: '', sort: 'new' })
const page = ref(1)
const total = ref(0)
const list = ref([])
const loading = ref(false)
const gameOptions = ref([])

async function loadGames() {
  try {
    const data = await getGameList({ size: 100 })
    gameOptions.value = data.records || []
  } catch {
    // 错误提示已由 request 拦截器统一处理
  }
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize, sort: filter.sort }
    if (filter.category) params.category = filter.category
    if (filter.gameId) params.gameId = filter.gameId
    if (filter.keyword) params.keyword = filter.keyword
    const data = await getStrategyList(params)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  page.value = 1
  load()
}

function resetFilters() {
  filter.category = ''
  filter.gameId = null
  filter.keyword = ''
  filter.sort = 'new'
  onFilterChange()
}

function onPageChange(p) {
  page.value = p
  load()
}

function goCreate() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push('/strategy/create')
}

onMounted(() => {
  loadGames()
  load()
})
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-bar {
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-label {
  font-size: 13px;
  width: 34px;
  flex-shrink: 0;
}

.game-select {
  width: 220px;
}

.keyword-input {
  width: 240px;
}

.sort-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.strategy-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-wrap {
  min-height: 200px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
