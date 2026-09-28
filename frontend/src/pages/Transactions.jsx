import { useEffect, useState, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
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
      setError("We couldn't load your transactions. Please try again.")
    } finally {
      setLoading(false)
    }
  }, [token, pageIndex, filters, filtersActive])

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

  const openEdit = (t) => {
    setEditing(t)
    setForm({
      description: t.description,
      amount: t.amount,
      type: t.type,
      categoryId: t.category.id,
      date: t.date,
      paymentMethod: t.paymentMethod || '',
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
        push('Transaction updated.', 'success')
      } else {
        await createTransaction(token, payload)
        push('Transaction added.', 'success')
      }
      setModalOpen(false)
      load()
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Something went wrong.')
    } finally {
      setSaving(false)
    }
  }

  const confirmDelete = async () => {
    try {
      await deleteTransaction(token, confirmId)
      push('Transaction deleted.', 'success')
      setConfirmId(null)
      load()
    } catch (err) {
      push(err instanceof ApiError ? err.message : 'Could not delete.', 'error')
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
        <h1>Transactions</h1>
        <p className="page-subtitle">All your income and expenses.</p>
      </div>

      <div className="toolbar">
        <div className="filters">
          <Input
            label="From"
            type="date"
            value={filters.startDate}
            onChange={(e) => setFilter({ startDate: e.target.value })}
          />
          <Input
            label="To"
            type="date"
            value={filters.endDate}
            onChange={(e) => setFilter({ endDate: e.target.value })}
          />
          <Select label="Type" value={filters.type} onChange={(e) => setFilter({ type: e.target.value })}>
            <option value="">All</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </Select>
          <Select
            label="Category"
            value={filters.categoryId}
            onChange={(e) => setFilter({ categoryId: e.target.value })}
          >
            <option value="">All</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          {filtersActive && (
            <Button variant="secondary" type="button" onClick={clearFilters}>
              Clear filters
            </Button>
          )}
        </div>
        <Button onClick={openCreate}>+ Add transaction</Button>
      </div>

      {loading ? (
        <Loading label="Loading transactions…" />
      ) : error ? (
        <EmptyState title="Something went wrong" description={error} />
      ) : page.content.length === 0 ? (
        <EmptyState
          title={filtersActive ? 'No results' : 'No transactions yet'}
          description={filtersActive ? 'Try adjusting your filters.' : 'Start by adding your first transaction.'}
          action={!filtersActive && <Button onClick={openCreate}>+ Add transaction</Button>}
        />
      ) : (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Description</th>
                  <th>Category</th>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((t) => (
                  <tr key={t.id}>
                    <td>{t.description}</td>
                    <td>{t.category.name}</td>
                    <td>{formatDate(t.date)}</td>
                    <td>
                      <Badge tone={t.type === 'INCOME' ? 'positive' : 'negative'}>{t.type}</Badge>
                    </td>
                    <td className={t.type === 'INCOME' ? 'positive' : 'negative'}>
                      {t.type === 'INCOME' ? '+' : '-'}
                      {formatCurrency(t.amount)}
                    </td>
                    <td className="row-actions">
                      <button className="link-btn" onClick={() => openEdit(t)}>
                        Edit
                      </button>
                      <button
                        className="link-btn"
                        style={{ color: 'var(--negative)' }}
                        onClick={() => setConfirmId(t.id)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card-list">
            {page.content.map((t) => (
              <div key={t.id} className="tx-card">
                <div className="tx-row">
                  <div>
                    <Badge tone={t.type === 'INCOME' ? 'positive' : 'negative'}>{t.category.name}</Badge>
                    <p className="tx-desc">{t.description}</p>
                    <p className="tx-date">{formatDate(t.date)}</p>
                  </div>
                  <span className={t.type === 'INCOME' ? 'positive' : 'negative'}>
                    {t.type === 'INCOME' ? '+' : '-'}
                    {formatCurrency(t.amount)}
                  </span>
                </div>
                <div className="row-actions">
                  <button className="link-btn" onClick={() => openEdit(t)}>
                    Edit
                  </button>
                  <button className="link-btn" style={{ color: 'var(--negative)' }} onClick={() => setConfirmId(t.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="pagination">
            <button className="link-btn" disabled={page.first} onClick={() => setPageIndex((p) => p - 1)}>
              Previous
            </button>
            <span>
              Page {page.number + 1} of {Math.max(page.totalPages, 1)}
            </span>
            <button className="link-btn" disabled={page.last} onClick={() => setPageIndex((p) => p + 1)}>
              Next
            </button>
          </div>
        </>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit transaction' : 'Add transaction'}>
        <form onSubmit={submit}>
          <Input
            label="Description"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            required
          />
          <Input
            label="Amount"
            type="number"
            step="0.01"
            min="0.01"
            value={form.amount}
            onChange={(e) => setForm({ ...form, amount: e.target.value })}
            required
          />
          <Select label="Type" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </Select>
          <Select
            label="Category"
            value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
            required
          >
            <option value="" disabled>
              Select a category
            </option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          <Input
            label="Date"
            type="date"
            value={form.date}
            onChange={(e) => setForm({ ...form, date: e.target.value })}
            required
          />
          <Input
            label="Payment method (optional)"
            value={form.paymentMethod}
            onChange={(e) => setForm({ ...form, paymentMethod: e.target.value })}
          />
          {formError && <p className="form-error">{formError}</p>}
          <Button type="submit" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </Button>
        </form>
      </Modal>

      <Modal open={!!confirmId} onClose={() => setConfirmId(null)} title="Delete transaction">
        <p>Are you sure you want to delete this transaction?</p>
        <div className="row-actions" style={{ marginTop: 16, justifyContent: 'flex-end' }}>
          <Button variant="secondary" onClick={() => setConfirmId(null)}>
            Cancel
          </Button>
          <Button variant="danger" onClick={confirmDelete}>
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  )
}
