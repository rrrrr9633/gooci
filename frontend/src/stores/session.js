import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import http from '../api/http'

export const useSessionStore = defineStore('session', () => {
  const token = ref(localStorage.getItem('gooci-token') || '')
  const profile = ref(null)
  const siteMeta = ref({ brandName: 'GOOCI 谷器', servicePhone: '' })

  const loggedIn = computed(() => Boolean(token.value))

  async function loadSite() {
    siteMeta.value = await http.get('/site/meta')
  }

  async function login(loginName, loginPassword) {
    const data = await http.post('/auth/login', { loginName, loginPassword })
    return applySession(data)
  }

  function applySession(data) {
    token.value = data.token
    localStorage.setItem('gooci-token', data.token)
    profile.value = data
    return data
  }

  async function loadSession() {
    if (!token.value) {
      profile.value = null
      return null
    }
    const data = await http.get('/console/session')
    profile.value = data
    return data
  }

  function logout() {
    token.value = ''
    profile.value = null
    localStorage.removeItem('gooci-token')
  }

  return { token, profile, siteMeta, loggedIn, loadSite, login, applySession, loadSession, logout }
})
