import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('gb_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('gb_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(form) {
    const data = await loginApi(form)
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
      bio: data.bio
    }
    localStorage.setItem('gb_token', data.token)
    localStorage.setItem('gb_user', JSON.stringify(userInfo.value))
    return data
  }

  function updateProfile(profile) {
    userInfo.value = { ...userInfo.value, ...profile }
    localStorage.setItem('gb_user', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('gb_token')
    localStorage.removeItem('gb_user')
  }

  return { token, userInfo, isLoggedIn, login, updateProfile, logout }
})
