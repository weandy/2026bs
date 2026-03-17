import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Token 刷新状态
let isRefreshing = false
let requestQueue = []

function processQueue(error, token = null) {
  requestQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  requestQueue = []
}

// 请求拦截器：添加 Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  async error => {
    const originalRequest = error.config
    const status = error.response?.status

    // 登录接口本身的 401 = 用户名或密码错误，不触发续签
    if (status === 401 && originalRequest.url?.includes('/auth/')) {
      return Promise.reject(error)
    }

    // 其他接口的 401: 尝试用 refreshToken 静默续签
    if (status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        goLogin()
        return Promise.reject(error)
      }

      if (isRefreshing) {
        // 加入队列等待刷新完成
        return new Promise((resolve, reject) => {
          requestQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return request(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const res = await axios.post('/api/auth/refresh', { refreshToken })
        const { accessToken, refreshToken: newRefresh } = res.data.data
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', newRefresh)

        // 重发当前请求 + 排队请求
        processQueue(null, accessToken)
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return request(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError)
        goLogin()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    if (status === 403) {
      ElNotification({ title: '权限拒绝', message: '您无权访问该资源', type: 'error' })
    } else if (status === 400) {
      const data = error.response?.data
      ElMessage.error(data?.message || '请求参数错误')
    } else if (status !== 401) {
      ElNotification({ title: '系统错误', message: error.response?.data?.message || '服务器异常，请稍后重试', type: 'error', duration: 5000 })
    }
    return Promise.reject(error)
  }
)

/**
 * 强制跳转登录页，同时清理 localStorage + Pinia 内存态
 * 在 401 刷新失败 或 外部主动调用时触发
 */
function goLogin() {
  ElMessage.error('登录已过期，请重新登录')
  // 同时清 Pinia store（内存）+ localStorage（持久化）
  try { useUserStore().logout() } catch (e) { /* store 可能尚未挂载 */ }
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

/**
 * 主动登出：先通知后端废弃 refresh token，再清前端态
 */
export async function forceLogout() {
  try {
    const rt = localStorage.getItem('refreshToken')
    await request.post('/auth/logout', { refreshToken: rt })
  } catch (e) { /* 忽略——即使后端失败也要清前端 */ }
  try { useUserStore().logout() } catch (e) {}
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

export default request

