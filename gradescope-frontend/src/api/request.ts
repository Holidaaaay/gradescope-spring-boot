import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ElNotification } from 'element-plus'
import type { ApiResponse } from '@/types/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动注入 JWT
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理后端 Result<T> 与 HTTP 错误
request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse<unknown>
    if (data.code !== 200) {
      ElNotification.error({
        title: '请求失败',
        message: data.message || '操作未能完成，请稍后重试'
      })
      return Promise.reject(new Error(data.message))
    }
    return response
  },
  (error: AxiosError) => {
    if (error.response) {
      const status = error.response.status
      const data = error.response.data as ApiResponse<unknown> | undefined

      if (status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
        ElNotification.error({
          title: '登录已过期',
          message: '请重新登录'
        })
      } else if (status === 403) {
        ElNotification.error({
          title: '没有权限',
          message: data?.message || '你没有执行该操作的权限'
        })
      } else if (status >= 500) {
        ElNotification.error({
          title: '服务器错误',
          message: '服务器内部错误，请稍后重试'
        })
      } else {
        ElNotification.error({
          title: '请求失败',
          message: data?.message || '操作未能完成，请稍后重试'
        })
      }
    } else {
      ElNotification.error({
        title: '网络错误',
        message: '网络连接失败，请检查网络或稍后重试'
      })
    }
    return Promise.reject(error)
  }
)

export default request
