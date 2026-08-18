import { request } from '@/api/request'
import type {
  LoginPayload,
  LoginResult,
  LoginUser,
  RegisterPayload,
} from '@/types/auth'

export function login(payload: LoginPayload): Promise<LoginResult> {
  return request({ method: 'POST', url: '/api/auth/login', data: payload })
}

export function register(payload: RegisterPayload): Promise<LoginUser> {
  return request({ method: 'POST', url: '/api/auth/register', data: payload })
}

export function getCurrentUser(): Promise<LoginUser> {
  return request({ method: 'GET', url: '/api/auth/me' })
}

export function logout(): Promise<Record<string, never>> {
  return request({ method: 'POST', url: '/api/auth/logout' })
}
