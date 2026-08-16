import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'
import Field from '../components/Field'
import Modal from '../components/Modal'
import StatusBadge from '../components/StatusBadge'
import { Banner, Button, Card, EmptyRow, Td, TableWrap, Th } from '../components/Ui'

const EMPTY_FORM = { customerId: '', pickupAddress: '', deliveryAddress: '' }

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [customers, setCustomers] = useState([])
  const [availableDrivers, setAvailableDrivers] = useState([])
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
      const [nextOrders, nextCustomers, nextDrivers] = await Promise.all([
        api.orders.list(),
        api.customers.list(),
        api.drivers.available(),
      ])
      setOrders(nextOrders)
      setCustomers(nextCustomers)
      setAvailableDrivers(nextDrivers)
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

  /**
   * Every mutation refreshes the whole page state rather than patching the row locally.
   * Assigning or delivering an order also changes driver availability, so a local patch
   * would leave the "available drivers" list stale.
   */
  const run = async (id, action, successMessage) => {
    setBusyId(id)
    setError(null)
    setNotice(null)
    try {
      await action()
      setNotice(successMessage)
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusyId(null)
    }
  }

  const submitCreate = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setFieldErrors({})
    setError(null)
    setNotice(null)
    try {
      await api.orders.create({
        customerId: Number(form.customerId),
        pickupAddress: form.pickupAddress,
        deliveryAddress: form.deliveryAddress,
      })
      setCreateOpen(false)
      setForm(EMPTY_FORM)
      setNotice('Order created.')
      await load()
    } catch (err) {
      setFieldErrors(err.fieldErrors ?? {})
      setError(err.fieldErrors ? 'Please correct the highlighted fields.' : err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">Orders</h1>
          <p className="mt-0.5 text-sm text-slate-500">
            Create orders, assign an available driver, then advance them through delivery.
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)} disabled={customers.length === 0}>
          + New order
        </Button>
      </div>

      {customers.length === 0 && !loading && (
        <Banner tone="error">Add a customer first — an order must belong to one.</Banner>
      )}
      <Banner onDismiss={() => setError(null)}>{error}</Banner>
      <Banner tone="success" onDismiss={() => setNotice(null)}>
        {notice}
      </Banner>

      <Card>
        <TableWrap>
          <thead>
            <tr>
              <Th>Order</Th>
              <Th>Customer</Th>
              <Th>Route</Th>
              <Th>Driver</Th>
              <Th>Status</Th>
              <Th className="text-right">Actions</Th>
            </tr>
          </thead>
          <tbody>
            {loading && <EmptyRow colSpan={6}>Loading…</EmptyRow>}
            {!loading && orders.length === 0 && <EmptyRow colSpan={6}>No orders yet.</EmptyRow>}
            {orders.map((order) => (
              <tr key={order.id} className="align-top hover:bg-slate-50">
                <Td className="font-medium whitespace-nowrap text-slate-900">{order.orderNumber}</Td>
                <Td className="whitespace-nowrap">{order.customerName}</Td>
                <Td className="text-xs leading-relaxed text-slate-500">
                  <div>{order.pickupAddress}</div>
                  <div className="text-slate-400">↓</div>
                  <div>{order.deliveryAddress}</div>
                </Td>
                <Td className="whitespace-nowrap">
                  {order.driverName ?? <span className="text-slate-400">Unassigned</span>}
                </Td>
                <Td>
                  <StatusBadge status={order.status} />
                </Td>
                <Td>
                  <RowActions
                    order={order}
                    availableDrivers={availableDrivers}
                    busy={busyId === order.id}
                    onAssign={(driverId) =>
                      run(order.id, () => api.orders.assign(order.id, driverId), `Driver assigned to ${order.orderNumber}.`)
                    }
                    onAdvance={(status) =>
                      run(order.id, () => api.orders.setStatus(order.id, status), `${order.orderNumber} is now ${status.replaceAll('_', ' ')}.`)
                    }
                    onCancel={() => run(order.id, () => api.orders.cancel(order.id), `${order.orderNumber} cancelled.`)}
                  />
                </Td>
              </tr>
            ))}
          </tbody>
        </TableWrap>
      </Card>

      <Modal open={createOpen} title="New order" onClose={() => setCreateOpen(false)}>
        <form onSubmit={submitCreate} className="space-y-4">
          <Field label="Customer" name="customerId" error={fieldErrors.customerId}>
            <select
              name="customerId"
              required
              value={form.customerId}
              onChange={(e) => setForm({ ...form, customerId: e.target.value })}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100"
            >
              <option value="">Select a customer…</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.name} — {customer.address}
                </option>
              ))}
            </select>
          </Field>

          <Field
            label="Pickup address"
            name="pickupAddress"
            required
            placeholder="Bandra, Mumbai"
            value={form.pickupAddress}
            error={fieldErrors.pickupAddress}
            onChange={(e) => setForm({ ...form, pickupAddress: e.target.value })}
          />

          <Field
            label="Delivery address"
            name="deliveryAddress"
            required
            placeholder="Andheri, Mumbai"
            value={form.deliveryAddress}
            error={fieldErrors.deliveryAddress}
            onChange={(e) => setForm({ ...form, deliveryAddress: e.target.value })}
          />

          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="secondary" onClick={() => setCreateOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Creating…' : 'Create order'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

/**
 * The action buttons are driven by `allowedTransitions` from the API rather than a copy of
 * the state machine here, so the UI can only ever offer moves the backend will accept.
 */
function RowActions({ order, availableDrivers, busy, onAssign, onAdvance, onCancel }) {
  const [driverId, setDriverId] = useState('')

  const canCancel = order.allowedTransitions.includes('CANCELLED')
  const advanceTargets = order.allowedTransitions.filter((s) => s !== 'CANCELLED' && s !== 'ASSIGNED')

  if (order.status === 'CREATED') {
    return (
      <div className="flex flex-wrap items-center justify-end gap-2">
        <select
          value={driverId}
          onChange={(e) => setDriverId(e.target.value)}
          disabled={busy || availableDrivers.length === 0}
          className="rounded-lg border border-slate-300 px-2 py-1.5 text-xs text-slate-700 outline-none focus:border-indigo-400"
        >
          <option value="">{availableDrivers.length === 0 ? 'No drivers free' : 'Assign driver…'}</option>
          {availableDrivers.map((driver) => (
            <option key={driver.id} value={driver.id}>
              {driver.name} · {driver.vehicleNumber}
            </option>
          ))}
        </select>
        <Button disabled={!driverId || busy} onClick={() => onAssign(Number(driverId))}>
          Assign
        </Button>
        {canCancel && (
          <Button variant="danger" disabled={busy} onClick={onCancel}>
            Cancel
          </Button>
        )}
      </div>
    )
  }

  return (
    <div className="flex flex-wrap items-center justify-end gap-2">
      {advanceTargets.map((target) => (
        <Button key={target} disabled={busy} onClick={() => onAdvance(target)}>
          {LABELS[target] ?? target}
        </Button>
      ))}
      {canCancel && (
        <Button variant="danger" disabled={busy} onClick={onCancel}>
          Cancel
        </Button>
      )}
      {advanceTargets.length === 0 && !canCancel && <span className="text-xs text-slate-400">—</span>}
    </div>
  )
}

const LABELS = {
  PICKED_UP: 'Mark picked up',
  OUT_FOR_DELIVERY: 'Out for delivery',
  DELIVERED: 'Mark delivered',
}
