import { request } from './client'

export const listCategories = (token) => request('/api/categories', { token })

export const createCategory = (token, payload) =>
  request('/api/categories', { method: 'POST', body: payload, token })

export const updateCategory = (token, id, payload) =>
  request(`/api/categories/${id}`, { method: 'PUT', body: payload, token })

export const deleteCategory = (token, id) =>
  request(`/api/categories/${id}`, { method: 'DELETE', token })
