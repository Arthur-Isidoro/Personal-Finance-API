import { NavLink } from 'react-router-dom'
import { usePreferences } from '../../context/PreferencesContext'

export default function MobileNav() {
  const { t } = usePreferences()

  const items = [
    { to: '/', label: t('nav.dashboard') },
    { to: '/transactions', label: t('nav.transactions') },
    { to: '/categories', label: t('nav.categories') },
    { to: '/settings', label: t('nav.settings') },
  ]

  return (
    <nav className="mobile-nav">
      {items.map((i) => (
        <NavLink
          key={i.to}
          to={i.to}
          end={i.to === '/'}
          className={({ isActive }) => (isActive ? 'mobile-link active' : 'mobile-link')}
        >
          {i.label}
        </NavLink>
      ))}
    </nav>
  )
}
