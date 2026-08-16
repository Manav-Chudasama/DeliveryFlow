import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import StatusBadge from '../components/StatusBadge'
import { Banner, Card, EmptyRow, Td, TableWrap, Th } from '../components/Ui'

const TILES = [
  { key: 'totalOrders', label: 'Total orders', accent: 'text-slate-900' },
  { key: 'pending', label: 'Pending', accent: 'text-blue-600' },
  { key: 'outForDelivery', label: 'Out for delivery', accent: 'text-amber-600' },
  { key: 'delivered', label: 'Delivered', accent: 'text-emerald-600' },
]

export default function Dashboard() {
  const [stats, setStats] = useState(null)
  const [orders, setOrders] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    try {
      // Both reads are independent, so they go out together rather than in sequence.
      const [nextStats, nextOrders] = await Promise.all([api.orders.stats(), api.orders.list()])
      setStats(nextStats)
      setOrders(nextOrders)
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

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold tracking-tight text-slate-900">Dashboard</h1>
        <p className="mt-0.5 text-sm text-slate-500">Live view of orders and fleet availability.</p>
      </div>

      <Banner onDismiss={() => setError(null)}>{error}</Banner>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {TILES.map((tile) => (
          <div key={tile.key} className="rounded-xl bg-white px-5 py-4 shadow-sm ring-1 ring-slate-200">
            <p className="text-xs font-medium tracking-wide text-slate-500 uppercase">{tile.label}</p>
            <p className={`mt-1 text-3xl font-semibold tabular-nums ${tile.accent}`}>
              {loading ? '—' : (stats?.[tile.key] ?? 0)}
            </p>
          </div>
        ))}
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <SmallStat label="Drivers available" value={stats?.availableDrivers} total={stats?.totalDrivers} loading={loading} />
        <SmallStat label="Customers" value={stats?.totalCustomers} loading={loading} />
        <SmallStat label="Cancelled orders" value={stats?.cancelled} loading={loading} />
      </div>

      <Card
        title="Recent orders"
        actions={
          <Link to="/orders" className="text-sm font-medium text-indigo-600 hover:text-indigo-500">
            View all →
          </Link>
        }
      >
        <TableWrap>
          <thead>
            <tr>
              <Th>Order</Th>
              <Th>Customer</Th>
              <Th>Driver</Th>
              <Th>Status</Th>
            </tr>
          </thead>
          <tbody>
            {loading && <EmptyRow colSpan={4}>Loading…</EmptyRow>}
            {!loading && orders.length === 0 && <EmptyRow colSpan={4}>No orders yet.</EmptyRow>}
            {orders.slice(0, 8).map((order) => (
              <tr key={order.id} className="hover:bg-slate-50">
                <Td className="font-medium text-slate-900">{order.orderNumber}</Td>
                <Td>{order.customerName}</Td>
                <Td>{order.driverName ?? <span className="text-slate-400">Unassigned</span>}</Td>
                <Td>
                  <StatusBadge status={order.status} />
                </Td>
              </tr>
            ))}
          </tbody>
        </TableWrap>
      </Card>
    </div>
  )
}

function SmallStat({ label, value, total, loading }) {
  return (
    <div className="rounded-xl bg-white px-5 py-3.5 shadow-sm ring-1 ring-slate-200">
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <p className="mt-0.5 text-lg font-semibold tabular-nums text-slate-900">
        {loading ? '—' : (value ?? 0)}
        {total !== undefined && !loading && <span className="text-sm font-normal text-slate-400"> / {total}</span>}
      </p>
    </div>
  )
}
