import { createContext } from 'react'

// { account, isAdmin, login(username, password), logout() }, provided by AuthProvider.
export const AuthContext = createContext(null)
