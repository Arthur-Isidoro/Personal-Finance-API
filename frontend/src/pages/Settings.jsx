import { useState } from 'react'
import { usePreferences } from '../context/PreferencesContext'
import { useToast } from '../context/ToastContext'
import Select from '../components/ui/Select'

const LANGUAGES = [
  { value: 'pt-BR', label: 'Português (Brasil)' },
  { value: 'en-US', label: 'English' },
]

const CURRENCIES = [
  { value: 'BRL', label: 'R$ BRL' },
  { value: 'USD', label: '$ USD' },
  { value: 'EUR', label: '€ EUR' },
]

export default function Settings() {
  const { language, currency, setLanguage, setCurrency, t } = usePreferences()
  const { push } = useToast()
  const [savingLanguage, setSavingLanguage] = useState(false)
  const [savingCurrency, setSavingCurrency] = useState(false)

  const onLanguageChange = async (e) => {
    setSavingLanguage(true)
    try {
      await setLanguage(e.target.value)
      push(t('settings.saved'), 'success')
    } catch {
      push(t('settings.saveError'), 'error')
    } finally {
      setSavingLanguage(false)
    }
  }

  const onCurrencyChange = async (e) => {
    setSavingCurrency(true)
    try {
      await setCurrency(e.target.value)
      push(t('settings.saved'), 'success')
    } catch {
      push(t('settings.saveError'), 'error')
    } finally {
      setSavingCurrency(false)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t('settings.title')}</h1>
        <p className="page-subtitle">{t('settings.subtitle')}</p>
      </div>

      <div className="panel" style={{ maxWidth: 420 }}>
        <h2>{t('settings.preferences')}</h2>

        <Select
          label={t('settings.language')}
          value={language}
          onChange={onLanguageChange}
          disabled={savingLanguage}
        >
          {LANGUAGES.map((l) => (
            <option key={l.value} value={l.value}>
              {l.label}
            </option>
          ))}
        </Select>

        <Select
          label={t('settings.currency')}
          value={currency}
          onChange={onCurrencyChange}
          disabled={savingCurrency}
        >
          {CURRENCIES.map((c) => (
            <option key={c.value} value={c.value}>
              {c.label}
            </option>
          ))}
        </Select>
      </div>
    </div>
  )
}
