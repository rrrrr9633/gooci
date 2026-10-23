import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('gooci-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      const data = response.data
      if (data instanceof Blob && /json/i.test(data.type || '')) {
        return data.text().then((text) => {
          try {
            const body = JSON.parse(text)
            if (body && body.code !== 0) {
              ElMessage.error(body.message || '请求失败')
              return Promise.reject(new Error(body.message))
            }
          } catch {
            /* 按文件下载 */
          }
          return data
        })
      }
      return data
    }
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
      if (window.location.pathname.startsWith('/ops')) {
        return Promise.reject(error)
      }
      localStorage.removeItem('gooci-token')
      if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register')) {
        window.location.href = '/login'
      }
    }
    ElMessage.error(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

export default http
