import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'
import { setToken, setUser, clearAuth, getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const isLoggedIn = ref(!!getToken())

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const data = res.data
    token.value = data.token
    setToken(data.token)
    setUser({ userId: data.userId, username: data.username, name: data.name })
    userInfo.value = { userId: data.userId, username: data.username, name: data.name }
    isLoggedIn.value = true
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      userInfo.value = null
      isLoggedIn.value = false
      clearAuth()
    }
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
  }

  return { token, userInfo, isLoggedIn, login, logout, fetchUserInfo }
})
