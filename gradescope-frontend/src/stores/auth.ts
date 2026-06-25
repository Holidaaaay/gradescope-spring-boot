import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { UserVO } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<UserVO | null>(null)
  const loading = ref(false)

  // Getters
  const isAuthenticated = computed(() => !!token.value)

  // Actions
  function setToken(newToken: string | null) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  async function login(username: string, password: string) {
    loading.value = true
    try {
      const response = await authApi.login({ username, password })
      const { token: newToken } = response.data.data
      setToken(newToken)
      return true
    } catch {
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(data: {
    username: string
    password: string
    realName?: string
    email?: string
    phone?: string
    userNo?: string
  }) {
    loading.value = true
    try {
      await authApi.register(data)
      return true
    } catch {
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const response = await authApi.getMe()
      user.value = response.data.data
    } catch {
      user.value = null
    }
  }

  function logout() {
    setToken(null)
    user.value = null
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    login,
    register,
    fetchUser,
    logout
  }
})
