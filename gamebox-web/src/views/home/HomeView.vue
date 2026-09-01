<template>
  <div class="page-container">
    <div class="home-banner">
      <div class="banner-content">
        <div class="banner-logo">
          <el-icon :size="40"><GamepadIcon /></el-icon>
          <h1>GameBox</h1>
        </div>
        <p class="banner-slogan">GameBox 游戏攻略与玩家社区</p>
        <p class="banner-sub gb-muted">查攻略、找队友、晒战绩，一站式玩家聚集地</p>
        <div class="banner-entries">
          <div
            v-for="entry in entries"
            :key="entry.path"
            class="entry-item"
            @click="$router.push(entry.path)"
          >
            <el-icon :size="26"><component :is="entry.icon" /></el-icon>
            <div class="entry-info">
              <div class="entry-label">{{ entry.label }}</div>
              <div class="entry-desc">{{ entry.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="section-head">
      <h2 class="section-title">热门攻略</h2>
      <router-link to="/strategy" class="gb-link">查看全部 »</router-link>
    </div>

    <div v-loading="loading">
      <template v-if="hotList.length">
        <div class="strategy-list">
          <StrategyCard v-for="item in hotList" :key="item.id" :item="item" />
        </div>
      </template>
      <EmptyTip v-else-if="!loading" text="暂无热门攻略，快来发布第一篇吧" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Notebook, UserFilled, Medal } from '@element-plus/icons-vue'
import { GamepadIcon } from '@/components/icons'
import { getStrategyList } from '@/api/strategy'
import StrategyCard from '@/components/StrategyCard.vue'
import EmptyTip from '@/components/EmptyTip.vue'

const entries = [
  { icon: Notebook, label: '攻略广场', desc: '攻略 · 评测 · 资讯', path: '/strategy' },
  { icon: GamepadIcon, label: '游戏库', desc: '发现好游戏', path: '/games' },
  { icon: UserFilled, label: '组队大厅', desc: '找队友一起开黑', path: '/team' },
  { icon: Medal, label: '战绩动态', desc: '晒出高光时刻', path: '/record' }
]

const hotList = ref([])
const loading = ref(false)

async function loadHot() {
  loading.value = true
  try {
    const data = await getStrategyList({ sort: 'hot', page: 1, size: 8 })
    hotList.value = data.records || []
  } finally {
    loading.value = false
  }
}

onMounted(loadHot)
</script>

<style scoped>
.home-banner {
  border-radius: 10px;
  margin-bottom: 32px;
  padding: 48px 40px;
  background:
    radial-gradient(ellipse at 85% 0%, rgba(102, 192, 244, 0.28) 0%, transparent 55%),
    radial-gradient(ellipse at 8% 100%, rgba(163, 207, 6, 0.12) 0%, transparent 50%),
    linear-gradient(120deg, #2a475e 0%, #1b2838 55%, #171a21 100%);
  border: 1px solid var(--gb-border);
}

.banner-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--gb-accent);
}

.banner-logo h1 {
  font-size: 34px;
  letter-spacing: 1px;
}

.banner-slogan {
  margin-top: 14px;
  font-size: 22px;
  font-weight: 600;
}

.banner-sub {
  margin-top: 8px;
  font-size: 14px;
}

.banner-entries {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-top: 30px;
}

.entry-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-radius: var(--gb-radius);
  background-color: rgba(27, 40, 56, 0.55);
  border: 1px solid rgba(102, 192, 244, 0.18);
  cursor: pointer;
  transition: all 0.2s;
}

.entry-item:hover {
  background-color: rgba(102, 192, 244, 0.12);
  border-color: var(--gb-accent);
  transform: translateY(-2px);
}

.entry-item .el-icon {
  color: var(--gb-accent);
}

.entry-label {
  font-size: 15px;
  font-weight: 600;
}

.entry-desc {
  font-size: 12px;
  color: var(--gb-text-muted);
  margin-top: 2px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.strategy-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (max-width: 900px) {
  .banner-entries {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
