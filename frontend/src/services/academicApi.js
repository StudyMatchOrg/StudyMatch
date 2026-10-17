import { get } from './http.js'

export function getDegrees() {
  return get('/api/degrees')
}

export function getCourseUnits(degreeId) {
  return get(degreeId ? `/api/course-units?degreeId=${degreeId}` : '/api/course-units')
}
