import { useAuth } from '../../context/AuthContext'
import { useTheme } from '../../context/ThemeContext'
import { usePreferences } from '../../context/PreferencesContext'

export default function Topbar() {
  const { logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { t } = usePreferences()
  return (
    <header className="topbar">
      <div />
      <div className="topbar-actions">
        <button className="icon-btn" onClick={toggleTheme} aria-label="Toggle theme">
          {theme === 'light' ? '☾' : '☀'}
        </button>
        <button className="link-btn" onClick={logout}>
          {t('nav.logout')}
        </button>
      </div>
    </header>
  )
}
