

export type Role =
  | 'ADMIN'
  | 'JUDGE'
  | 'DANCER'
  | 'TRAINER'

export interface User {
  id: number
  name: string
  lastName: string
  email: string
  role: Role
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
  lastName: string
}

export interface AuthResponse {
  access: string
  refresh?: string
}

export type Permission =
  | 'trainers.edit'
  | 'marks.edit'
  | 'clubs.edit'
  | 'all.view'
  | 'finalMark.edit'

