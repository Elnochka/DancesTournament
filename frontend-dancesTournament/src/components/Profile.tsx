
import useAuth from '../hooks/useAuth'

export default function Profile() {
  const { user } = useAuth()

  if (!user) {
    return <p>Loading...</p>
  }

  return (
    <div style={{ maxWidth: 500, margin: '50px auto' }}>
      <h2>Profile</h2>

      <p>
        <strong>First Name:</strong> {user.name}
      </p>

      <p>
        <strong>Last Name:</strong> {user.lastName}
      </p>

      <p>
        <strong>Email:</strong> {user.email}
      </p>

      <p>
        <strong>Role:</strong> {user.role}
      </p>
    </div>
  )
}