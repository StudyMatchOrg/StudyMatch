// Shared helper for calling the backend REST API. Each feature has its own service file
// (studentsApi.js, recordsApi.js, ...) built on these functions, so features do not
// have to edit the same file.

export async function request(path, options = {}) {
  const isUpload = options.body instanceof FormData
  const response = await fetch(path, {
    ...options,
    headers: isUpload ? options.headers : { 'Content-Type': 'application/json', ...options.headers },
  })
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
