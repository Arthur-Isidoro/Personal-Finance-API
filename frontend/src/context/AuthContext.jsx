import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { setUnauthorizedHandler } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('pf_token'))

  const logout = useCallback(() => {
    setToken(null)
    localStorage.removeItem('pf_token')
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
  }, [logout])

  const loginWithToken = (t) => {
    setToken(t)
    localStorage.setItem('pf_token', t)
  }

  return (
    <AuthContext.Provider value={{ token, isAuthenticated: !!token, loginWithToken, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
