<template>
  <div class="layout">
    <header class="topbar">
      <div class="topbar-inner">
        <router-link to="/" class="logo">
          <el-icon :size="24"><GamepadIcon /></el-icon>
          <span>GameBox</span>
        </router-link>

        <nav class="nav">
          <router-link to="/" class="nav-item" exact-active-class="active">首页</router-link>
          <router-link to="/strategy" class="nav-item" active-class="active">攻略</router-link>
          <router-link to="/games" class="nav-item" active-class="active">游戏</router-link>
          <router-link to="/team" class="nav-item" active-class="active">组队</router-link>
          <router-link to="/record" class="nav-item" active-class="active">战绩</router-link>
        </nav>

        <div class="topbar-right">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/messages" class="msg-entry" title="私信">
              <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
                <el-icon :size="21"><ChatDotRound /></el-icon>
              </el-badge>
            </router-link>

            <el-dropdown trigger="click" @command="handlePublish">
              <el-button type="primary" round size="small">
                发布<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/strategy/create">发攻略</el-dropdown-item>
                  <el-dropdown-item command="/team/create">发组队</el-dropdown-item>
                  <el-dropdown-item command="/record/create">发战绩</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-dropdown trigger="click" @command="handleUserMenu">
              <span class="avatar-wrap">
                <el-avatar :size="34" :src="userStore.userInfo?.avatar || undefined">
                  {{ avatarText }}
                </el-avatar>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled>{{ userStore.userInfo?.nickname }}</el-dropdown-item>
                  <el-dropdown-item command="/profile" divided>个人中心</el-dropdown-item>
                  <el-dropdown-item command="/my/games">我的游戏库</el-dropdown-item>
                  <el-dropdown-item command="/messages">我的私信</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button size="small" round @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" size="small" round @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ChatDotRound } from '@element-plus/icons-vue'
import { GamepadIcon } from '@/components/icons'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/message'

const router = useRouter()
const userStore = useUserStore()

const avatarText = computed(() => (userStore.userInfo?.nickname || '?').slice(0, 1).toUpperCase())

const unreadCount = ref(0)
let unreadTimer = null

async function refreshUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const data = await getUnreadCount()
    unreadCount.value = data?.count || 0
  } catch {
    // 静默失败，下一轮重试
  }
}

function startUnreadPolling() {
  stopUnreadPolling()
  refreshUnread()
  unreadTimer = setInterval(refreshUnread, 10000)
}

function stopUnreadPolling() {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
}

watch(
  () => userStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) startUnreadPolling()
    else {
      stopUnreadPolling()
      unreadCount.value = 0
    }
  }
)

onMounted(() => {
  if (userStore.isLoggedIn) startUnreadPolling()
})

onBeforeUnmount(stopUnreadPolling)

function handlePublish(path) {
  router.push(path)
}

async function handleUserMenu(command) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else {
    router.push(command)
  }
}
</script>

<style scoped>
.topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: var(--gb-bg-deep);
  border-bottom: 1px solid var(--gb-border);
  z-index: 100;
}

.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--gb-accent);
  white-space: nowrap;
}

.nav {
  display: flex;
  gap: 6px;
  flex: 1;
}

.nav-item {
  padding: 6px 14px;
  border-radius: 4px;
  color: var(--gb-text);
  font-size: 15px;
  transition: all 0.2s;
}

.nav-item:hover {
  color: var(--gb-accent);
  background-color: rgba(102, 192, 244, 0.08);
}

.nav-item.active {
  color: var(--gb-accent);
  background-color: rgba(102, 192, 244, 0.12);
  font-weight: 600;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.msg-entry {
  display: flex;
  align-items: center;
  color: var(--gb-text-muted);
  padding: 4px 2px;
  transition: color 0.2s;
}

.msg-entry:hover,
.msg-entry.router-link-active {
  color: var(--gb-accent);
}

.avatar-wrap {
  cursor: pointer;
  display: flex;
}

.main {
  padding-top: 60px;
  min-height: 100vh;
}
</style>
