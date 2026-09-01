import request from '@/utils/request'

export function getConversations() {
  return request.get('/api/message/conversations')
}

export function getMessages(peerId, params) {
  return request.get(`/api/message/with/${peerId}`, { params })
}

export function sendMessage(data) {
  return request.post('/api/message/send', data)
}

export function getUnreadCount() {
  return request.get('/api/message/unread/count')
}
