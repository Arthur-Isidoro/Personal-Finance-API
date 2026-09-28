import { NavLink } from 'react-router-dom'

const items = [
  { to: '/', label: 'Dashboard', icon: '◧' },
  { to: '/transactions', label: 'Transactions', icon: '↕' },
  { to: '/categories', label: 'Categories', icon: '▤' },
]

export default function Sidebar() {
  return (
    <nav className="sidebar">
      <div className="brand">Personal Finance</div>
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
