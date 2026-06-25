import request from './request'
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  UserVO
} from '@/types/auth'

export const authApi = {
  login(data: LoginRequest) {
    return request.post<ApiResponse<LoginResponse>>('/auth/login', data)
  },

  register(data: RegisterRequest) {
    return request.post<ApiResponse<RegisterResponse>>('/auth/register', data)
  },

  getMe() {
    return request.get<ApiResponse<UserVO>>('/auth/me')
  }
}
