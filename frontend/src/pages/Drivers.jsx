import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'
import Field from '../components/Field'
import Modal from '../components/Modal'
import StatusBadge from '../components/StatusBadge'
import { Banner, Button, Card, EmptyRow, Td, TableWrap, Th } from '../components/Ui'

const EMPTY_FORM = { name: '', phone: '', vehicleNumber: '', currentLocation: '' }
const EMPTY_LOCATION = { latitude: '', longitude: '', currentLocation: '' }

export default function Drivers() {
  const [drivers, setDrivers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)
  const [busyId, setBusyId] = useState(null)

  const [createOpen, setCreateOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  const [locationFor, setLocationFor] = useState(null)
  const [locationForm, setLocationForm] = useState(EMPTY_LOCATION)

  const [ordersFor, setOrdersFor] = useState(null)
  const [driverOrders, setDriverOrders] = useState([])

  const load = useCallback(async () => {
    try {
      setDrivers(await api.drivers.list())
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
      await api.drivers.create(form)
      setCreateOpen(false)
      setForm(EMPTY_FORM)
      setNotice('Driver added.')
      await load()
    } catch (err) {
      setFieldErrors(err.fieldErrors ?? {})
      setError(err.fieldErrors ? 'Please correct the highlighted fields.' : err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const submitLocation = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setFieldErrors({})
    try {
      await api.drivers.setLocation(locationFor.id, {
        latitude: Number(locationForm.latitude),
        longitude: Number(locationForm.longitude),
        currentLocation: locationForm.currentLocation || null,
      })
      setLocationFor(null)
      setNotice(`Location updated for ${locationFor.name}.`)
      await load()
    } catch (err) {
      setFieldErrors(err.fieldErrors ?? {})
      setError(err.fieldErrors ? 'Please correct the highlighted fields.' : err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const openOrders = async (driver) => {
    setOrdersFor(driver)
    setDriverOrders([])
    try {
      setDriverOrders(await api.drivers.orders(driver.id))
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">Drivers</h1>
          <p className="mt-0.5 text-sm text-slate-500">
            BUSY is set by the order lifecycle — only AVAILABLE and OFFLINE can be toggled here.
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>+ New driver</Button>
      </div>

      <Banner onDismiss={() => setError(null)}>{error}</Banner>
      <Banner tone="success" onDismiss={() => setNotice(null)}>
        {notice}
      </Banner>

      <Card>
        <TableWrap>
          <thead>
            <tr>
              <Th>Driver</Th>
              <Th>Phone</Th>
              <Th>Vehicle</Th>
              <Th>Location</Th>
              <Th>Status</Th>
              <Th className="text-right">Actions</Th>
            </tr>
          </thead>
          <tbody>
            {loading && <EmptyRow colSpan={6}>Loading…</EmptyRow>}
            {!loading && drivers.length === 0 && <EmptyRow colSpan={6}>No drivers yet.</EmptyRow>}
            {drivers.map((driver) => (
              <tr key={driver.id} className="hover:bg-slate-50">
                <Td className="font-medium whitespace-nowrap text-slate-900">{driver.name}</Td>
                <Td className="whitespace-nowrap tabular-nums">{driver.phone}</Td>
                <Td className="whitespace-nowrap">{driver.vehicleNumber}</Td>
                <Td className="text-xs text-slate-500">
                  <div>{driver.currentLocation ?? '—'}</div>
                  {driver.latitude != null && (
                    <div className="text-slate-400 tabular-nums">
                      {driver.latitude.toFixed(4)}, {driver.longitude.toFixed(4)}
                    </div>
                  )}
                </Td>
                <Td>
                  <StatusBadge status={driver.status} kind="driver" />
                </Td>
                <Td>
                  <div className="flex flex-wrap justify-end gap-2">
                    {driver.status !== 'BUSY' && (
                      <Button
                        variant="secondary"
                        disabled={busyId === driver.id}
                        onClick={() =>
                          run(
                            driver.id,
                            () => api.drivers.setStatus(driver.id, driver.status === 'AVAILABLE' ? 'OFFLINE' : 'AVAILABLE'),
                            `${driver.name} is now ${driver.status === 'AVAILABLE' ? 'OFFLINE' : 'AVAILABLE'}.`,
                          )
                        }
                      >
                        {driver.status === 'AVAILABLE' ? 'Go offline' : 'Go available'}
                      </Button>
                    )}
                    <Button
                      variant="secondary"
                      onClick={() => {
                        setLocationFor(driver)
                        setLocationForm({
                          latitude: driver.latitude ?? '',
                          longitude: driver.longitude ?? '',
                          currentLocation: driver.currentLocation ?? '',
                        })
                      }}
                    >
                      Location
                    </Button>
                    <Button variant="secondary" onClick={() => openOrders(driver)}>
                      Orders
                    </Button>
                  </div>
                </Td>
              </tr>
            ))}
          </tbody>
        </TableWrap>
      </Card>

      <Modal open={createOpen} title="New driver" onClose={() => setCreateOpen(false)}>
        <form onSubmit={submitCreate} className="space-y-4">
          <Field
            label="Name"
            name="name"
            required
            placeholder="Amit Patel"
            value={form.name}
            error={fieldErrors.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
          <Field
            label="Phone (10 digits)"
            name="phone"
            required
            inputMode="numeric"
            placeholder="9876543211"
            value={form.phone}
            error={fieldErrors.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
          />
          <Field
            label="Vehicle number"
            name="vehicleNumber"
            required
            placeholder="MH01AB1234"
            value={form.vehicleNumber}
            error={fieldErrors.vehicleNumber}
            onChange={(e) => setForm({ ...form, vehicleNumber: e.target.value.toUpperCase() })}
          />
          <Field
            label="Current location (optional)"
            name="currentLocation"
            placeholder="Andheri, Mumbai"
            value={form.currentLocation}
            error={fieldErrors.currentLocation}
            onChange={(e) => setForm({ ...form, currentLocation: e.target.value })}
          />
          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="secondary" onClick={() => setCreateOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Saving…' : 'Add driver'}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={Boolean(locationFor)} title={`Update location — ${locationFor?.name ?? ''}`} onClose={() => setLocationFor(null)}>
        <form onSubmit={submitLocation} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Field
              label="Latitude"
              name="latitude"
              required
              type="number"
              step="any"
              placeholder="19.1197"
              value={locationForm.latitude}
              error={fieldErrors.latitude}
              onChange={(e) => setLocationForm({ ...locationForm, latitude: e.target.value })}
            />
            <Field
              label="Longitude"
              name="longitude"
              required
              type="number"
              step="any"
              placeholder="72.9089"
              value={locationForm.longitude}
              error={fieldErrors.longitude}
              onChange={(e) => setLocationForm({ ...locationForm, longitude: e.target.value })}
            />
          </div>
          <Field
            label="Area label"
            name="currentLocation"
            placeholder="Powai, Mumbai"
            value={locationForm.currentLocation}
            error={fieldErrors.currentLocation}
            onChange={(e) => setLocationForm({ ...locationForm, currentLocation: e.target.value })}
          />
          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="secondary" onClick={() => setLocationFor(null)}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              Save location
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={Boolean(ordersFor)} title={`Orders — ${ordersFor?.name ?? ''}`} onClose={() => setOrdersFor(null)}>
        {driverOrders.length === 0 ? (
          <p className="py-4 text-sm text-slate-500">No orders assigned to this driver.</p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {driverOrders.map((order) => (
              <li key={order.id} className="flex items-center justify-between gap-3 py-2.5">
                <div>
                  <p className="text-sm font-medium text-slate-900">{order.orderNumber}</p>
                  <p className="text-xs text-slate-500">
                    {order.pickupAddress} → {order.deliveryAddress}
                  </p>
                </div>
                <StatusBadge status={order.status} />
              </li>
            ))}
          </ul>
        )}
      </Modal>
    </div>
  )
}
