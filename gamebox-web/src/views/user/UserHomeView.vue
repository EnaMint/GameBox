<template>
  <div class="page-container">
    <div class="user-card gb-card" v-loading="loading">
      <template v-if="user">
        <el-avatar :size="84" :src="user.avatar || undefined">{{ avatarText }}</el-avatar>
        <div class="user-info">
          <h2 class="user-name">{{ user.nickname }}</h2>
          <p class="user-bio gb-muted">{{ user.bio || '这个人很懒，什么都没写～' }}</p>
          <p class="user-meta gb-muted">
            @{{ user.username }} · 加入于 {{ formatDateTime(user.createdAt) }}
            <span class="user-stats">
              <router-link :to="`/user/${user.id}/follows`"><b>{{ user.followCount || 0 }}</b> 关注</router-link>
              <router-link :to="`/user/${user.id}/fans`"><b>{{ user.fansCount || 0 }}</b> 粉丝</router-link>
            </span>
          </p>
        </div>
        <div v-if="!isSelf" class="user-actions">
          <el-button
            v-if="userStore.isLoggedIn"
            round
            :type="user.followed ? 'default' : 'primary'"
            :loading="followSaving"
            @click="toggleFollow"
          >
            {{ user.followed ? '已关注' : '+ 关注' }}
          </el-button>
          <el-button round @click="goChat">发私信</el-button>
        </div>
      </template>
    </div>

    <template v-if="user">
      <h3 class="section-title">他的战绩</h3>
      <div v-loading="recordLoading" class="list-wrap">
        <div v-if="records.length" class="record-list">
          <RecordCard v-for="item in records" :key="item.id" :item="item" @like="onLike" />
        </div>
        <EmptyTip v-else-if="!recordLoading" text="他还没有发布过战绩" />
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
    </template>
    <EmptyTip v-else-if="!loading" text="用户不存在或已被删除" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserById } from '@/api/user'
import { followUser, unfollowUser } from '@/api/follow'
import { getUserRecords, likeRecord } from '@/api/record'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import RecordCard from '@/components/RecordCard.vue'
import EmptyTip from '@/components/EmptyTip.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = route.params.id
const pageSize = 10

const user = ref(null)
const loading = ref(false)
const records = ref([])
const recordLoading = ref(false)
const page = ref(1)
const total = ref(0)
const followSaving = ref(false)

const avatarText = computed(() => (user.value?.nickname || '?').slice(0, 1).toUpperCase())
const isSelf = computed(() => String(userStore.userInfo?.userId ?? '') === String(userId))

async function toggleFollow() {
  if (!user.value) return
  followSaving.value = true
  try {
    if (user.value.followed) {
      await unfollowUser(user.value.id)
      user.value.followed = false
      user.value.fansCount = Math.max((user.value.fansCount || 1) - 1, 0)
    } else {
      await followUser(user.value.id)
      user.value.followed = true
      user.value.fansCount = (user.value.fansCount || 0) + 1
    }
  } catch {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    followSaving.value = false
  }
}

function goChat() {
  if (!userStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  router.push(`/messages?to=${userId}`)
}

async function loadUser() {
  loading.value = true
  try {
    user.value = await getUserById(userId)
  } catch {
    user.value = null
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  recordLoading.value = true
  try {
    const data = await getUserRecords(userId, { page: page.value, size: pageSize })
    records.value = data.records || []
    total.value = data.total || 0
  } catch {
    records.value = []
  } finally {
    recordLoading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  loadRecords()
}

async function onLike(item) {
  if (!userStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const res = await likeRecord(item.id)
    item.liked = res.liked
    item.likeCount = res.likeCount
  } catch {
    // 错误提示已由 request 拦截器统一处理
  }
}

onMounted(() => {
  loadUser()
  loadRecords()
})
</script>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px 32px;
  margin-bottom: 28px;
}

.user-name {
  font-size: 22px;
  margin-bottom: 8px;
}

.user-bio {
  font-size: 13px;
  margin-bottom: 8px;
}

.user-meta {
  font-size: 12px;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-stats {
  margin-left: 16px;
}

.user-stats a {
  color: var(--gb-text-muted);
  margin-right: 14px;
  font-size: 12px;
}

.user-stats a:hover {
  color: var(--gb-accent);
}

.user-stats b {
  color: var(--gb-text);
  margin-right: 2px;
}

.user-stats a:hover b {
  color: var(--gb-accent);
}

.user-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.list-wrap {
  min-height: 160px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
