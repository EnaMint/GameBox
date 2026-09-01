<template>
  <div class="page-container" v-loading="loading">
    <BackBar fallback="/team" />
    <template v-if="detail">
      <div class="detail-card gb-card">
        <div class="detail-tags">
          <span class="gb-tag-game" v-if="detail.game?.name">{{ detail.game.name }}</span>
          <el-tag size="small" :type="statusType" effect="plain">{{ statusText }}</el-tag>
        </div>

        <h1 class="detail-title">{{ detail.title }}</h1>

        <div class="detail-meta">
          <UserTag :user="detail.leader" :size="26" />
          <span class="gb-muted">队长</span>
          <span class="gb-muted">发布于 {{ formatDateTime(detail.createdAt) }}</span>
        </div>

        <div class="detail-progress">
          <span class="gb-muted">队伍人数</span>
          <el-progress
            class="progress-bar"
            :percentage="progressPercent"
            :format="() => `${detail.memberCount}/${detail.memberLimit}`"
            :stroke-width="10"
          />
        </div>

        <div class="detail-info gb-muted">
          <span class="info-item">
            <el-icon><Microphone /></el-icon>
            {{ detail.needVoice ? '需要开麦' : '无需开麦' }}
          </span>
          <span class="info-item" v-if="detail.playTime">
            <el-icon><Clock /></el-icon>
            {{ detail.playTime }}
          </span>
        </div>

        <el-divider />

        <div class="detail-content">{{ detail.content || '队长没有填写招募说明...' }}</div>

        <el-divider />

        <!-- 队长视角 -->
        <template v-if="isLeader">
          <div class="section-title">收到的申请（{{ applications.length }}）</div>
          <div v-if="applications.length" class="apply-list">
            <div v-for="app in applications" :key="app.id" class="apply-item">
              <UserTag :user="app.user" :size="28" />
              <div class="apply-body">
                <p class="apply-message">{{ app.message || '（未留言）' }}</p>
                <span class="gb-muted apply-time">{{ formatDateTime(app.createdAt) }}</span>
              </div>
              <el-tag size="small" :type="applyStatusType(app.status)" effect="plain">{{ applyStatusText(app.status) }}</el-tag>
              <template v-if="app.status === 0 && detail.status === 1">
                <el-button size="small" type="success" @click="audit(app, 'approve')">通过</el-button>
                <el-button size="small" type="danger" plain @click="audit(app, 'reject')">拒绝</el-button>
              </template>
            </div>
          </div>
          <EmptyTip v-else text="还没有人申请" />

          <div class="manage-bar">
            <el-button v-if="detail.status === 1" type="warning" plain @click="changeStatus(2)">设为满员</el-button>
            <el-button v-if="detail.status === 1 || detail.status === 2" type="danger" plain @click="changeStatus(3)">关闭组队</el-button>
            <el-button type="danger" :icon="Delete" @click="confirmDelete">删除组队</el-button>
          </div>
        </template>

        <!-- 普通用户视角 -->
        <template v-else>
          <div class="section-title">申请加入</div>

          <div v-if="!userStore.isLoggedIn" class="apply-anon">
            <el-button type="primary" @click="router.push('/login')">登录后申请加入</el-button>
          </div>

          <template v-else>
            <div v-if="myStatus === 0" class="apply-state">
              <el-tag type="warning" effect="plain">已申请，等待审核</el-tag>
              <el-button size="small" @click="withdraw">撤回申请</el-button>
            </div>
            <div v-else-if="myStatus === 1" class="apply-state">
              <el-tag type="success" effect="plain">你已加入队伍</el-tag>
            </div>
            <div v-else class="apply-state">
              <el-tag v-if="myStatus === 2" type="danger" effect="plain" class="reject-hint">申请被拒绝，可重新申请</el-tag>
              <el-tag v-else-if="myStatus === 3" type="info" effect="plain" class="reject-hint">申请已撤回，可重新申请</el-tag>
              <el-button type="primary" :icon="Promotion" @click="applyVisible = true">{{ myStatus ? '重新申请' : '申请加入' }}</el-button>
            </div>
          </template>
        </template>
      </div>
    </template>
    <EmptyTip v-else-if="!loading" text="组队帖不存在或已被删除" />

    <el-dialog v-model="applyVisible" title="申请加入" width="440px" :close-on-click-modal="false">
      <el-input
        v-model="applyMessage"
        type="textarea"
        :rows="4"
        maxlength="200"
        show-word-limit
        placeholder="向队长介绍下自己，如段位、在线时间段..."
      />
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applySubmitting" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Microphone, Clock, Delete, Promotion } from '@element-plus/icons-vue'
import { getTeamById, getTeamApplications, auditApplication, applyTeam, withdrawApplication, updateTeamStatus, deleteTeam } from '@/api/team'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import UserTag from '@/components/UserTag.vue'
import EmptyTip from '@/components/EmptyTip.vue'
import BackBar from '@/components/BackBar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(null)
const applications = ref([])

const applyVisible = ref(false)
const applyMessage = ref('')
const applySubmitting = ref(false)

const teamStatusMap = { 1: ['招募中', 'success'], 2: ['已满员', 'info'], 3: ['已关闭', 'danger'] }
const applyStatusMap = { 0: ['待审核', 'warning'], 1: ['已通过', 'success'], 2: ['已拒绝', 'danger'], 3: ['已撤回', 'info'] }

const statusText = computed(() => teamStatusMap[detail.value?.status]?.[0] || '未知')
const statusType = computed(() => teamStatusMap[detail.value?.status]?.[1] || 'info')
const isLeader = computed(() => userStore.userInfo?.userId != null && userStore.userInfo.userId === detail.value?.leader?.id)
const myStatus = computed(() => detail.value?.myApplication?.status ?? null)
const progressPercent = computed(() => {
  const limit = detail.value?.memberLimit || 1
  return Math.min(100, Math.round((detail.value?.memberCount || 0) / limit * 100))
})

function applyStatusText(status) {
  return applyStatusMap[status]?.[0] || '未知'
}

function applyStatusType(status) {
  return applyStatusMap[status]?.[1] || 'info'
}

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await getTeamById(route.params.id)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function loadApplications() {
  try {
    applications.value = await getTeamApplications(route.params.id) || []
  } catch {
    applications.value = []
  }
}

async function audit(app, action) {
  await auditApplication(app.id, action)
  ElMessage.success(action === 'approve' ? '已通过申请' : '已拒绝申请')
  await Promise.all([loadDetail(), loadApplications()])
}

async function changeStatus(status) {
  await updateTeamStatus(route.params.id, status)
  ElMessage.success(status === 2 ? '已设为满员' : '组队已关闭')
  loadDetail()
}

async function confirmDelete() {
  await ElMessageBox.confirm('删除后组队帖不可恢复，确定删除吗？', '提示', { type: 'warning' })
  await deleteTeam(route.params.id)
  ElMessage.success('已删除')
  router.push('/team')
}

async function submitApply() {
  applySubmitting.value = true
  try {
    await applyTeam(route.params.id, { message: applyMessage.value })
    ElMessage.success('申请已提交')
    applyVisible.value = false
    applyMessage.value = ''
    loadDetail()
  } finally {
    applySubmitting.value = false
  }
}

async function withdraw() {
  await withdrawApplication(route.params.id)
  ElMessage.success('已撤回申请')
  loadDetail()
}

watch(isLeader, (v) => {
  if (v) loadApplications()
})

watch(() => route.params.id, () => {
  if (route.params.id) {
    detail.value = null
    applications.value = []
    loadDetail()
  }
})

onMounted(loadDetail)
</script>

<style scoped>
.detail-card {
  padding: 24px;
}

.detail-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 24px;
  margin-bottom: 16px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.progress-bar {
  flex: 1;
  max-width: 360px;
}

.detail-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.detail-content {
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--gb-text);
}

.apply-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.apply-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--gb-radius);
  background-color: var(--gb-bg);
}

.apply-body {
  flex: 1;
  min-width: 0;
}

.apply-message {
  line-height: 1.5;
  word-break: break-word;
}

.apply-time {
  font-size: 12px;
}

.manage-bar {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.apply-state {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reject-hint {
  flex-shrink: 0;
}

.apply-anon {
  padding: 4px 0;
}
</style>
