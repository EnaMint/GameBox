import request from '@/utils/request'

export function getGameList(params) {
  return request.get('/api/game/list', { params })
}

export function getGameById(id) {
  return request.get(`/api/game/${id}`)
}
