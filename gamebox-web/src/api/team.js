import request from '@/utils/request'

export function getTeamList(params) {
  return request.get('/api/team/list', { params })
}

export function getTeamById(id) {
  return request.get(`/api/team/${id}`)
}

export function createTeam(data) {
  return request.post('/api/team', data)
}

export function updateTeamStatus(id, status) {
  return request.put(`/api/team/${id}/status`, { status })
}

export function deleteTeam(id) {
  return request.delete(`/api/team/${id}`)
}

export function getMyTeams(params) {
  return request.get('/api/team/my', { params })
}

export function applyTeam(id, data) {
  return request.post(`/api/team/${id}/apply`, data)
}

export function withdrawApplication(id) {
  return request.delete(`/api/team/${id}/apply`)
}

export function getTeamApplications(id) {
  return request.get(`/api/team/${id}/applications`)
}

export function auditApplication(id, action) {
  return request.put(`/api/team/application/${id}`, { action })
}

export function getMyApplications() {
  return request.get('/api/team/applications/mine')
}
