import request from '@/utils/request'

export function uploadFile(file, type) {
  const form = new FormData()
  form.append('file', file)
  form.append('type', type)
  return request.post('/api/file/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
