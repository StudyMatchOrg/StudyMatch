import { useContext } from 'react'
import { AuthContext } from './authContext.js'

// The logged-in account and the login/logout actions.
export function useAuth() {
  return useContext(AuthContext)
}
