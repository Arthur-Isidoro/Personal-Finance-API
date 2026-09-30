import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { usePreferences } from '../context/PreferencesContext'
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
  const { t } = usePreferences()
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
      setError(t('categories.loadError'))
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
        push(t('categories.updated'), 'success')
      } else {
        await createCategory(token, form)
        push(t('categories.created'), 'success')
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
      await deleteCategory(token, confirmId)
      push(t('categories.deleted'), 'success')
      setConfirmId(null)
      load()
    } catch (err) {
      push(err instanceof ApiError ? err.message : t('categories.deleteError'), 'error')
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t('categories.title')}</h1>
        <p className="page-subtitle">{t('categories.subtitle')}</p>
      </div>

      <div className="toolbar">
        <div />
        <Button onClick={openCreate}>{t('categories.add')}</Button>
      </div>

      {loading ? (
        <Loading label={t('categories.loading')} />
      ) : error ? (
        <EmptyState title={t('common.somethingWrong')} description={error} />
      ) : categories.length === 0 ? (
        <EmptyState
          title={t('categories.noneYet')}
          description={t('categories.createOne')}
          action={<Button onClick={openCreate}>{t('categories.add')}</Button>}
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>{t('categories.name')}</th>
                <th>{t('categories.type')}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id}>
                  <td>{c.name}</td>
                  <td>
                    <Badge tone={c.type === 'INCOME' ? 'positive' : 'negative'}>
                      {c.type === 'INCOME' ? t('categories.income') : t('categories.expense')}
                    </Badge>
                  </td>
                  <td className="row-actions">
                    <button className="link-btn" onClick={() => openEdit(c)}>
                      {t('categories.edit')}
                    </button>
                    <button
                      className="link-btn"
                      style={{ color: 'var(--negative)' }}
                      onClick={() => setConfirmId(c.id)}
                    >
                      {t('categories.delete')}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? t('categories.editTitle') : t('categories.addTitle')}>
        <form onSubmit={submit}>
          <Input label={t('categories.name')} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <Select label={t('categories.type')} value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="EXPENSE">{t('categories.expense')}</option>
            <option value="INCOME">{t('categories.income')}</option>
          </Select>
          {formError && <p className="form-error">{formError}</p>}
          <Button type="submit" disabled={saving}>
            {saving ? t('categories.saving') : t('categories.save')}
          </Button>
        </form>
      </Modal>

      <Modal open={!!confirmId} onClose={() => setConfirmId(null)} title={t('categories.deleteTitle')}>
        <p>{t('categories.deleteConfirm')}</p>
        <div className="row-actions" style={{ marginTop: 16, justifyContent: 'flex-end' }}>
          <Button variant="secondary" onClick={() => setConfirmId(null)}>
            {t('categories.cancel')}
          </Button>
          <Button variant="danger" onClick={confirmDelete}>
            {t('categories.delete')}
          </Button>
        </div>
      </Modal>
    </div>
  )
}
