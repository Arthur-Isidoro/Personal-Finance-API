import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/auth'
import { useToast } from '../context/ToastContext'
import { usePreferences } from '../context/PreferencesContext'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'
import { ApiError } from '../api/client'

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { push } = useToast()
  const { t } = usePreferences()
  const navigate = useNavigate()

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      push(t('auth.accountCreated'), 'success')
      navigate('/login')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('auth.genericError'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-screen">
      <form className="auth-card" onSubmit={submit}>
        <h1>{t('auth.registerTitle')}</h1>
        <p className="auth-subtitle">{t('auth.registerSubtitle')}</p>
        <Input
          label={t('auth.name')}
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          required
        />
        <Input
          label={t('auth.email')}
          type="email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          required
        />
        <Input
          label={t('auth.password')}
          type="password"
          minLength={6}
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
        />
        {error && <p className="form-error">{error}</p>}
        <Button type="submit" disabled={loading}>
          {loading ? t('auth.registerLoading') : t('auth.registerButton')}
        </Button>
        <p className="auth-switch">
          {t('auth.hasAccount')} <Link to="/login">{t('auth.loginLink')}</Link>
        </p>
      </form>
    </div>
  )
}
