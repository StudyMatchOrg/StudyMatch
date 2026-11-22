import { useEffect, useMemo, useState } from 'react'
import { login as requestLogin } from '../services/authApi.js'
import { clearSession, getSession, saveSession, SESSION_EXPIRED_EVENT } from '../services/session.js'
import { AuthContext } from './authContext.js'

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(getSession)

  useEffect(() => {
    const onExpired = () => setSession(null)
    window.addEventListener(SESSION_EXPIRED_EVENT, onExpired)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, onExpired)
  }, [])

  const value = useMemo(
    () => ({
      account: session?.account ?? null,
      isAdmin: session?.account.role === 'ADMIN',
      async login(username, password) {
        const result = await requestLogin(username, password)
        saveSession(result)
        setSession(result)
      },
      logout() {
        clearSession()
        setSession(null)
      },
    }),
    [session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
