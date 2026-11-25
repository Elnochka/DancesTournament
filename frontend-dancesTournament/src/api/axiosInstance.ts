

import axios, {
  type AxiosError,
  type InternalAxiosRequestConfig,
} from 'axios'

const api = axios.create({
  baseURL: '/api',
})

// =========================== REQUEST ===============================

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token')

    if (
      token &&
      !config.url?.includes('/auth/login') &&
      !config.url?.includes('/auth/register')
    ) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
)

// =========================== RESPONSE ==============================

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

api.interceptors.response.use(
  (res) => res,

  async (error: AxiosError) => {
    const originalRequest =
      error.config as RetryableRequestConfig | undefined

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true

      const refreshToken = localStorage.getItem('refresh_token')

      if (refreshToken) {
        try {
          const res = await axios.post<{ access: string }>(
            '/api/auth/refresh',
            {
              refresh: refreshToken,
            },
          )

          localStorage.setItem(
            'access_token',
            res.data.access,
          )

          originalRequest.headers.Authorization =
            `Bearer ${res.data.access}`

          return api(originalRequest)
        } catch (err) {
          localStorage.removeItem('access_token')
          localStorage.removeItem('refresh_token')

          window.location.href = '/'

          return Promise.reject(err)
        }
      }
    }

    return Promise.reject(error)
  },
)

export default api

