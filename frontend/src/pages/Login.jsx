import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { usePreferences } from '../context/PreferencesContext'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'
import { ApiError } from '../api/client'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { loginWithToken } = useAuth()
  const { push } = useToast()
  const { t } = usePreferences()
  const navigate = useNavigate()

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await login({ email, password })
      loginWithToken(data.token)
      push(t('auth.welcomeBack'), 'success')
      navigate('/')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('auth.genericError'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-screen">
      <form className="auth-card" onSubmit={submit}>
        <h1>{t('auth.loginTitle')}</h1>
        <p className="auth-subtitle">{t('auth.loginSubtitle')}</p>
        <Input
          label={t('auth.email')}
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <Input
          label={t('auth.password')}
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        {error && <p className="form-error">{error}</p>}
        <Button type="submit" disabled={loading}>
          {loading ? t('auth.loginLoading') : t('auth.loginButton')}
        </Button>
        <p className="auth-switch">
          {t('auth.noAccount')} <Link to="/register">{t('auth.createOne')}</Link>
        </p>
      </form>
    </div>
  )
}
