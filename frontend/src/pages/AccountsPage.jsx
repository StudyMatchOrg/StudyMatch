import { useCallback, useEffect, useState } from 'react'
import { createAccount, getAccounts } from '../services/accountsApi.js'

const EMPTY_ACCOUNT = { username: '', displayName: '', password: '', role: 'TEACHER' }

// Admin only: who can log in to StudyMatch (UC1).
export default function AccountsPage() {
  const [accounts, setAccounts] = useState([])
  const [account, setAccount] = useState(EMPTY_ACCOUNT)
  const [error, setError] = useState(null)
  const [created, setCreated] = useState(null)

  const loadAccounts = useCallback(
    () =>
      getAccounts()
        .then(setAccounts)
        .catch((err) => setError(err.message)),
    [],
  )

  useEffect(() => {
    loadAccounts()
  }, [loadAccounts])

  function handleChange(event) {
    setAccount({ ...account, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setCreated(null)
    try {
      const saved = await createAccount(account)
      setCreated(saved.username)
      setAccount(EMPTY_ACCOUNT)
      await loadAccounts()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <h1>Accounts</h1>

      <section>
        <h2>New account</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>
              Username
              <input name="username" value={account.username} onChange={handleChange} required />
            </label>
            <label>
              Name
              <input name="displayName" value={account.displayName} onChange={handleChange} required />
            </label>
            <label>
              Password (at least 8 characters)
              <input
                name="password"
                type="password"
                minLength={8}
                value={account.password}
                onChange={handleChange}
                autoComplete="new-password"
                required
              />
            </label>
            <label>
              Role
              <select name="role" value={account.role} onChange={handleChange}>
                <option value="TEACHER">Teacher</option>
                <option value="ADMIN">Administrator</option>
              </select>
            </label>
          </div>
          <button type="submit">Create account</button>
          {created && <p className="alert alert-success">Account {created} created.</p>}
          {error && <p className="alert alert-error">{error}</p>}
        </form>
      </section>

      <section>
        <h2>All accounts</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Username</th>
                <th>Name</th>
                <th>Role</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((a) => (
                <tr key={a.id}>
                  <td>{a.username}</td>
                  <td>{a.displayName}</td>
                  <td>
                    <span className={a.role === 'ADMIN' ? 'badge badge-accent' : 'badge'}>
                      {a.role === 'ADMIN' ? 'Administrator' : 'Teacher'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  )
}
