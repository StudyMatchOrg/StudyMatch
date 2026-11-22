import { get, post, put } from './http.js'

// Projects are called grouping contexts in the backend (UC14).

export function getProjects() {
  return get('/api/grouping-contexts')
}

export function getProject(id) {
  return get(`/api/grouping-contexts/${id}`)
}

export function createProject(project) {
  return post('/api/grouping-contexts', project)
}

export function updateProject(id, project) {
  return put(`/api/grouping-contexts/${id}`, project)
}

// Students enrolled in a course unit in an academic year.
export function getCandidates(courseUnitId, academicYear) {
  const query = new URLSearchParams({ courseUnitId, academicYear })
  return get(`/api/grouping-contexts/candidates?${query}`)
}
