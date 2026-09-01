<template>
  <div class="page-container">
    <h2 class="section-title">我的游戏库</h2>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane v-for="tab in tabs" :key="tab.key" :label="tab.label" :name="tab.key" />
    </el-tabs>

    <div v-loading="loading" class="my-list">
      <div v-for="item in list" :key="item.id" class="my-row gb-card">
        <div class="row-game">
          <div class="row-cover cover-fallback">
            <img v-if="item.gameCover" :src="item.gameCover" :alt="item.gameName" />
            <span v-else>{{ (item.gameName || '游戏').slice(0, 2) }}</span>
          </div>
          <div class="row-game-info">
            <div class="row-name">{{ item.gameName }}</div>
            <span class="gb-tag-game">{{ item.genre || '未知类型' }}</span>
          </div>
        </div>

        <div class="row-fields">
          <el-select v-model="item.status" size="small" class="field-status" @change="saveItem(item)">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>

          <el-input-number
            v-model="item.playHours"
            size="small"
            class="field-hours"
            :min="0"
            :step="0.1"
            :precision="1"
            controls-position="right"
            @change="saveItem(item)"
          />

          <el-rate v-model="item.rating" class="field-rate" :max="5" @change="saveItem(item)" />

          <span class="row-remark gb-muted" :title="item.remark">{{ item.remark || '无备注' }}</span>
        </div>

        <div class="row-ops">
          <el-button size="small" :icon="Edit" @click="openEdit(item)">编辑</el-button>
          <el-button size="small" type="danger" plain :icon="Delete" @click="confirmRemove(item)">移除</el-button>
        </div>
      </div>
    </div>
    <EmptyTip v-if="!list.length && !loading" text="这里空空如也，去游戏字典收录几款游戏吧" />

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

    <el-dialog v-model="editVisible" title="编辑游戏记录" width="440px" :close-on-click-modal="false">
      <el-form label-width="72px">
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长">
          <el-input-number v-model="editForm.playHours" :min="0" :step="0.1" :precision="1" controls-position="right" />
          <span class="gb-muted edit-unit">小时</span>
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="editForm.rating" :max="5" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="写点游玩感想..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import { getMyGames, updateGame, removeGame } from '@/api/ugame'
import EmptyTip from '@/components/EmptyTip.vue'

const tabs = [
  { key: 'all', label: '全部', status: '' },
  { key: 'want', label: '想玩', status: 1 },
  { key: 'playing', label: '在玩', status: 2 },
  { key: 'finished', label: '已通关', status: 3 },
  { key: 'shelved', label: '搁置', status: 4 }
]
const statusOptions = [
  { label: '想玩', value: 1 },
  { label: '在玩', value: 2 },
  { label: '已通关', value: 3 },
  { label: '搁置', value: 4 }
]

const activeTab = ref('all')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

const editVisible = ref(false)
const editSaving = ref(false)
const editTarget = ref(null)
const editForm = reactive({ status: 1, playHours: 0, rating: 0, remark: '' })

async function loadList() {
  loading.value = true
  try {
    const status = tabs.find((t) => t.key === activeTab.value)?.status
    const data = await getMyGames({ status: status || undefined, page: page.value, size })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  page.value = 1
  loadList()
}

async function saveItem(item) {
  await updateGame(item.id, {
    status: item.status,
    playHours: item.playHours || 0,
    rating: item.rating || 0,
    remark: item.remark
  })
  ElMessage.success('已保存')
}

function openEdit(item) {
  editTarget.value = item
  editForm.status = item.status
  editForm.playHours = item.playHours || 0
  editForm.rating = item.rating || 0
  editForm.remark = item.remark || ''
  editVisible.value = true
}

async function submitEdit() {
  editSaving.value = true
  try {
    await updateGame(editTarget.value.id, { ...editForm })
    ElMessage.success('已保存')
    editVisible.value = false
    loadList()
  } finally {
    editSaving.value = false
  }
}

async function confirmRemove(item) {
  await ElMessageBox.confirm(`确定将《${item.gameName}》从游戏库移除吗？`, '提示', { type: 'warning' })
  await removeGame(item.id)
  ElMessage.success('已移除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.my-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}

.my-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
}

.row-game {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 240px;
  flex-shrink: 0;
}

.row-cover {
  width: 72px;
  height: 44px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.row-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.row-game-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.row-name {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-fields {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.field-status {
  width: 110px;
}

.field-hours {
  width: 120px;
}

.row-remark {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-ops {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.pager {
  margin-top: 24px;
  justify-content: center;
}

.edit-unit {
  margin-left: 8px;
}

@media (max-width: 900px) {
  .my-row {
    flex-wrap: wrap;
  }

  .row-game {
    width: auto;
  }
}
</style>
