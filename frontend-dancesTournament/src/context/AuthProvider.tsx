

import {
  createContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import { useNavigate } from 'react-router-dom'

import api from '../api/axiosInstance'

import type {
  AuthResponse,
  LoginRequest,
  Permission,
  RegisterRequest,
  Role,
  User,
} from '../types/auth'

interface AuthContextType {
  user: User | null
  loading: boolean
  login: (data: LoginRequest) => Promise<void>
  register: (data: RegisterRequest) => Promise<unknown>
  logout: () => void
  hasRole: (role: Role) => boolean
  can: (permission: Permission) => boolean
}

export const AuthContext = createContext<AuthContextType | null>(null)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const navigate = useNavigate()

  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const init = async () => {
      const token = localStorage.getItem('access_token')

      if (!token) {
        setLoading(false)
        return
      }

      try {
        const res = await api.get<User>('/auth/me')
        setUser(res.data)
      } catch (err) {
        console.error('Auth init error:', err)

        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
      } finally {
        setLoading(false)
      }
    }

    init()
  }, [])

  // =========================== AUTH ===============================

  const register = async ({
    email,
    password,
    name,
    lastName,

  }: RegisterRequest) => {
    const res = await api.post('/auth/register', {
      email,
      password,
      name,
      lastName,
      // role,
    })

    return res.data
  }

  const login = async ({
    email,
    password,
  }: LoginRequest): Promise<void> => {
    const res = await api.post<AuthResponse>('/auth/login', {
      email,
      password,
    })

    localStorage.setItem('access_token', res.data.access)

    if (res.data.refresh) {
      localStorage.setItem('refresh_token', res.data.refresh)
    }

    // Отримати профіль користувача
    const profileRes = await api.get<User>('/auth/me')

    setUser(profileRes.data)

    // Редірект залежно від ролі
    switch (profileRes.data.role) {
      case 'ADMIN':
        navigate('/admin')
        break

      case 'JUDGE':
        //navigate('/marks')
        navigate('/judge')
        break

      case 'DANCER':
        navigate('/')
        break

      default:
        navigate('/')
    }
  }

  const logout = () => {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')

    setUser(null)

    navigate('/')
  }

  // =========================== RBAC ===============================

  const hasRole = (role: Role): boolean => {
    return user?.role === role
  }

  const can = (permission: Permission): boolean => {
    if (!user) {
      return false
    }

    const ROLE_PERMISSIONS: Record<Role, Permission[]> = {
      ADMIN: [
        'trainers.edit',
        'marks.edit',
        'clubs.edit',
        'all.view',
      ],

      JUDGE: [
        'marks.edit',
        'finalMark.edit',
        'all.view',
      ],

      DANCER: [
        'all.view',
      ],

      TRAINER: [],
    }

    return ROLE_PERMISSIONS[user.role].includes(permission)
  }

  // =================================================================

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        hasRole,
        can,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

