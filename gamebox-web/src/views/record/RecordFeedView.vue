<template>
  <div class="page-container">
    <div class="feed-toolbar">
      <h2 class="section-title feed-title">战绩动态</h2>
      <el-button type="primary" :icon="Plus" @click="goPublish">发布战绩</el-button>
    </div>

    <div v-loading="loading" class="feed-list">
      <RecordCard
        v-for="item in list"
        :key="item.id"
        :item="item"
        :show-delete="isMine(item)"
        @like="handleLike"
        @delete="confirmDelete"
      />
    </div>
    <EmptyTip v-if="!list.length && !loading" text="还没有战绩动态，来发第一条吧" />

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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRecordFeed, likeRecord, deleteRecord } from '@/api/record'
import { useUserStore } from '@/stores/user'
import RecordCard from '@/components/RecordCard.vue'
import EmptyTip from '@/components/EmptyTip.vue'

const router = useRouter()
const userStore = useUserStore()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const data = await getRecordFeed({ page: page.value, size })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function goPublish() {
  router.push(userStore.isLoggedIn ? '/record/create' : '/login')
}

function isMine(item) {
  return userStore.isLoggedIn && item.user?.id === userStore.userInfo?.userId
}

async function handleLike(item) {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const data = await likeRecord(item.id)
  item.liked = data.liked
  item.likeCount = data.likeCount
}

async function confirmDelete(item) {
  await ElMessageBox.confirm('删除后不可恢复，确定删除这条战绩吗？', '提示', { type: 'warning' })
  await deleteRecord(item.id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.feed-toolbar {
  max-width: 720px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.feed-title {
  margin-bottom: 0;
}

.feed-list {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 120px;
}

.pager {
  max-width: 720px;
  margin: 24px auto 0;
  justify-content: center;
}
</style>
