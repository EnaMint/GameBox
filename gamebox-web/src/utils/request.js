import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gb_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      handleUnauthorized(res.message)
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      handleUnauthorized()
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后再试')
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized(message) {
  localStorage.removeItem('gb_token')
  localStorage.removeItem('gb_user')
  if (router.currentRoute.value.path !== '/login') {
    ElMessage.warning(message || '请先登录')
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  }
}

export default request
