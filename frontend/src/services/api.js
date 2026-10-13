// All calls to the backend REST API go through this file.

async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(body?.message ?? `Request failed with status ${response.status}`)
  }
  return body
}

export function getHealth() {
  return request('/api/health')
}

export function getStudents() {
  return request('/api/students')
}

export function createStudent(student) {
  return request('/api/students', {
    method: 'POST',
    body: JSON.stringify(student),
  })
}
