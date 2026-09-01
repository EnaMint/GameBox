import request from '@/utils/request'

export function getRecordFeed(params) {
  return request.get('/api/record/feed', { params })
}

export function getMyRecords(params) {
  return request.get('/api/record/my', { params })
}

export function getUserRecords(userId, params) {
  return request.get(`/api/record/user/${userId}`, { params })
}

export function createRecord(data) {
  return request.post('/api/record', data)
}

export function deleteRecord(id) {
  return request.delete(`/api/record/${id}`)
}

export function likeRecord(id) {
  return request.post(`/api/record/${id}/like`)
}
