

import {
  useState,
  type ChangeEvent,

} from 'react'

import { useNavigate } from 'react-router-dom'
import useAuth from '../hooks/useAuth'
import './Auth.css'

interface RegisterForm {
  email: string
  password: string
  confirmPassword: string
  name: string
  lastName: string
}

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState<RegisterForm>({
    email: '',
    password: '',
    name: '',
    lastName: '',
    confirmPassword: '',
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

    if (form.password !== form.confirmPassword) {
            setError("Паролі не співпадають");
            return;
        }

        // setLoading(true);

    try {
      await register(form)

      alert(
        'Registration successful! Please login.'
      )

      navigate('/')
    } catch (err: any) {
      console.error(err)

      setError(
        err.response?.data?.message ||
          'Registration failed'
      )
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h2>Create account</h2>
          <p>Register a new account</p>
        </div>

        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >
          <div className="form-group">
            <label htmlFor="name">
              First Name
            </label>

            <input
              id="name"
              type="text"
              name="name"
              placeholder="Enter your first name"
              value={form.name}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="lastName">
              Last Name
            </label>

            <input
              id="lastName"
              type="text"
              name="lastName"
              placeholder="Enter your last name"
              value={form.lastName}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">
              Email
            </label>

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
            <label htmlFor="password">
              Password
            </label>

            <input
              id="password"
              type="password"
              name="password"
              placeholder="Create a password"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">
              Confirm Password
            </label>

            <input
              id="confirmPassword"
              type="password"
              name="confirmPassword"
              placeholder="Confirm your password"
              value={form.confirmPassword}
              onChange={handleChange}
              required
            />
          </div>

          <button
            type="submit"
            className="auth-button"
          >
            Register
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

