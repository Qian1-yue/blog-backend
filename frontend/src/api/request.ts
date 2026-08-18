import axios, { type AxiosRequestConfig } from 'axios'

import type { ApiResult } from '@/types/api'

const TOKEN_KEY = 'blog_token'

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status?: number,
    public readonly code?: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

let unauthorizedHandler: (() => void) | undefined

export function setUnauthorizedHandler(handler: () => void): void {
  unauthorizedHandler = handler
}

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 12_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError<ApiResult<unknown>>(error)) {
      const status = error.response?.status
      const url = error.config?.url ?? ''
      const hasToken = Boolean(localStorage.getItem(TOKEN_KEY))
      const isLoginRequest = url.includes('/api/auth/login')

      if (status === 401 && hasToken && !isLoginRequest) {
        unauthorizedHandler?.()
      }

      const message =
        error.response?.data?.msg ||
        (error.code === 'ECONNABORTED'
          ? '请求超时，请检查网络后重试'
          : error.response
            ? '请求失败，请稍后重试'
            : '无法连接服务器，请确认后端已经启动')

      return Promise.reject(
        new ApiError(message, status, error.response?.data?.code),
      )
    }

    return Promise.reject(new ApiError('请求失败，请稍后重试'))
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await client.request<ApiResult<T>>(config)
  const result = response.data

  if (result.code < 200 || result.code >= 300) {
    throw new ApiError(result.msg || '请求失败', response.status, result.code)
  }

  return result.data
}
