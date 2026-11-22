import { get, post } from './http.js'

export function getAccounts() {
  return get('/api/accounts')
}

export function createAccount(account) {
  return post('/api/accounts', account)
}
