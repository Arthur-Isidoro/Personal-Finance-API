import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { useAuth } from '../context/AuthContext'
import { getDashboard, getCategoryReport } from '../api/dashboard'
import { listTransactions } from '../api/transactions'
import { formatCurrency, formatDate } from '../utils/format'
import Loading from '../components/ui/Loading'
import EmptyState from '../components/ui/EmptyState'
import Badge from '../components/ui/Badge'

export default function Dashboard() {
  const { token } = useAuth()
  const [summary, setSummary] = useState(null)
  const [report, setReport] = useState([])
  const [recent, setRecent] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    async function load() {
      setLoading(true)
      setError('')
      try {
        const [s, r, tx] = await Promise.all([
          getDashboard(token),
          getCategoryReport(token),
          listTransactions(token, { page: 0, size: 5 }),
        ])
        if (!active) return
        setSummary(s)
        setReport(r)
        setRecent(tx.content)
      } catch {
        if (active) setError("We couldn't load your dashboard. Please try again.")
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => {
      active = false
    }
  }, [token])

  const greeting = (() => {
    const h = new Date().getHours()
    if (h < 12) return 'Good morning'
    if (h < 18) return 'Good afternoon'
    return 'Good evening'
  })()

  if (loading) return <Loading label="Loading your overview…" />
  if (error) return <EmptyState title="Something went wrong" description={error} />

  return (
    <div className="page">
      <div className="page-header">
        <h1>{greeting}</h1>
        <p className="page-subtitle">Here's your financial overview.</p>
      </div>

      <div className="summary-grid">
        <div className="summary-card">
          <span className="summary-label">Balance</span>
          <span className={`summary-value ${summary.balance >= 0 ? 'positive' : 'negative'}`}>
            {formatCurrency(summary.balance)}
          </span>
        </div>
        <div className="summary-card">
          <span className="summary-label">Income</span>
          <span className="summary-value positive">{formatCurrency(summary.totalIncome)}</span>
        </div>
        <div className="summary-card">
          <span className="summary-label">Expenses</span>
          <span className="summary-value negative">{formatCurrency(summary.totalExpense)}</span>
        </div>
      </div>

      <div className="dashboard-grid">
        <section className="panel">
          <h2>Expenses by category</h2>
          {report.length === 0 ? (
            <EmptyState title="No expenses yet" description="Add a transaction to see this chart." />
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={report} layout="vertical" margin={{ left: 24 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="var(--border)" />
                <XAxis type="number" tickFormatter={(v) => formatCurrency(v)} stroke="var(--text-muted)" fontSize={12} />
                <YAxis type="category" dataKey="categoryName" stroke="var(--text-muted)" fontSize={12} width={100} />
                <Tooltip
                  formatter={(v) => formatCurrency(v)}
                  contentStyle={{ background: 'var(--surface)', border: '1px solid var(--border)' }}
                />
                <Bar dataKey="totalAmount" fill="var(--accent)" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </section>

        <section className="panel">
          <div className="panel-header-row">
            <h2>Recent transactions</h2>
            <Link to="/transactions" className="link-btn">
              View all transactions
            </Link>
          </div>
          {recent.length === 0 ? (
            <EmptyState title="No transactions yet" description="Start by adding your first transaction." />
          ) : (
            <ul className="tx-list">
              {recent.map((t) => (
                <li key={t.id} className="tx-row">
                  <div>
                    <Badge tone={t.type === 'INCOME' ? 'positive' : 'negative'}>{t.category.name}</Badge>
                    <p className="tx-desc">{t.description}</p>
                    <p className="tx-date">{formatDate(t.date)}</p>
                  </div>
                  <span className={t.type === 'INCOME' ? 'positive' : 'negative'}>
                    {t.type === 'INCOME' ? '+' : '-'}
                    {formatCurrency(t.amount)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  )
}
