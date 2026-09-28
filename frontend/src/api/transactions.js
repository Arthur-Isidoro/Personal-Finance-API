import { request } from './client'

export const listTransactions = (token, { page = 0, size = 10 } = {}) =>
  request('/api/transactions', { token, params: { page, size } })

export const filterTransactions = (token, filters, { page = 0, size = 10 } = {}) =>
  request('/api/transactions/filter', { token, params: { ...filters, page, size } })

export const getTransaction = (token, id) => request(`/api/transactions/${id}`, { token })

export const createTransaction = (token, payload) =>
  request('/api/transactions', { method: 'POST', body: payload, token })

export const updateTransaction = (token, id, payload) =>
  request(`/api/transactions/${id}`, { method: 'PUT', body: payload, token })

export const deleteTransaction = (token, id) =>
  request(`/api/transactions/${id}`, { method: 'DELETE', token })
