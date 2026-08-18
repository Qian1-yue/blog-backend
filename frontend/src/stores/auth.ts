import { defineStore } from 'pinia'

import * as authApi from '@/api/auth'
import type { LoginPayload, LoginUser, RegisterPayload } from '@/types/auth'

const TOKEN_KEY = 'blog_token'
const USER_KEY = 'blog_user'
const EXPIRES_AT_KEY = 'blog_token_expires_at'

function storedUser(): LoginUser | null {
  const value = localStorage.getItem(USER_KEY)
  if (!value) return null

  try {
    return JSON.parse(value) as LoginUser
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) ?? '',
    user: storedUser() as LoginUser | null,
    expiresAt: Number(localStorage.getItem(EXPIRES_AT_KEY) || 0),
    restoring: false,
  }),
  getters: {
    isLoggedIn: (state) =>
      Boolean(state.token) && (!state.expiresAt || state.expiresAt > Date.now()),
  },
  actions: {
    async login(payload: LoginPayload) {
      const result = await authApi.login(payload)
      this.token = result.token
      this.user = result.user
      this.expiresAt = Date.now() + result.expiresInSeconds * 1000

      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      localStorage.setItem(EXPIRES_AT_KEY, String(this.expiresAt))
    },
    register(payload: RegisterPayload) {
      return authApi.register(payload)
    },
    async logout() {
      try {
        if (this.token) await authApi.logout()
      } finally {
        this.clearSession()
      }
    },
    clearSession() {
      this.token = ''
      this.user = null
      this.expiresAt = 0
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      localStorage.removeItem(EXPIRES_AT_KEY)
    },
    async restoreSession() {
      if (!this.token) return
      if (this.expiresAt && this.expiresAt <= Date.now()) {
        this.clearSession()
        return
      }

      this.restoring = true
      try {
        this.user = await authApi.getCurrentUser()
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      } catch {
        this.clearSession()
      } finally {
        this.restoring = false
      }
    },
  },
})
