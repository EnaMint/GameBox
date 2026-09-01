<template>
  <div class="page-container">
    <div class="profile-card gb-card" v-loading="!profile">
      <template v-if="profile">
        <div class="profile-avatar">
          <ImageUploader :model-value="profile.avatar" type="avatar" crop text="更换头像" @update:model-value="onAvatarChange" />
        </div>
        <div class="profile-info">
          <h2 class="profile-name">{{ profile.nickname }}</h2>
          <p class="profile-bio gb-muted">{{ profile.bio || '这个人很懒，什么都没写～' }}</p>
          <p class="profile-meta gb-muted">@{{ profile.username }} · 加入于 {{ formatDateTime(profile.createdAt) }}</p>
          <p class="profile-stats">
            <router-link :to="`/user/${profile.id}/follows`"><b>{{ profile.followCount || 0 }}</b> 关注</router-link>
            <router-link :to="`/user/${profile.id}/fans`"><b>{{ profile.fansCount || 0 }}</b> 粉丝</router-link>
          </p>
        </div>
        <el-button class="profile-edit-btn" :icon="Edit" @click="openEdit">编辑资料</el-button>
      </template>
    </div>

    <div class="tabs-card gb-card">
      <el-tabs v-model="activeTab" @tab-change="loadTab">
        <el-tab-pane label="我的攻略" name="strategy">
          <div v-loading="strategyTab.loading" class="tab-body">
            <div v-if="strategyTab.list.length" class="strategy-list">
              <StrategyCard v-for="item in strategyTab.list" :key="item.id" :item="item" />
            </div>
            <EmptyTip v-else-if="!strategyTab.loading" text="还没有发布过攻略" />
            <div class="pager">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="strategyTab.total"
                :page-size="pageSize"
                :current-page="strategyTab.page"
                @current-change="onStrategyPage"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的组队" name="team">
          <div v-loading="teamTab.loading" class="tab-body">
            <div v-if="teamTab.list.length" class="team-grid">
              <TeamCard v-for="item in teamTab.list" :key="item.id" :item="item" />
            </div>
            <EmptyTip v-else-if="!teamTab.loading" text="还没有发布过组队帖" />
            <div class="pager">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="teamTab.total"
                :page-size="pageSize"
                :current-page="teamTab.page"
                @current-change="onTeamPage"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的申请" name="application">
          <div v-loading="appLoading" class="tab-body">
            <div v-if="applications.length" class="app-list">
              <div v-for="(app, index) in applications" :key="app.id || index" class="app-row gb-card">
                <span v-if="appName(app)" class="gb-tag-game">{{ appName(app) }}</span>
                <span class="app-title gb-link" @click="$router.push(`/team/${app.postId}`)">{{ app.postTitle }}</span>
                <span class="app-message gb-muted">留言：{{ app.message || '无' }}</span>
                <el-tag size="small" :type="appStatus(app.status)[1]">{{ appStatus(app.status)[0] }}</el-tag>
              </div>
            </div>
            <EmptyTip v-else-if="!appLoading" text="还没有申请过组队" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的战绩" name="record">
          <div v-loading="recordTab.loading" class="tab-body">
            <div v-if="recordTab.list.length" class="record-list">
              <RecordCard
                v-for="item in recordTab.list"
                :key="item.id"
                :item="item"
                show-delete
                @delete="onRecordDelete"
                @like="onRecordLike"
              />
            </div>
            <EmptyTip v-else-if="!recordTab.loading" text="还没有发布过战绩" />
            <div class="pager">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="recordTab.total"
                :page-size="pageSize"
                :current-page="recordTab.page"
                @current-change="onRecordPage"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="editVisible" title="编辑资料" width="420px" :close-on-click-modal="false">
      <el-form label-width="60px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" maxlength="20" show-word-limit placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="签名">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" maxlength="100" show-word-limit placeholder="介绍一下自己" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { getMe, updateMe } from '@/api/user'
import { getMyStrategies } from '@/api/strategy'
import { getMyTeams, getMyApplications } from '@/api/team'
import { getMyRecords, deleteRecord, likeRecord } from '@/api/record'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import StrategyCard from '@/components/StrategyCard.vue'
import TeamCard from '@/components/TeamCard.vue'
import RecordCard from '@/components/RecordCard.vue'
import ImageUploader from '@/components/ImageUploader.vue'
import EmptyTip from '@/components/EmptyTip.vue'

const userStore = useUserStore()
const pageSize = 10

const profile = ref(null)
const activeTab = ref('strategy')

const strategyTab = reactive({ list: [], total: 0, page: 1, loading: false, loaded: false })
const teamTab = reactive({ list: [], total: 0, page: 1, loading: false, loaded: false })
const recordTab = reactive({ list: [], total: 0, page: 1, loading: false, loaded: false })
const applications = ref([])
const appLoading = ref(false)
let appLoaded = false

const editVisible = ref(false)
const editSaving = ref(false)
const editForm = reactive({ nickname: '', bio: '' })

async function loadProfile() {
  profile.value = await getMe()
}

async function loadStrategies() {
  strategyTab.loaded = true
  strategyTab.loading = true
  try {
    const data = await getMyStrategies({ page: strategyTab.page, size: pageSize })
    strategyTab.list = data.records || []
    strategyTab.total = data.total || 0
  } finally {
    strategyTab.loading = false
  }
}

async function loadTeams() {
  teamTab.loaded = true
  teamTab.loading = true
  try {
    const data = await getMyTeams({ page: teamTab.page, size: pageSize })
    teamTab.list = data.records || []
    teamTab.total = data.total || 0
  } finally {
    teamTab.loading = false
  }
}

async function loadApplications() {
  appLoaded = true
  appLoading.value = true
  try {
    const data = await getMyApplications()
    applications.value = Array.isArray(data) ? data : data?.records || []
  } finally {
    appLoading.value = false
  }
}

async function loadRecords() {
  recordTab.loaded = true
  recordTab.loading = true
  try {
    const data = await getMyRecords({ page: recordTab.page, size: pageSize })
    recordTab.list = data.records || []
    recordTab.total = data.total || 0
  } finally {
    recordTab.loading = false
  }
}

function loadTab(name) {
  if (name === 'strategy' && !strategyTab.loaded) loadStrategies()
  else if (name === 'team' && !teamTab.loaded) loadTeams()
  else if (name === 'application' && !appLoaded) loadApplications()
  else if (name === 'record' && !recordTab.loaded) loadRecords()
}

function onStrategyPage(p) {
  strategyTab.page = p
  loadStrategies()
}

function onTeamPage(p) {
  teamTab.page = p
  loadTeams()
}

function onRecordPage(p) {
  recordTab.page = p
  loadRecords()
}

async function onAvatarChange(url) {
  if (!url) return
  try {
    await updateMe({ nickname: profile.value.nickname, avatar: url })
    profile.value.avatar = url
    userStore.updateProfile({ avatar: url })
    ElMessage.success('头像已更新')
  } catch {
    // 错误提示已由 request 拦截器统一处理
  }
}

function openEdit() {
  editForm.nickname = profile.value?.nickname || ''
  editForm.bio = profile.value?.bio || ''
  editVisible.value = true
}

async function saveProfile() {
  const nickname = editForm.nickname.trim()
  if (!nickname) {
    ElMessage.warning('昵称不能为空')
    return
  }
  editSaving.value = true
  try {
    await updateMe({ nickname, bio: editForm.bio })
    ElMessage.success('资料已更新')
    userStore.updateProfile({ nickname, bio: editForm.bio })
    editVisible.value = false
    loadProfile()
  } finally {
    editSaving.value = false
  }
}

async function onRecordDelete(item) {
  await ElMessageBox.confirm('确定要删除这条战绩吗？删除后不可恢复', '提示', { type: 'warning' })
  await deleteRecord(item.id)
  ElMessage.success('删除成功')
  if (recordTab.list.length === 1 && recordTab.page > 1) recordTab.page--
  loadRecords()
}

async function onRecordLike(item) {
  try {
    const res = await likeRecord(item.id)
    item.liked = res.liked
    item.likeCount = res.likeCount
  } catch {
    // 错误提示已由 request 拦截器统一处理
  }
}

function appName(app) {
  return typeof app.game === 'string' ? app.game : app.game?.name || ''
}

const appStatusMap = {
  0: ['待审核', 'warning'],
  1: ['已通过', 'success'],
  2: ['已拒绝', 'danger'],
  3: ['已撤回', 'info']
}

function appStatus(status) {
  return appStatusMap[status] || ['未知', 'info']
}

onMounted(() => {
  loadProfile()
  loadStrategies()
})
</script>

<style scoped>
.profile-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px 28px;
  margin-bottom: 20px;
}

.profile-name {
  font-size: 22px;
  margin-bottom: 8px;
}

.profile-bio {
  font-size: 13px;
  margin-bottom: 8px;
}

.profile-meta {
  font-size: 12px;
}

.profile-stats {
  margin-top: 8px;
}

.profile-stats a {
  color: var(--gb-text-muted);
  margin-right: 16px;
  font-size: 13px;
}

.profile-stats a:hover {
  color: var(--gb-accent);
}

.profile-stats b {
  color: var(--gb-text);
  margin-right: 2px;
}

.profile-stats a:hover b {
  color: var(--gb-accent);
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.tabs-card {
  padding: 12px 24px 24px;
}

.tab-body {
  min-height: 180px;
  padding-top: 8px;
}

.strategy-list,
.record-list,
.app-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 12px;
}

.app-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.app-title {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.app-message {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
