<template>
  <div class="page-container">
    <div class="messages-card gb-card">
      <div class="conv-pane">
        <div class="conv-header">私信</div>
        <div class="conv-list" v-loading="convLoading">
          <template v-if="conversations.length">
            <div
              v-for="c in conversations"
              :key="c.peerId"
              class="conv-item"
              :class="{ active: String(c.peerId) === String(activePeerId) }"
              @click="selectConversation(c)"
            >
              <el-badge :value="c.unread" :hidden="!c.unread" :max="99">
                <el-avatar :size="42" :src="c.peerAvatar || undefined">
                  {{ (c.peerNickname || '?').slice(0, 1).toUpperCase() }}
                </el-avatar>
              </el-badge>
              <div class="conv-info">
                <div class="conv-name">{{ c.peerNickname }}</div>
                <div class="conv-last gb-muted">{{ c.lastMessage || '开始聊天吧' }}</div>
              </div>
              <div class="conv-time gb-muted">{{ relativeTime(c.lastAt) }}</div>
            </div>
          </template>
          <EmptyTip v-else-if="!convLoading && !activePeerInfo" text="还没有私信，去用户主页发起聊天吧" />
        </div>
      </div>

      <div class="chat-pane">
        <template v-if="activePeerInfo">
          <div class="chat-header">
            <span class="chat-peer" @click="$router.push(`/user/${activePeerInfo.id}`)">
              {{ activePeerInfo.nickname }}
            </span>
            <span class="gb-muted chat-header-tip">点击查看对方主页</span>
          </div>

          <div class="chat-body" ref="bodyRef">
            <div v-if="limitReached" class="limit-tip">
              对方回复前最多只能发送 2 条私信，等待对方回复后即可继续聊天
            </div>
            <div v-else-if="!peerReplied && messages.length" class="limit-tip soft">
              对方尚未回复，你还可以发送 {{ 2 - mySentCount }} 条消息
            </div>
            <div v-for="m in messages" :key="m.id" class="msg-row" :class="{ mine: String(m.fromUserId) === myId }">
              <div class="msg-bubble">{{ m.content }}</div>
              <div class="msg-time gb-muted">{{ formatDateTime(m.createdAt) }}</div>
            </div>
            <div v-if="!messages.length && !msgLoading" class="chat-empty gb-muted">
              还没有消息，打个招呼吧～
            </div>
          </div>

          <div class="chat-input">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              resize="none"
              :disabled="limitReached"
              :placeholder="limitReached ? '等待对方回复后才能继续发送' : '输入消息，Enter 发送，Shift+Enter 换行'"
              @keydown.enter.exact.prevent="send"
            />
            <el-button
              class="send-btn"
              type="primary"
              :disabled="limitReached || !draft.trim()"
              :loading="sending"
              @click="send"
            >
              发送
            </el-button>
          </div>
        </template>
        <div v-else class="chat-placeholder">
          <EmptyTip text="选择左侧联系人开始聊天" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getConversations, getMessages, sendMessage } from '@/api/message'
import { getUserById } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { formatDateTime, relativeTime } from '@/utils/format'
import EmptyTip from '@/components/EmptyTip.vue'

const route = useRoute()
const userStore = useUserStore()

const myId = computed(() => String(userStore.userInfo?.userId ?? ''))

const conversations = ref([])
const convLoading = ref(false)
const activePeerId = ref(null)
const activePeerInfo = ref(null)
const messages = ref([])
const msgLoading = ref(false)
const draft = ref('')
const sending = ref(false)
const bodyRef = ref(null)

let timer = null
let polling = false

const activeConv = computed(() => conversations.value.find((c) => String(c.peerId) === String(activePeerId.value)) || null)
const peerReplied = computed(() => activeConv.value ? !!activeConv.value.peerReplied : false)
const mySentCount = computed(() => messages.value.filter((m) => String(m.fromUserId) === myId.value).length)
const limitReached = computed(() => !peerReplied.value && mySentCount.value >= 2)

async function loadConversations() {
  convLoading.value = conversations.value.length === 0
  try {
    conversations.value = await getConversations()
  } catch {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    convLoading.value = false
  }
}

async function loadMessages(scrollToBottom = true) {
  if (!activePeerId.value) return
  msgLoading.value = messages.value.length === 0
  try {
    const nearBottom = isNearBottom()
    const data = await getMessages(activePeerId.value, { page: 1, size: 50 })
    messages.value = data.records || []
    await nextTick()
    if (scrollToBottom || nearBottom) scrollBottom()
  } catch {
    messages.value = []
  } finally {
    msgLoading.value = false
  }
}

function isNearBottom() {
  const el = bodyRef.value
  if (!el) return true
  return el.scrollHeight - el.scrollTop - el.clientHeight < 80
}

function scrollBottom() {
  const el = bodyRef.value
  if (el) el.scrollTop = el.scrollHeight
}

async function selectConversation(c) {
  if (String(c.peerId) === String(activePeerId.value)) return
  activePeerId.value = c.peerId
  activePeerInfo.value = { id: c.peerId, nickname: c.peerNickname, avatar: c.peerAvatar }
  messages.value = []
  await loadMessages()
  loadConversations()
}

async function openPeer(peerId) {
  const existing = conversations.value.find((c) => String(c.peerId) === String(peerId))
  if (existing) {
    await selectConversation(existing)
    return
  }
  try {
    const user = await getUserById(peerId)
    activePeerId.value = user.id
    activePeerInfo.value = { id: user.id, nickname: user.nickname, avatar: user.avatar }
    messages.value = []
    await loadMessages()
  } catch {
    // 用户不存在时由拦截器提示
  }
}

async function send() {
  const content = draft.value.trim()
  if (!content || sending.value || limitReached.value) return
  sending.value = true
  try {
    const msg = await sendMessage({ toUserId: Number(activePeerId.value), content })
    messages.value.push(msg)
    draft.value = ''
    await nextTick()
    scrollBottom()
    await loadConversations()
    const conv = activeConv.value
    if (conv) {
      conv.lastMessage = content
      conv.lastAt = msg.createdAt
    }
  } catch {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    sending.value = false
  }
}

async function poll() {
  if (polling) return
  polling = true
  try {
    const nearBottom = isNearBottom()
    await loadConversations()
    if (activePeerId.value) {
      const data = await getMessages(activePeerId.value, { page: 1, size: 50 })
      const records = data.records || []
      const changed = records.length !== messages.value.length
        || records[records.length - 1]?.id !== messages.value[messages.value.length - 1]?.id
      if (changed) {
        messages.value = records
        await nextTick()
        if (nearBottom) scrollBottom()
      }
    }
  } catch {
    // 静默失败，下一轮重试
  } finally {
    polling = false
  }
}

watch(
  () => route.query.to,
  (to) => {
    if (to) openPeer(to)
  }
)

onMounted(async () => {
  await loadConversations()
  if (route.query.to) {
    await openPeer(route.query.to)
  } else if (conversations.value.length) {
    await selectConversation(conversations.value[0])
  }
  timer = setInterval(poll, 5000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.messages-card {
  display: flex;
  min-height: 560px;
  overflow: hidden;
}

.conv-pane {
  width: 300px;
  border-right: 1px solid var(--gb-border);
  display: flex;
  flex-direction: column;
}

.conv-header {
  padding: 16px 20px 12px;
  font-size: 16px;
  font-weight: 700;
  border-bottom: 1px solid var(--gb-border);
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.conv-item:hover {
  background: rgba(102, 192, 244, 0.06);
}

.conv-item.active {
  background: rgba(102, 192, 244, 0.12);
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
}

.conv-last {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  font-size: 11px;
  white-space: nowrap;
}

.chat-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--gb-border);
}

.chat-peer {
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.chat-peer:hover {
  color: var(--gb-accent);
}

.chat-header-tip {
  font-size: 12px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.limit-tip {
  align-self: center;
  font-size: 12px;
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.1);
  border: 1px solid rgba(230, 162, 60, 0.3);
  padding: 6px 12px;
  border-radius: 6px;
  margin-bottom: 4px;
}

.limit-tip.soft {
  color: var(--gb-text-muted);
  background: rgba(102, 192, 244, 0.06);
  border-color: var(--gb-border);
}

.msg-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 75%;
}

.msg-row.mine {
  align-self: flex-end;
  align-items: flex-end;
}

.msg-bubble {
  padding: 9px 13px;
  border-radius: 10px;
  background: var(--gb-bg-deep);
  border: 1px solid var(--gb-border);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-row.mine .msg-bubble {
  background: rgba(102, 192, 244, 0.18);
  border-color: rgba(102, 192, 244, 0.4);
}

.msg-time {
  font-size: 11px;
  margin-top: 4px;
}

.chat-empty {
  align-self: center;
  margin-top: 40px;
  font-size: 13px;
}

.chat-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid var(--gb-border);
  align-items: flex-end;
}

.chat-input :deep(.el-textarea) {
  flex: 1;
}

.send-btn {
  height: 40px;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
