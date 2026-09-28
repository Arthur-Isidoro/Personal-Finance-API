import { request } from './client'

export const register = (payload) =>
  request('/api/auth/register', { method: 'POST', body: payload })

export const login = (payload) =>
  request('/api/auth/login', { method: 'POST', body: payload })
