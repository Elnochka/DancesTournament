
import { useEffect, useState } from 'react'
import api from '../api/axiosInstance'
import './UsersAdmin.css'

type Role =
  | 'ADMIN'
  | 'JUDGE'
  | 'DANCER'
  | 'TRAINER'

interface User {
  id: number
  email: string
  name: string
  lastName: string
  // active: boolean
  role: Role
}

export default function UsersAdmin() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [editingUser, setEditingUser] =
    useState<User | null>(null)

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      const response = await api.get<User[]>('/users')
      setUsers(response.data)
    } catch (error) {
      console.error('Users load error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (user: User) => {
    setEditingUser({ ...user })
  }

  const handleCancel = () => {
    setEditingUser(null)
  }

  const handleSave = async () => {
    if (!editingUser) {
      return
    }

    try {
      await api.patch(
        `/users/${editingUser.id}`,
        {
          email: editingUser.email,
          name: editingUser.name,
          lastName: editingUser.lastName,
          // active: editingUser.active,
          role: editingUser.role,
        },
      )

      setUsers(prev =>
        prev.map(user =>
          user.id === editingUser.id
            ? editingUser
            : user,
        ),
      )

      setEditingUser(null)
    } catch (error) {
      console.error('User update error:', error)
    }
  }

  if (loading) {
    return <p>Loading...</p>
  }

  return (
    <div className="users-admin">
      <h2>Users</h2>

      <table className="users-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Name</th>
            <th>Last name</th>
            {/* <th>Active</th> */}
            <th>Role</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {users.map(user => (
            <tr key={user.id}>
              <td>{user.id}</td>
              <td>{user.email}</td>
              <td>{user.name}</td>
              <td>{user.lastName}</td>

              {/* <td>
                {user.active ? 'Yes' : 'No'}
              </td> */}

              <td>{user.role}</td>

              <td>
                <button
                  onClick={() => handleEdit(user)}
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {editingUser && (
        <div className="user-edit">
          <h3>Edit User</h3>

          <div className="user-edit-form">
            <div className="user-edit-field">
              <label>Email</label>

              <input
                value={editingUser.email}
                onChange={e =>
                  setEditingUser({
                    ...editingUser,
                    email: e.target.value,
                  })
                }
              />
            </div>

            <div className="user-edit-field">
              <label>Name</label>

              <input
                value={editingUser.name}
                onChange={e =>
                  setEditingUser({
                    ...editingUser,
                    name: e.target.value,
                  })
                }
              />
            </div>

            <div className="user-edit-field">
              <label>Last name</label>

              <input
                value={editingUser.lastName}
                onChange={e =>
                  setEditingUser({
                    ...editingUser,
                    lastName: e.target.value,
                  })
                }
              />
            </div>

            <div className="user-edit-field">
              <label>Role</label>

              <select
                value={editingUser.role}
                onChange={e =>
                  setEditingUser({
                    ...editingUser,
                    role: e.target.value as Role,
                  })
                }
              >
                <option value="ADMIN">
                  ADMIN
                </option>

                <option value="JUDGE">
                  JUDGE
                </option>

                <option value="DANCER">
                  DANCER
                </option>

                <option value="TRAINER">
                  TRAINER
                </option>
              </select>
            </div>

            {/* <label className="user-edit-checkbox">
              <input
                type="checkbox"
                checked={editingUser.active}
                onChange={e =>
                  setEditingUser({
                    ...editingUser,
                    active: e.target.checked,
                  })
                }
              />

              Active
            </label> */}

            <div className="user-edit-actions">
              <button
                className="user-edit-save"
                onClick={handleSave}
              >
                Save
              </button>

              <button
                className="user-edit-cancel"
                onClick={handleCancel}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

// import { useEffect, useState } from 'react'
// import api from '../api/axiosInstance'
// import './UsersAdmin.css'

// type Role =
//   | 'ADMIN'
//   | 'JUDGE'
//   | 'DANCER'
//   | 'TRAINER'

// interface User {
//   id: number
//   email: string
//   name: string
//   lastName: string
//   active: boolean
//   role: Role
// }

// export default function UsersAdmin() {
//   const [users, setUsers] = useState<User[]>([])
//   const [loading, setLoading] = useState(true)

//   useEffect(() => {
//     loadUsers()
//   }, [])

//   const loadUsers = async () => {
//     try {
//       const response = await api.get<User[]>('/users')
//       setUsers(response.data)
//     } catch (error) {
//       console.error('Users load error:', error)
//     } finally {
//       setLoading(false)
//     }
//   }

//   if (loading) {
//     return <p>Loading...</p>
//   }

//   return (
//     <div className="users-admin">
//       <h2>Users</h2>

//       <table className="users-table">
//         <thead>
//           <tr>
//             <th>ID</th>
//             <th>Email</th>
//             <th>Name</th>
//             <th>Last name</th>
//             <th>Active</th>
//             <th>Role</th>
//             <th>Actions</th>
//           </tr>
//         </thead>

//         <tbody>
//           {users.map(user => (
//             <tr key={user.id}>
//               <td>{user.id}</td>
//               <td>{user.email}</td>
//               <td>{user.name}</td>
//               <td>{user.lastName}</td>
//               <td>
//                 {user.active ? 'Yes' : 'No'}
//               </td>
//               <td>{user.role}</td>
//               <td>
//                 <button>
//                   Edit
//                 </button>
//               </td>
//             </tr>
//           ))}
//         </tbody>
//       </table>
//     </div>
//   )
// }