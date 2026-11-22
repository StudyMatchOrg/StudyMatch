import { get, post } from './http.js'

// Returns { token, expiresAt, account }.
export function login(username, password) {
  return post('/api/auth/login', { username, password })
}

export function getMe() {
  return get('/api/auth/me')
}
