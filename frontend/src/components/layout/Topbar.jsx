import { useAuth } from '../../context/AuthContext'
import { useTheme } from '../../context/ThemeContext'

export default function Topbar() {
  const { logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  return (
    <header className="topbar">
      <div />
      <div className="topbar-actions">
        <button className="icon-btn" onClick={toggleTheme} aria-label="Toggle theme">
          {theme === 'light' ? '☾' : '☀'}
        </button>
        <button className="link-btn" onClick={logout}>
          Log out
        </button>
      </div>
    </header>
  )
}
