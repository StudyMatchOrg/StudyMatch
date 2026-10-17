import { get, post } from './http.js'

export function getStudents() {
  return get('/api/students')
}

export function createStudent(student) {
  return post('/api/students', student)
}
