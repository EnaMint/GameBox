import request from '@/utils/request'

export function getMyGames(params) {
  return request.get('/api/ugame/list', { params })
}

export function checkGame(gameId) {
  return request.get(`/api/ugame/check/${gameId}`)
}

export function addGame(data) {
  return request.post('/api/ugame', data)
}

export function updateGame(id, data) {
  return request.put(`/api/ugame/${id}`, data)
}

export function removeGame(id) {
  return request.delete(`/api/ugame/${id}`)
}
