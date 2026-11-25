

import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

import useAuth from '../hooks/useAuth'
import './Home.css'

export default function Home() {
  const { user, logout, hasRole, loading } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (!loading && user) {
      if (hasRole('ADMIN')) {
        navigate('/admin')
      } else if (hasRole('JUDGE')) {
        navigate('/judge')
      } else if (hasRole('DANCER')) {
        navigate('/viewer')
      } else if (hasRole('TRAINER')) {
        navigate('/trainer')
      }
    }
  }, [user, loading, hasRole, navigate])

  if (loading) {
    return (
      <div className="home-page">
        <div className="home-loading">
          Loading...
        </div>
      </div>
    )
  }

  if (!user) {
    return (
      <div className="home-page">
        <div className="home-card home-welcome-card">
          <div className="home-header">
            <h1>Dance Management</h1>
            <p>
              Welcome to the dance competition platform
            </p>
          </div>

          <div className="home-actions">
            <button
              className="home-button home-button-primary"
              onClick={() => navigate('/login')}
            >
              Login
            </button>

            <button
              className="home-button home-button-secondary"
              onClick={() => navigate('/register')}
            >
              Register
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="home-page">
      <div className="home-card">
        <div className="home-header">
          <h1>Welcome, {user.name}</h1>

          <p>
            You are successfully logged in.
          </p>
        </div>

        <button
          className="home-button home-button-secondary"
          onClick={logout}
        >
          Logout
        </button>

        <div className="home-panels">
          {hasRole('ADMIN') && (
            <div className="home-panel">
              Admin Panel
            </div>
          )}

          {hasRole('JUDGE') && (
            <div className="home-panel">
              Judge Panel
            </div>
          )}

          {hasRole('DANCER') && (
            <div className="home-panel">
              Dancer Panel
            </div>
          )}

          {hasRole('TRAINER') && (
            <div className="home-panel">
              Trainer Panel
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

