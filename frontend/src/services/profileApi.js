import { get } from './http.js'

export function getProfile(studentId) {
  return get(`/api/students/${studentId}/profile`)
}
