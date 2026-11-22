import { del, get, post, put } from './http.js'

export function getCompetencies() {
  return get('/api/competencies')
}

export function getCourseUnitCompetencies(courseUnitId) {
  return get(courseUnitId ? `/api/course-unit-competencies?courseUnitId=${courseUnitId}` : '/api/course-unit-competencies')
}

export function createCompetency(competency) {
  return post('/api/competencies', competency)
}

// Only the name and description can change; the code is fixed.
export function updateCompetency(id, change) {
  return put(`/api/competencies/${id}`, change)
}

export function linkCourseUnit(courseUnitId, competencyId, weight) {
  return post('/api/course-unit-competencies', { courseUnitId, competencyId, weight })
}

export function changeWeight(linkId, weight) {
  return put(`/api/course-unit-competencies/${linkId}`, { weight })
}

export function unlinkCourseUnit(linkId) {
  return del(`/api/course-unit-competencies/${linkId}`)
}
