import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const domain = computed(() => userInfo.value?.domain || '')
  const role = computed(() => userInfo.value?.roleCode || userInfo.value?.role || '')

  function setLogin(data) {
    token.value = data.accessToken
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)

    const info = { ...data }
    delete info.accessToken
    delete info.refreshToken
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, domain, role, setLogin, logout }
})
