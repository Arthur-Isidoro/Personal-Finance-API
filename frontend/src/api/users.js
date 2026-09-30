import { request } from './client'

export const getPreferences = (token) => request('/api/users/me/preferences', { token })

export const updatePreferences = (token, payload) =>
  request('/api/users/me/preferences', { method: 'PUT', body: payload, token })
