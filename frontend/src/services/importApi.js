import { upload } from './http.js'

export function importAcademicRecords(file) {
  const formData = new FormData()
  formData.append('file', file)
  return upload('/api/imports/academic-records', formData)
}
