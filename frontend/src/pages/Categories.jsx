import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { listCategories, createCategory, updateCategory, deleteCategory } from '../api/categories'
import Loading from '../components/ui/Loading'
import EmptyState from '../components/ui/EmptyState'
import Badge from '../components/ui/Badge'
import Button from '../components/ui/Button'
import Input from '../components/ui/Input'
import Select from '../components/ui/Select'
import Modal from '../components/ui/Modal'
import { ApiError } from '../api/client'

const emptyForm = { name: '', type: 'EXPENSE' }

export default function Categories() {
  const { token } = useAuth()
  const { push } = useToast()
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [confirmId, setConfirmId] = useState(null)

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      setCategories(await listCategories(token))
    } catch {
      setError("We couldn't load your categories. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (c) => {
    setEditing(c)
    setForm({ name: c.name, type: c.type || 'EXPENSE' })
    setFormError('')
    setModalOpen(true)
  }

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateCategory(token, editing.id, form)
        push('Category updated.', 'success')
      } else {
        await createCategory(token, form)
        push('Category created.', 'success')
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
      await deleteCategory(token, confirmId)
      push('Category deleted.', 'success')
      setConfirmId(null)
      load()
    } catch (err) {
      push(err instanceof ApiError ? err.message : 'Could not delete.', 'error')
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Categories</h1>
        <p className="page-subtitle">Organize your income and expenses.</p>
      </div>

      <div className="toolbar">
        <div />
        <Button onClick={openCreate}>+ Add category</Button>
      </div>

      {loading ? (
        <Loading label="Loading categories…" />
      ) : error ? (
        <EmptyState title="Something went wrong" description={error} />
      ) : categories.length === 0 ? (
        <EmptyState
          title="No categories yet"
          description="Create one to start organizing your transactions."
          action={<Button onClick={openCreate}>+ Add category</Button>}
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Category</th>
                <th>Type</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id}>
                  <td>{c.name}</td>
                  <td>
                    <Badge tone={c.type === 'INCOME' ? 'positive' : 'negative'}>{c.type}</Badge>
                  </td>
                  <td className="row-actions">
                    <button className="link-btn" onClick={() => openEdit(c)}>
                      Edit
                    </button>
                    <button
                      className="link-btn"
                      style={{ color: 'var(--negative)' }}
                      onClick={() => setConfirmId(c.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit category' : 'Add category'}>
        <form onSubmit={submit}>
          <Input label="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <Select label="Type" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </Select>
          {formError && <p className="form-error">{formError}</p>}
          <Button type="submit" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </Button>
        </form>
      </Modal>

      <Modal open={!!confirmId} onClose={() => setConfirmId(null)} title="Delete category">
        <p>Are you sure you want to delete this category?</p>
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
