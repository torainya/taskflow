import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import http from '../api/http'

const TOKEN_KEY = 'tf_token'
const USER_KEY = 'tf_user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(readUser())

  const isLoggedIn = computed(() => Boolean(token.value))
  const displayName = computed(() => user.value?.nickname || user.value?.username || '')

  function setSession(newToken, newUser) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem(TOKEN_KEY, newToken)
    localStorage.setItem(USER_KEY, JSON.stringify(newUser))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(payload) {
    const { data } = await http.post('/auth/login', payload)
    setSession(data.token, data.user)
  }

  async function register(payload) {
    const { data } = await http.post('/auth/register', payload)
    setSession(data.token, data.user)
  }

  return { token, user, isLoggedIn, displayName, login, register, logout }
})
