<template>
  <div class="page-container">
    <BackBar :fallback="`/user/${userId}`" />
    <div class="follow-card gb-card">
      <h3 class="follow-title">{{ isFans ? '粉丝列表' : '关注列表' }}</h3>
      <div v-loading="loading" class="follow-body">
        <div v-if="list.length" class="follow-list">
          <div v-for="item in list" :key="item.id" class="follow-row">
            <div class="follow-user" @click="$router.push(`/user/${item.id}`)">
              <el-avatar :size="44" :src="item.avatar || undefined">
                {{ (item.nickname || '?').slice(0, 1).toUpperCase() }}
              </el-avatar>
              <div class="follow-info">
                <div class="follow-name">{{ item.nickname }}</div>
                <div class="follow-bio gb-muted">{{ item.bio || '这个人很懒，什么都没写～' }}</div>
              </div>
            </div>
            <el-button
              v-if="canOperate(item)"
              size="small"
              round
              :type="item.followed ? 'default' : 'primary'"
              :loading="item._saving"
              @click="toggleFollow(item)"
            >
              {{ item.followed ? '已关注' : (isFans ? '回关' : '关注') }}
            </el-button>
          </div>
        </div>
        <EmptyTip v-else-if="!loading" :text="isFans ? '还没有粉丝' : '还没有关注任何人'" />
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFollows, getFans, followUser, unfollowUser } from '@/api/follow'
import { useUserStore } from '@/stores/user'
import BackBar from '@/components/BackBar.vue'
import EmptyTip from '@/components/EmptyTip.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageSize = 20
const userId = computed(() => route.params.id)
const isFans = computed(() => route.meta.followType === 'fans')

const list = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const fetchFn = isFans.value ? getFans : getFollows
    const data = await fetchFn(userId.value, { page: page.value, size: pageSize })
    list.value = data.records || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function canOperate(item) {
  return userStore.isLoggedIn && String(item.id) !== String(userStore.userInfo?.userId)
}

async function toggleFollow(item) {
  if (!userStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  item._saving = true
  try {
    if (item.followed) {
      await unfollowUser(item.id)
      item.followed = false
    } else {
      await followUser(item.id)
      item.followed = true
    }
  } catch {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    item._saving = false
  }
}

function onPageChange(p) {
  page.value = p
  loadList()
}

watch(
  () => [route.params.id, route.meta.followType],
  () => {
    page.value = 1
    loadList()
  }
)

onMounted(loadList)
</script>

<style scoped>
.follow-card {
  padding: 20px 24px 24px;
}

.follow-title {
  font-size: 17px;
  margin-bottom: 16px;
}

.follow-body {
  min-height: 200px;
}

.follow-list {
  display: flex;
  flex-direction: column;
}

.follow-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 4px;
  border-bottom: 1px solid var(--gb-border);
}

.follow-row:last-child {
  border-bottom: none;
}

.follow-user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.follow-info {
  min-width: 0;
}

.follow-name {
  font-weight: 600;
  margin-bottom: 2px;
}

.follow-user:hover .follow-name {
  color: var(--gb-accent);
}

.follow-bio {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 520px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
