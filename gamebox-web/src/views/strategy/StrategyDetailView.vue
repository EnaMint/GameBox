<template>
  <div class="page-container" v-loading="loading">
    <BackBar fallback="/strategy" />
    <template v-if="detail">
      <div class="detail-wrap">
        <div class="detail-card gb-card">
          <h1 class="detail-title">{{ detail.title }}</h1>
          <div class="detail-meta">
            <el-tag size="small" effect="plain">{{ detail.category }}</el-tag>
            <span v-if="detail.game" class="gb-tag-game game-link" @click="$router.push('/games')">
              {{ detail.game.name }}
            </span>
            <UserTag :user="detail.author" :size="26" />
            <span class="gb-muted">{{ formatDateTime(detail.createdAt) }}</span>
          </div>
          <div class="detail-stats gb-muted">
            <span><el-icon><View /></el-icon>{{ detail.viewCount || 0 }} 浏览</span>
            <span><el-icon><Star /></el-icon>{{ detail.likeCount || 0 }} 点赞</span>
            <span><el-icon><ChatDotRound /></el-icon>{{ detail.commentCount || 0 }} 评论</span>
          </div>

          <MdPreview :model-value="detail.content" theme="dark" class="detail-content" />

          <div class="detail-actions">
            <el-button
              :type="detail.liked ? 'primary' : 'default'"
              :icon="Star"
              :loading="liking"
              @click="toggleLike"
            >
              {{ detail.liked ? '已点赞' : '点赞' }} {{ detail.likeCount || 0 }}
            </el-button>
            <template v-if="isAuthor">
              <el-button :icon="Edit" @click="$router.push(`/strategy/edit/${detail.id}`)">编辑</el-button>
              <el-button type="danger" :icon="Delete" @click="onDelete">删除</el-button>
            </template>
          </div>
        </div>

        <div class="detail-card gb-card comment-card">
          <h3 class="section-title">评论 {{ detail.commentCount || 0 }}</h3>

          <div v-if="userStore.isLoggedIn" class="comment-editor">
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="友善发言，聊聊你对这篇攻略的看法～"
            />
            <div class="comment-editor-foot">
              <el-button type="primary" :loading="submitting" :disabled="!commentText.trim()" @click="submitComment">
                发表评论
              </el-button>
            </div>
          </div>
          <el-button v-else class="login-comment-btn" @click="goLogin">登录后参与评论</el-button>

          <div v-loading="commentLoading" class="comment-list">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <UserTag :user="c.user" :size="32" />
              <div class="comment-main">
                <div class="comment-meta gb-muted">{{ relativeTime(c.createdAt) }}</div>
                <p class="comment-content">{{ c.content }}</p>
              </div>
              <el-button
                v-if="canDeleteComment(c)"
                type="danger"
                size="small"
                text
                :icon="Delete"
                @click="onDeleteComment(c)"
              >删除</el-button>
            </div>
            <EmptyTip v-if="!comments.length && !commentLoading" text="暂无评论，快来抢沙发" />
          </div>
        </div>
      </div>
    </template>
    <EmptyTip v-else-if="!loading" text="攻略不存在或已被删除" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Star, ChatDotRound, Edit, Delete } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getStrategyById, deleteStrategy, likeStrategy, getComments, addComment, deleteComment } from '@/api/strategy'
import { useUserStore } from '@/stores/user'
import { formatDateTime, relativeTime } from '@/utils/format'
import UserTag from '@/components/UserTag.vue'
import EmptyTip from '@/components/EmptyTip.vue'
import BackBar from '@/components/BackBar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = route.params.id
const detail = ref(null)
const loading = ref(false)
const liking = ref(false)

const comments = ref([])
const commentLoading = ref(false)
const commentText = ref('')
const submitting = ref(false)

const isAuthor = computed(() => !!userStore.userInfo && userStore.userInfo.userId === detail.value?.author?.id)

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await getStrategyById(id)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  commentLoading.value = true
  try {
    const data = await getComments(id)
    comments.value = Array.isArray(data) ? data : data?.records || []
  } catch {
    comments.value = []
  } finally {
    commentLoading.value = false
  }
}

async function toggleLike() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  liking.value = true
  try {
    const res = await likeStrategy(id)
    detail.value.liked = res.liked
    detail.value.likeCount = res.likeCount
  } finally {
    liking.value = false
  }
}

async function onDelete() {
  await ElMessageBox.confirm('确定要删除这篇攻略吗？删除后不可恢复', '提示', { type: 'warning' })
  await deleteStrategy(id)
  ElMessage.success('删除成功')
  router.push('/strategy')
}

async function submitComment() {
  const content = commentText.value.trim()
  if (!content) return
  submitting.value = true
  try {
    await addComment(id, { content })
    ElMessage.success('评论成功')
    commentText.value = ''
    detail.value.commentCount = (detail.value.commentCount || 0) + 1
    loadComments()
  } finally {
    submitting.value = false
  }
}

function canDeleteComment(comment) {
  const uid = userStore.userInfo?.userId
  if (!uid) return false
  return uid === comment.user?.id || uid === detail.value?.author?.id
}

async function onDeleteComment(comment) {
  await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', { type: 'warning' })
  await deleteComment(comment.id)
  ElMessage.success('删除成功')
  detail.value.commentCount = Math.max((detail.value.commentCount || 1) - 1, 0)
  loadComments()
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

onMounted(() => {
  loadDetail()
  loadComments()
})
</script>

<style scoped>
.detail-wrap {
  max-width: 860px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-card {
  padding: 28px 32px;
}

.detail-title {
  font-size: 26px;
  line-height: 1.4;
  margin-bottom: 14px;
}

.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.game-link {
  cursor: pointer;
}

.game-link:hover {
  filter: brightness(1.2);
}

.detail-stats {
  display: flex;
  gap: 18px;
  font-size: 13px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--gb-border);
}

.detail-stats span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.detail-content {
  margin: 18px 0;
  background-color: transparent;
}

.detail-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--gb-border);
}

.comment-card {
  padding: 24px 32px;
}

.comment-editor {
  margin-bottom: 20px;
}

.comment-editor-foot {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.login-comment-btn {
  width: 100%;
  margin-bottom: 20px;
}

.comment-list {
  min-height: 80px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--gb-border);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  font-size: 12px;
  margin-bottom: 4px;
}

.comment-content {
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
