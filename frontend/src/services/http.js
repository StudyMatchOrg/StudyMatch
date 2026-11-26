// Shared helper for calling the backend REST API. Each feature has its own service file
// (studentsApi.js, recordsApi.js, ...) built on these functions, so features do not
// have to edit the same file.
import { clearSession, getSession, SESSION_EXPIRED_EVENT } from './session.js'

function headersFor(options) {
  const token = getSession()?.token
  return {
    ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  }
}

export async function request(path, options = {}) {
  const response = await fetch(path, { ...options, headers: headersFor(options) })
  if (response.status === 401 && getSession()) {
    // The login expired or is no longer valid: go back to the login page.
    clearSession()
    window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT))
  }
  if (response.status === 204) {
    return null
  }
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(body?.message ?? `Request failed with status ${response.status}`)
  }
  return body
}

export const get = (path) => request(path)

export const post = (path, data) => request(path, { method: 'POST', body: JSON.stringify(data) })

export const put = (path, data) => request(path, { method: 'PUT', body: JSON.stringify(data) })

export const del = (path) => request(path, { method: 'DELETE' })

export const upload = (path, formData) => request(path, { method: 'POST', body: formData })

// Downloads a file (e.g. a CSV export) with the login token and saves it with the name sent by
// the backend. A plain link cannot be used, because it would not send the token.
export async function download(path) {
  const token = getSession()?.token
  const response = await fetch(path, { headers: token ? { Authorization: `Bearer ${token}` } : {} })
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? `Download failed with status ${response.status}`)
  }
  const disposition = response.headers.get('Content-Disposition') ?? ''
  const fileName = /filename="?([^";]+)"?/.exec(disposition)?.[1] ?? 'download'
  const url = URL.createObjectURL(await response.blob())
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}
