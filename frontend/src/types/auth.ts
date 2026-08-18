export interface LoginUser {
  id: number
  username: string
  nickname: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresInSeconds: number
  user: LoginUser
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload extends LoginPayload {
  nickname: string
}
