import { get } from './http.js'

export function getHealth() {
  return get('/api/health')
}
