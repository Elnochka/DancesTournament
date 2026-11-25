
import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import useAuth from '../hooks/useAuth'
import type { Role } from '../types/auth'

interface ProtectedRouteWithRoleProps {
  roles: Role[]
  children: ReactNode
}

export default function ProtectedRouteWithRole({
  roles,
  children,
}: ProtectedRouteWithRoleProps) {
  const { user, loading } = useAuth()

  if (loading) {
    return <p>Loading...</p>
  }

  if (!user) {
    return <Navigate to="/login" />
  }

  if (!roles.includes(user.role)) {
    return <Navigate to="/403" />
  }

  return children
}