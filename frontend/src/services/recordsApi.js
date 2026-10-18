import { del, get, post, put } from './http.js'

export function getRecords(studentId) {
  return get(`/api/students/${studentId}/academic-records`)
}

export function addRecord(studentId, record) {
  return post(`/api/students/${studentId}/academic-records`, record)
}

export function changeRecord(recordId, change) {
  return put(`/api/academic-records/${recordId}`, change)
}

export function deleteRecord(recordId) {
  return del(`/api/academic-records/${recordId}`)
}
