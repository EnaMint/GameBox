<template>
  <div class="page-container">
    <h2 class="section-title">发布战绩</h2>

    <div class="form-card gb-card">
      <el-form label-width="90px" label-position="top">
        <el-form-item label="关联游戏（选填）">
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

        <el-form-item label="战绩描述">
          <el-input
            v-model="content"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="晒晒你的战绩，比如通关时间、段位、击杀数..."
          />
        </el-form-item>

        <el-form-item label="图片（最多 9 张）">
          <MultiImageUploader v-model="images" type="record" :limit="9" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <GamePicker
      v-model="pickerVisible"
      :selected-id="pickedGame?.id"
      @select="(game) => (pickedGame = game)"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createRecord } from '@/api/record'
import GamePicker from '@/components/GamePicker.vue'
import MultiImageUploader from '@/components/MultiImageUploader.vue'

const router = useRouter()

const pickerVisible = ref(false)
const pickedGame = ref(null)
const content = ref('')
const images = ref([])
const submitting = ref(false)

async function submit() {
  if (!content.value.trim() && !images.value.length) {
    ElMessage.warning('描述和图片至少填写一项')
    return
  }
  submitting.value = true
  try {
    await createRecord({
      gameId: pickedGame.value?.id || null,
      content: content.value,
      images: images.value
    })
    ElMessage.success('战绩发布成功')
    router.push('/record')
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
