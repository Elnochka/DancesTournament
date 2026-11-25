

import { useState, type ChangeEvent } from 'react'
import useAuth from '../hooks/useAuth'
import './Auth.css'

interface LoginForm {
  email: string
  password: string
}

export default function Login() {
  const { login } = useAuth()

  const [form, setForm] = useState<LoginForm>({
    email: '',
    password: '',
  })

  const [error, setError] = useState<string | null>(null)

  const handleChange = (
    e: ChangeEvent<HTMLInputElement>
  ) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    })
  }

  const handleSubmit = async (
    e: React.SubmitEvent<HTMLFormElement>
  ) => {
    e.preventDefault()
    setError(null)

    try {
      await login(form)
    } catch (err: any) {
      console.error(err)

      setError(
        err.response?.data?.message ||
          'Login failed'
      )
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h2>Welcome back</h2>
          <p>Sign in to your account</p>
        </div>

        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >
          <div className="form-group">
            <label htmlFor="email">Email</label>

            <input
              id="email"
              type="email"
              name="email"
              placeholder="Enter your email"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>

            <input
              id="password"
              type="password"
              name="password"
              placeholder="Enter your password"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>

          <button
            type="submit"
            className="auth-button"
          >
            Login
          </button>
        </form>

        {error && (
          <p className="auth-error">
            {error}
          </p>
        )}
      </div>
    </div>
  )
}

