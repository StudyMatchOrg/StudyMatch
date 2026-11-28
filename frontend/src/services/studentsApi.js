import { del, get, post, put } from './http.js'

// Without filters, every student. Filters: q, degreeId, enrolmentYear, courseUnitId,
// competency, minScore, lowEvidence and sort (number, name or score). Empty ones are left out.
export function getStudents(filters = {}) {
  const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value !== '' && value != null))
  const text = query.toString()
  return get(text ? `/api/students?${text}` : '/api/students')
}

export function getStudent(id) {
  return get(`/api/students/${id}`)
}

export function createStudent(student) {
  return post('/api/students', student)
}

export function updateStudent(id, student) {
  return put(`/api/students/${id}`, student)
}

// Admin only. Refused when the student is in saved groups.
export function deleteStudent(id) {
  return del(`/api/students/${id}`)
}
