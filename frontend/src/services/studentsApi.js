import { get, post, put } from './http.js'

export function getStudents() {
  return get('/api/students')
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
