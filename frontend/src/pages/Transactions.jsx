import { useEffect, useState, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { usePreferences } from '../context/PreferencesContext'
import {
  listTransactions,
  filterTransactions,
  createTransaction,
  updateTransaction,
  deleteTransaction,
} from '../api/transactions'
import { listCategories } from '../api/categories'
import { formatCurrency, formatDate } from '../utils/format'
import Loading from '../components/ui/Loading'
import EmptyState from '../components/ui/EmptyState'
import Badge from '../components/ui/Badge'
import Button from '../components/ui/Button'
import Input from '../components/ui/Input'
import Select from '../components/ui/Select'
import Modal from '../components/ui/Modal'
import { ApiError } from '../api/client'

const emptyForm = { description: '', amount: '', type: 'EXPENSE', categoryId: '', date: '', paymentMethod: '' }

export default function Transactions() {
  const { token } = useAuth()
  const { push } = useToast()
  const { currency, language, t } = usePreferences()

  const [categories, setCategories] = useState([])
  const [page, setPage] = useState({ content: [], totalPages: 0, number: 0, first: true, last: true })
  const [pageIndex, setPageIndex] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [filters, setFilters] = useState({ startDate: '', endDate: '', type: '', categoryId: '' })
  const filtersActive = Object.values(filters).some(Boolean)

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [confirmId, setConfirmId] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = filtersActive
        ? await filterTransactions(token, filters, { page: pageIndex, size: 10 })
        : await listTransactions(token, { page: pageIndex, size: 10 })
      setPage(data)
    } catch {
      setError(t('transactions.loadError'))
    } finally {
      setLoading(false)
    }
  }, [token, pageIndex, filters, filtersActive, t])

  useEffect(() => {
    listCategories(token).then(setCategories).catch(() => {})
  }, [token])

  useEffect(() => {
    load()
  }, [load])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (tx) => {
    setEditing(tx)
    setForm({
      description: tx.description,
      amount: tx.amount,
      type: tx.type,
      categoryId: tx.category.id,
      date: tx.date,
      paymentMethod: tx.paymentMethod || '',
    })
    setFormError('')
    setModalOpen(true)
  }

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    const payload = { ...form, amount: parseFloat(form.amount), categoryId: parseInt(form.categoryId) }
    try {
      if (editing) {
        await updateTransaction(token, editing.id, payload)
        push(t('transactions.updated'), 'success')
      } else {
        await createTransaction(token, payload)
        push(t('transactions.created'), 'success')
      }
      setModalOpen(false)
      load()
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : t('auth.genericError'))
    } finally {
      setSaving(false)
    }
  }

  const confirmDelete = async () => {
    try {
      await deleteTransaction(token, confirmId)
      push(t('transactions.deleted'), 'success')
      setConfirmId(null)
      load()
    } catch (err) {
      push(err instanceof ApiError ? err.message : t('transactions.deleteError'), 'error')
    }
  }

  const setFilter = (patch) => {
    setFilters((f) => ({ ...f, ...patch }))
    setPageIndex(0)
  }

  const clearFilters = () => {
    setFilters({ startDate: '', endDate: '', type: '', categoryId: '' })
    setPageIndex(0)
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t('transactions.title')}</h1>
        <p className="page-subtitle">{t('transactions.subtitle')}</p>
      </div>

      <div className="toolbar">
        <div className="filters">
          <Input
            label={t('transactions.from')}
            type="date"
            value={filters.startDate}
            onChange={(e) => setFilter({ startDate: e.target.value })}
          />
          <Input
            label={t('transactions.to')}
            type="date"
            value={filters.endDate}
            onChange={(e) => setFilter({ endDate: e.target.value })}
          />
          <Select label={t('transactions.type')} value={filters.type} onChange={(e) => setFilter({ type: e.target.value })}>
            <option value="">{t('transactions.all')}</option>
            <option value="INCOME">{t('transactions.income')}</option>
            <option value="EXPENSE">{t('transactions.expense')}</option>
          </Select>
          <Select
            label={t('transactions.category')}
            value={filters.categoryId}
            onChange={(e) => setFilter({ categoryId: e.target.value })}
          >
            <option value="">{t('transactions.all')}</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          {filtersActive && (
            <Button variant="secondary" type="button" onClick={clearFilters}>
              {t('transactions.clearFilters')}
            </Button>
          )}
        </div>
        <Button onClick={openCreate}>{t('transactions.add')}</Button>
      </div>

      {loading ? (
        <Loading label={t('transactions.loading')} />
      ) : error ? (
        <EmptyState title={t('common.somethingWrong')} description={error} />
      ) : page.content.length === 0 ? (
        <EmptyState
          title={filtersActive ? t('transactions.noResults') : t('transactions.noneYet')}
          description={filtersActive ? t('transactions.adjustFilters') : t('transactions.startFirst')}
          action={!filtersActive && <Button onClick={openCreate}>{t('transactions.add')}</Button>}
        />
      ) : (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>{t('transactions.description')}</th>
                  <th>{t('transactions.category')}</th>
                  <th>{t('transactions.date')}</th>
                  <th>{t('transactions.type')}</th>
                  <th>{t('transactions.amount')}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((tx) => (
                  <tr key={tx.id}>
                    <td>{tx.description}</td>
                    <td>{tx.category.name}</td>
                    <td>{formatDate(tx.date, language)}</td>
                    <td>
                      <Badge tone={tx.type === 'INCOME' ? 'positive' : 'negative'}>
                        {tx.type === 'INCOME' ? t('transactions.income') : t('transactions.expense')}
                      </Badge>
                    </td>
                    <td className={tx.type === 'INCOME' ? 'positive' : 'negative'}>
                      {tx.type === 'INCOME' ? '+' : '-'}
                      {formatCurrency(tx.amount, currency)}
                    </td>
                    <td className="row-actions">
                      <button className="link-btn" onClick={() => openEdit(tx)}>
                        {t('transactions.edit')}
                      </button>
                      <button
                        className="link-btn"
                        style={{ color: 'var(--negative)' }}
                        onClick={() => setConfirmId(tx.id)}
                      >
                        {t('transactions.delete')}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card-list">
            {page.content.map((tx) => (
              <div key={tx.id} className="tx-card">
                <div className="tx-row">
                  <div>
                    <Badge tone={tx.type === 'INCOME' ? 'positive' : 'negative'}>{tx.category.name}</Badge>
                    <p className="tx-desc">{tx.description}</p>
                    <p className="tx-date">{formatDate(tx.date, language)}</p>
                  </div>
                  <span className={tx.type === 'INCOME' ? 'positive' : 'negative'}>
                    {tx.type === 'INCOME' ? '+' : '-'}
                    {formatCurrency(tx.amount, currency)}
                  </span>
                </div>
                <div className="row-actions">
                  <button className="link-btn" onClick={() => openEdit(tx)}>
                    {t('transactions.edit')}
                  </button>
                  <button className="link-btn" style={{ color: 'var(--negative)' }} onClick={() => setConfirmId(tx.id)}>
                    {t('transactions.delete')}
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="pagination">
            <button className="link-btn" disabled={page.first} onClick={() => setPageIndex((p) => p - 1)}>
              {t('transactions.previous')}
            </button>
            <span>{t('transactions.pageOf', { current: page.number + 1, total: Math.max(page.totalPages, 1) })}</span>
            <button className="link-btn" disabled={page.last} onClick={() => setPageIndex((p) => p + 1)}>
              {t('transactions.next')}
            </button>
          </div>
        </>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? t('transactions.editTitle') : t('transactions.addTitle')}
      >
        <form onSubmit={submit}>
          <Input
            label={t('transactions.description')}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            required
          />
          <Input
            label={t('transactions.amount')}
            type="number"
            step="0.01"
            min="0.01"
            value={form.amount}
            onChange={(e) => setForm({ ...form, amount: e.target.value })}
            required
          />
          <Select label={t('transactions.type')} value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="EXPENSE">{t('transactions.expense')}</option>
            <option value="INCOME">{t('transactions.income')}</option>
          </Select>
          <Select
            label={t('transactions.category')}
            value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
            required
          >
            <option value="" disabled>
              {t('transactions.selectCategory')}
            </option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          <Input
            label={t('transactions.date')}
            type="date"
            value={form.date}
            onChange={(e) => setForm({ ...form, date: e.target.value })}
            required
          />
          <Input
            label={t('transactions.paymentMethod')}
            value={form.paymentMethod}
            onChange={(e) => setForm({ ...form, paymentMethod: e.target.value })}
          />
          {formError && <p className="form-error">{formError}</p>}
          <Button type="submit" disabled={saving}>
            {saving ? t('transactions.saving') : t('transactions.save')}
          </Button>
        </form>
      </Modal>

      <Modal open={!!confirmId} onClose={() => setConfirmId(null)} title={t('transactions.deleteTitle')}>
        <p>{t('transactions.deleteConfirm')}</p>
        <div className="row-actions" style={{ marginTop: 16, justifyContent: 'flex-end' }}>
          <Button variant="secondary" onClick={() => setConfirmId(null)}>
            {t('transactions.cancel')}
          </Button>
          <Button variant="danger" onClick={confirmDelete}>
            {t('transactions.delete')}
          </Button>
        </div>
      </Modal>
    </div>
  )
}
