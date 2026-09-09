import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 统一 axios 实例：baseURL=/api，请求自动带 Bearer token
const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('tf_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    // 未登录 / token 过期：清掉本地登录态并回登录页（避免死循环：仅非登录接口触发）
    if (status === 401 && !error.config.url.includes('/auth/')) {
      localStorage.removeItem('tf_token')
      localStorage.removeItem('tf_user')
      router.push('/login')
    }
    const message = data?.message || error.message || '网络异常，请稍后再试'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
)

export default http
