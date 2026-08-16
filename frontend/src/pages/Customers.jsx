import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'
import Field from '../components/Field'
import Modal from '../components/Modal'
import { Banner, Button, Card, EmptyRow, Td, TableWrap, Th } from '../components/Ui'

const EMPTY_FORM = { name: '', email: '', phone: '', address: '' }

export default function Customers() {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)
  const [busyId, setBusyId] = useState(null)

  const [createOpen, setCreateOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  const load = useCallback(async () => {
    try {
      setCustomers(await api.customers.list())
      setError(null)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const submitCreate = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setFieldErrors({})
    setError(null)
    setNotice(null)
    try {
      await api.customers.create(form)
      setCreateOpen(false)
      setForm(EMPTY_FORM)
      setNotice('Customer added.')
      await load()
    } catch (err) {
      setFieldErrors(err.fieldErrors ?? {})
      setError(err.fieldErrors ? 'Please correct the highlighted fields.' : err.message)
    } finally {
      setSubmitting(false)
    }
  }

  // The backend refuses to delete a customer who has orders; that 409 surfaces in the banner.
  const remove = async (customer) => {
    setBusyId(customer.id)
    setError(null)
    setNotice(null)
    try {
      await api.customers.remove(customer.id)
      setNotice(`${customer.name} deleted.`)
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">Customers</h1>
          <p className="mt-0.5 text-sm text-slate-500">People that delivery orders are placed for.</p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>+ New customer</Button>
      </div>

      <Banner onDismiss={() => setError(null)}>{error}</Banner>
      <Banner tone="success" onDismiss={() => setNotice(null)}>
        {notice}
      </Banner>

      <Card>
        <TableWrap>
          <thead>
            <tr>
              <Th>Name</Th>
              <Th>Email</Th>
              <Th>Phone</Th>
              <Th>Address</Th>
              <Th className="text-right">Actions</Th>
            </tr>
          </thead>
          <tbody>
            {loading && <EmptyRow colSpan={5}>Loading…</EmptyRow>}
            {!loading && customers.length === 0 && <EmptyRow colSpan={5}>No customers yet.</EmptyRow>}
            {customers.map((customer) => (
              <tr key={customer.id} className="hover:bg-slate-50">
                <Td className="font-medium whitespace-nowrap text-slate-900">{customer.name}</Td>
                <Td className="whitespace-nowrap">{customer.email}</Td>
                <Td className="whitespace-nowrap tabular-nums">{customer.phone}</Td>
                <Td>{customer.address}</Td>
                <Td>
                  <div className="flex justify-end">
                    <Button variant="danger" disabled={busyId === customer.id} onClick={() => remove(customer)}>
                      Delete
                    </Button>
                  </div>
                </Td>
              </tr>
            ))}
          </tbody>
        </TableWrap>
      </Card>

      <Modal open={createOpen} title="New customer" onClose={() => setCreateOpen(false)}>
        <form onSubmit={submitCreate} className="space-y-4">
          <Field
            label="Name"
            name="name"
            required
            placeholder="Rahul Sharma"
            value={form.name}
            error={fieldErrors.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
          <Field
            label="Email"
            name="email"
            required
            type="email"
            placeholder="rahul@example.com"
            value={form.email}
            error={fieldErrors.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
          <Field
            label="Phone (10 digits)"
            name="phone"
            required
            inputMode="numeric"
            placeholder="9876543210"
            value={form.phone}
            error={fieldErrors.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
          />
          <Field
            label="Address"
            name="address"
            required
            placeholder="Andheri West, Mumbai"
            value={form.address}
            error={fieldErrors.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
          />
          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="secondary" onClick={() => setCreateOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Saving…' : 'Add customer'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
