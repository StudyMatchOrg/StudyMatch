import { get, put } from './http.js'

export function getAssessments(studentId) {
  return get(`/api/students/${studentId}/assessments`)
}

// Replaces the whole set: competencies left out (or with a null value) are no longer assessed.
export function saveAssessments(studentId, assessments) {
  return put(`/api/students/${studentId}/assessments`, { assessments })
}
