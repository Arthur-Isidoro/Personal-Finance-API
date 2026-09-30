import { NavLink } from 'react-router-dom'
import { usePreferences } from '../../context/PreferencesContext'

export default function Sidebar() {
  const { t } = usePreferences()

  const items = [
    { to: '/', label: t('nav.dashboard'), icon: '◧' },
    { to: '/transactions', label: t('nav.transactions'), icon: '↕' },
    { to: '/categories', label: t('nav.categories'), icon: '▤' },
    { to: '/settings', label: t('nav.settings'), icon: '⚙' },
  ]

  return (
    <nav className="sidebar">
      <div className="brand">{t('auth.appName')}</div>
      <ul>
        {items.map((i) => (
          <li key={i.to}>
            <NavLink
              to={i.to}
              end={i.to === '/'}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              <span className="nav-icon">{i.icon}</span>
              {i.label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
