<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo">
        <el-icon :size="36"><GamepadIcon /></el-icon>
        <h1>GameBox</h1>
      </div>
      <p class="auth-sub">游戏攻略与玩家社区</p>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="submit">登 录</el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        还没有账号？<router-link to="/register" class="gb-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { GamepadIcon } from '@/components/icons'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(102, 192, 244, 0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(163, 207, 6, 0.06) 0%, transparent 50%),
    var(--gb-bg);
}

.auth-card {
  width: 400px;
  padding: 40px 36px;
  background-color: var(--gb-card);
  border-radius: 10px;
  border: 1px solid var(--gb-border);
}

.auth-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--gb-accent);
}

.auth-logo h1 {
  font-size: 26px;
}

.auth-sub {
  text-align: center;
  color: var(--gb-text-muted);
  margin: 8px 0 28px;
}

.auth-btn {
  width: 100%;
}

.auth-footer {
  text-align: center;
  color: var(--gb-text-muted);
  margin-top: 12px;
}
</style>
