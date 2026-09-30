import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { useAuth } from './AuthContext'
import { getPreferences, updatePreferences } from '../api/users'
import translations from '../i18n/translations'

const PreferencesContext = createContext(null)

const DEFAULT_LANGUAGE = localStorage.getItem('pf_language') || 'pt-BR'
const DEFAULT_CURRENCY = 'BRL'

function getByPath(obj, path) {
  return path.split('.').reduce((acc, key) => (acc && acc[key] !== undefined ? acc[key] : undefined), obj)
}

export function PreferencesProvider({ children }) {
  const { token, isAuthenticated } = useAuth()
  const [language, setLanguageState] = useState(DEFAULT_LANGUAGE)
  const [currency, setCurrencyState] = useState(DEFAULT_CURRENCY)

  useEffect(() => {
    let active = true
    if (!isAuthenticated) return
    getPreferences(token)
      .then((prefs) => {
        if (!active) return
        setLanguageState(prefs.language)
        setCurrencyState(prefs.currency)
        localStorage.setItem('pf_language', prefs.language)
      })
      .catch(() => {
        // keep whatever was already set locally if the request fails
      })
    return () => {
      active = false
    }
  }, [isAuthenticated, token])

  const save = useCallback(
    async (nextLanguage, nextCurrency) => {
      const previousLanguage = language
      const previousCurrency = currency
      setLanguageState(nextLanguage)
      setCurrencyState(nextCurrency)
      localStorage.setItem('pf_language', nextLanguage)
      try {
        await updatePreferences(token, { language: nextLanguage, currency: nextCurrency })
        return true
      } catch (err) {
        setLanguageState(previousLanguage)
        setCurrencyState(previousCurrency)
        localStorage.setItem('pf_language', previousLanguage)
        throw err
      }
    },
    [token, language, currency]
  )

  const setLanguage = (value) => save(value, currency)
  const setCurrency = (value) => save(language, value)

  const t = useCallback(
    (key, vars) => {
      const dict = translations[language] || translations['pt-BR']
      let text = getByPath(dict, key) ?? getByPath(translations['pt-BR'], key) ?? key
      if (vars) {
        Object.entries(vars).forEach(([k, v]) => {
          text = text.replace(`{${k}}`, v)
        })
      }
      return text
    },
    [language]
  )

  return (
    <PreferencesContext.Provider value={{ language, currency, setLanguage, setCurrency, t }}>
      {children}
    </PreferencesContext.Provider>
  )
}

export const usePreferences = () => useContext(PreferencesContext)
