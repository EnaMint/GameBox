import request from '@/utils/request'

export function getMe() {
  return request.get('/api/user/me')
}

export function getUserById(id) {
  return request.get(`/api/user/${id}`)
}

export function updateMe(data) {
  return request.put('/api/user/me', data)
}
