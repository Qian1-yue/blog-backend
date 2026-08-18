import { ApiError } from '@/api/request'

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message
  }
  return '请求失败，请稍后重试'
}
