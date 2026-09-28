import { request } from './client'

export const getDashboard = (token, params) => request('/api/dashboard', { token, params })

export const getCategoryReport = (token, params) =>
  request('/api/reports/categories', { token, params })
