import { get, post } from './http.js'

export function getDegrees() {
  return get('/api/degrees')
}

export function getCourseUnits(degreeId) {
  return get(degreeId ? `/api/course-units?degreeId=${degreeId}` : '/api/course-units')
}

// Admin only.
export function createDegree(degree) {
  return post('/api/degrees', degree)
}

// Admin only.
export function createCourseUnit(courseUnit) {
  return post('/api/course-units', courseUnit)
}
