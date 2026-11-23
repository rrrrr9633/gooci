import axios from 'axios'
import { ElMessage } from 'element-plus'

const opsHttp = axios.create({
  baseURL: '/api/ops',
  timeout: 15000
})

opsHttp.interceptors.request.use((config) => {
  const token = localStorage.getItem('gooci-ops-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

opsHttp.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body.data
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('gooci-ops-token')
      if (!window.location.pathname.startsWith('/ops/login')) {
        window.location.href = '/ops/login'
      }
    }
    ElMessage.error(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

export default opsHttp
