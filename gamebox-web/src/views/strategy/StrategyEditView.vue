<template>
  <div class="page-container">
    <h2 class="section-title">{{ isEdit ? '编辑攻略' : '发布攻略' }}</h2>

    <div class="edit-card gb-card" v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入攻略标题" maxlength="100" show-word-limit />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="分类" prop="category" class="form-half">
            <el-select v-model="form.category" placeholder="请选择分类" class="full">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>

          <el-form-item label="关联游戏" class="form-half">
            <div class="game-select">
              <el-button :icon="Plus" @click="pickerVisible = true">{{ form.gameId ? '重新选择' : '选择游戏' }}</el-button>
              <span v-if="form.gameId" class="selected-game">
                <span class="gb-tag-game">{{ gameName }}</span>
                <el-icon class="clear-icon" @click="clearGame"><CircleClose /></el-icon>
              </span>
            </div>
          </el-form-item>
        </div>

        <el-form-item label="封面">
          <ImageUploader v-model="form.cover" type="cover" text="上传封面" />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="简单介绍一下这篇攻略的内容（可选）"
          />
        </el-form-item>

        <el-form-item label="正文（Markdown）" prop="content">
          <MdEditor v-model="form.content" theme="dark" :style="{ height: '500px' }" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">
            {{ isEdit ? '保存修改' : '发布攻略' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <GamePicker v-model="pickerVisible" :selected-id="form.gameId" @select="onGameSelect" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, CircleClose } from '@element-plus/icons-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getStrategyById, createStrategy, updateStrategy } from '@/api/strategy'
import GamePicker from '@/components/GamePicker.vue'
import ImageUploader from '@/components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()

const editId = computed(() => route.params.id)
const isEdit = computed(() => !!editId.value)
const categories = ['攻略', '评测', '资讯', '心得', '其他']

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const pickerVisible = ref(false)
const gameName = ref('')

const form = reactive({
  title: '',
  category: '',
  gameId: null,
  cover: '',
  summary: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '标题最长 100 字', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (value && value.trim()) callback()
        else callback(new Error('请输入正文内容'))
      },
      trigger: 'blur'
    }
  ]
}

async function loadDetail() {
  loading.value = true
  try {
    const data = await getStrategyById(editId.value)
    form.title = data.title || ''
    form.category = data.category || ''
    form.gameId = data.gameId || data.game?.id || null
    gameName.value = data.game?.name || ''
    form.cover = data.cover || ''
    form.summary = data.summary || ''
    form.content = data.content || ''
  } finally {
    loading.value = false
  }
}

function onGameSelect(game) {
  form.gameId = game.id
  gameName.value = game.name
}

function clearGame() {
  form.gameId = null
  gameName.value = ''
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    let targetId = editId.value
    if (isEdit.value) {
      await updateStrategy(editId.value, form)
      ElMessage.success('保存成功')
    } else {
      const data = await createStrategy(form)
      targetId = typeof data === 'number' ? data : data?.id
      ElMessage.success('发布成功')
    }
    router.push(targetId ? `/strategy/${targetId}` : '/strategy')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (isEdit.value) loadDetail()
})
</script>

<style scoped>
.edit-card {
  padding: 28px 32px;
}

.form-row {
  display: flex;
  gap: 24px;
}

.form-half {
  flex: 1;
}

.full {
  width: 100%;
}

.game-select {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-game {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.clear-icon {
  cursor: pointer;
  color: var(--gb-text-muted);
  font-size: 16px;
}

.clear-icon:hover {
  color: var(--el-color-danger);
}
</style>
