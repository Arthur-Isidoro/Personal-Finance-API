import { NavLink } from 'react-router-dom'

const items = [
  { to: '/', label: 'Dashboard' },
  { to: '/transactions', label: 'Transactions' },
  { to: '/categories', label: 'Categories' },
]

export default function MobileNav() {
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
