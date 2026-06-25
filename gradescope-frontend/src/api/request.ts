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

// Request interceptor: inject JWT
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

// Response interceptor: handle errors uniformly
request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse<unknown>
    if (data.code !== 200) {
      ElNotification.error({
        title: 'Error',
        message: data.message || 'Request failed'
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
      } else if (status === 403) {
        ElNotification.error({
          title: 'Forbidden',
          message: data?.message || 'You do not have permission'
        })
      } else if (status >= 500) {
        ElNotification.error({
          title: 'Server Error',
          message: 'Internal server error, please try again later'
        })
      } else {
        ElNotification.error({
          title: 'Error',
          message: data?.message || 'Request failed'
        })
      }
    } else {
      ElNotification.error({
        title: 'Network Error',
        message: 'Network connection failed, please check your connection'
      })
    }
    return Promise.reject(error)
  }
)

export default request
