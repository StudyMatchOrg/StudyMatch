import { get } from './http.js'

export function getCompetencies() {
  return get('/api/competencies')
}

export function getCourseUnitCompetencies(courseUnitId) {
  return get(courseUnitId ? `/api/course-unit-competencies?courseUnitId=${courseUnitId}` : '/api/course-unit-competencies')
}
