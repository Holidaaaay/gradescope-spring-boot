export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  realName?: string
  email?: string
  phone?: string
  userNo?: string
}

export interface LoginResponse {
  token: string
  tokenType: string
}

export interface RegisterResponse {
  userId: number
  username: string
}

export interface UserVO {
  id: number
  username: string
  realName: string
  email: string
  phone?: string
  userNo?: string
  avatarUrl?: string
  gender?: number
  status: number
}
