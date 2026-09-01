import request from '@/utils/request'

export function followUser(id) {
  return request.post(`/api/user/follow/${id}`)
}

export function unfollowUser(id) {
  return request.delete(`/api/user/follow/${id}`)
}

export function getFollows(id, params) {
  return request.get(`/api/user/${id}/follows`, { params })
}

export function getFans(id, params) {
  return request.get(`/api/user/${id}/fans`, { params })
}
