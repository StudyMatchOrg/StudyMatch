import { get, post } from './http.js'

export function getStrategies() {
  return get('/api/grouping-strategies')
}

// Generates and saves a new set of groups for a project.
export function generateGroups(projectId, strategy) {
  return post(`/api/grouping-contexts/${projectId}/group-sets`, { strategy })
}

// Earlier generations of a project, newest first.
export function getGroupSets(projectId) {
  return get(`/api/grouping-contexts/${projectId}/group-sets`)
}

export function getGroupSet(id) {
  return get(`/api/group-sets/${id}`)
}
