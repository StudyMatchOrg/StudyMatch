// The login is kept in sessionStorage: it survives reloading the page, but not closing the
// browser tab. The token is sent with every request by http.js.

const KEY = 'studymatch.session'

// Sent by http.js when the backend rejects the token, so the app goes back to the login page.
export const SESSION_EXPIRED_EVENT = 'studymatch:session-expired'

export function getSession() {
  try {
    const session = JSON.parse(sessionStorage.getItem(KEY))
    if (session && new Date(session.expiresAt) > new Date()) {
      return session
    }
  } catch {
    // An unreadable session is treated as no session.
  }
  sessionStorage.removeItem(KEY)
  return null
}

export function saveSession(session) {
  sessionStorage.setItem(KEY, JSON.stringify(session))
}

export function clearSession() {
  sessionStorage.removeItem(KEY)
}
