import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { useAuth } from '../context/AuthContext'
import { usePreferences } from '../context/PreferencesContext'
import { getDashboard, getCategoryReport } from '../api/dashboard'
import { listTransactions } from '../api/transactions'
import { formatCurrency, formatDate } from '../utils/format'
import Loading from '../components/ui/Loading'
import EmptyState from '../components/ui/EmptyState'
import Badge from '../components/ui/Badge'

export default function Dashboard() {
  const { token } = useAuth()
  const { currency, language, t } = usePreferences()
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
        if (active) setError(t('dashboard.loadError'))
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => {
      active = false
    }
  }, [token, t])

  const greeting = (() => {
    const h = new Date().getHours()
    if (h < 12) return t('dashboard.goodMorning')
    if (h < 18) return t('dashboard.goodAfternoon')
    return t('dashboard.goodEvening')
  })()

  if (loading) return <Loading label={t('dashboard.loading')} />
  if (error) return <EmptyState title={t('common.somethingWrong')} description={error} />

  return (
    <div className="page">
      <div className="page-header">
        <h1>{greeting}</h1>
        <p className="page-subtitle">{t('dashboard.subtitle')}</p>
      </div>

      <div className="summary-grid">
        <div className="summary-card">
          <span className="summary-label">{t('dashboard.balance')}</span>
          <span className={`summary-value ${summary.balance >= 0 ? 'positive' : 'negative'}`}>
            {formatCurrency(summary.balance, currency)}
          </span>
        </div>
        <div className="summary-card">
          <span className="summary-label">{t('dashboard.income')}</span>
          <span className="summary-value positive">{formatCurrency(summary.totalIncome, currency)}</span>
        </div>
        <div className="summary-card">
          <span className="summary-label">{t('dashboard.expenses')}</span>
          <span className="summary-value negative">{formatCurrency(summary.totalExpense, currency)}</span>
        </div>
      </div>

      <div className="dashboard-grid">
        <section className="panel">
          <h2>{t('dashboard.byCategory')}</h2>
          {report.length === 0 ? (
            <EmptyState title={t('dashboard.noExpenses')} description={t('dashboard.addToSeeChart')} />
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={report} layout="vertical" margin={{ left: 24 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="var(--border)" />
                <XAxis
                  type="number"
                  tickFormatter={(v) => formatCurrency(v, currency)}
                  stroke="var(--text-muted)"
                  fontSize={12}
                />
                <YAxis type="category" dataKey="categoryName" stroke="var(--text-muted)" fontSize={12} width={100} />
                <Tooltip
                  formatter={(v) => formatCurrency(v, currency)}
                  contentStyle={{ background: 'var(--surface)', border: '1px solid var(--border)' }}
                />
                <Bar dataKey="totalAmount" fill="var(--accent)" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </section>

        <section className="panel">
          <div className="panel-header-row">
            <h2>{t('dashboard.recent')}</h2>
            <Link to="/transactions" className="link-btn">
              {t('dashboard.viewAll')}
            </Link>
          </div>
          {recent.length === 0 ? (
            <EmptyState title={t('dashboard.noTransactions')} description={t('dashboard.startAdding')} />
          ) : (
            <ul className="tx-list">
              {recent.map((tx) => (
                <li key={tx.id} className="tx-row">
                  <div>
                    <Badge tone={tx.type === 'INCOME' ? 'positive' : 'negative'}>{tx.category.name}</Badge>
                    <p className="tx-desc">{tx.description}</p>
                    <p className="tx-date">{formatDate(tx.date, language)}</p>
                  </div>
                  <span className={tx.type === 'INCOME' ? 'positive' : 'negative'}>
                    {tx.type === 'INCOME' ? '+' : '-'}
                    {formatCurrency(tx.amount, currency)}
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
