import request from '@/utils/request'

export function getStrategyList(params) {
  return request.get('/api/strategy/list', { params })
}

export function getStrategyById(id) {
  return request.get(`/api/strategy/${id}`)
}

export function createStrategy(data) {
  return request.post('/api/strategy', data)
}

export function updateStrategy(id, data) {
  return request.put(`/api/strategy/${id}`, data)
}

export function deleteStrategy(id) {
  return request.delete(`/api/strategy/${id}`)
}

export function getMyStrategies(params) {
  return request.get('/api/strategy/my', { params })
}

export function likeStrategy(id) {
  return request.post(`/api/strategy/${id}/like`)
}

export function getComments(id, params) {
  return request.get(`/api/strategy/${id}/comments`, { params })
}

export function addComment(id, data) {
  return request.post(`/api/strategy/${id}/comments`, data)
}

export function deleteComment(id) {
  return request.delete(`/api/comment/${id}`)
}
