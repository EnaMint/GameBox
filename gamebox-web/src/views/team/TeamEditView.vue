<template>
  <div class="page-container">
    <h2 class="section-title">发布组队</h2>

    <div class="form-card gb-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" label-position="top">
        <el-form-item label="选择游戏" prop="gameId">
          <div class="picked-game" v-if="pickedGame">
            <div class="picked-cover cover-fallback">
              <img v-if="pickedGame.cover" :src="pickedGame.cover" :alt="pickedGame.name" />
              <span v-else>{{ pickedGame.name.slice(0, 4) }}</span>
            </div>
            <span class="picked-name">{{ pickedGame.name }}</span>
            <el-button size="small" @click="pickerVisible = true">更换</el-button>
            <el-button size="small" type="danger" plain @click="pickedGame = null">清除</el-button>
          </div>
          <el-button v-else :icon="Search" @click="pickerVisible = true">选择游戏</el-button>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话说明你要组什么队" />
        </el-form-item>

        <el-form-item label="招募说明" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="介绍下目标、要求、时间安排等..."
          />
        </el-form-item>

        <el-form-item label="目标人数" prop="memberLimit">
          <el-input-number v-model="form.memberLimit" :min="2" :max="20" />
        </el-form-item>

        <el-form-item label="需要开麦">
          <el-switch v-model="form.needVoice" />
        </el-form-item>

        <el-form-item label="游戏时间">
          <el-input v-model="form.playTime" maxlength="64" placeholder="如：周末晚上" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <GamePicker
      v-model="pickerVisible"
      :selected-id="form.gameId"
      @select="onPickGame"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createTeam } from '@/api/team'
import GamePicker from '@/components/GamePicker.vue'

const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const pickerVisible = ref(false)
const pickedGame = ref(null)

const form = reactive({
  gameId: null,
  title: '',
  content: '',
  memberLimit: 4,
  needVoice: false,
  playTime: ''
})

const rules = {
  gameId: [{ required: true, message: '请选择游戏', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

function onPickGame(game) {
  pickedGame.value = game
  form.gameId = game.id
  formRef.value?.validateField('gameId')
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const result = await createTeam({
      gameId: form.gameId,
      title: form.title,
      content: form.content,
      memberLimit: form.memberLimit,
      needVoice: form.needVoice ? 1 : 0,
      playTime: form.playTime
    })
    ElMessage.success('组队发布成功')
    const newId = result?.id || (Number.isInteger(result) ? result : null)
    router.replace(newId ? `/team/${newId}` : '/team')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.form-card {
  max-width: 680px;
  padding: 24px;
}

.picked-game {
  display: flex;
  align-items: center;
  gap: 12px;
}

.picked-cover {
  width: 72px;
  height: 44px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.picked-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.picked-name {
  font-weight: 600;
}
</style>
