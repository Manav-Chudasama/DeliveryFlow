const ORDER_STYLES = {
  CREATED: 'bg-slate-100 text-slate-700 ring-slate-200',
  ASSIGNED: 'bg-blue-50 text-blue-700 ring-blue-200',
  PICKED_UP: 'bg-violet-50 text-violet-700 ring-violet-200',
  OUT_FOR_DELIVERY: 'bg-amber-50 text-amber-800 ring-amber-200',
  DELIVERED: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  CANCELLED: 'bg-rose-50 text-rose-700 ring-rose-200',
}

const DRIVER_STYLES = {
  AVAILABLE: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  BUSY: 'bg-amber-50 text-amber-800 ring-amber-200',
  OFFLINE: 'bg-slate-100 text-slate-600 ring-slate-200',
}

/** Renders a status enum as a pill, using the same vocabulary as the backend. */
export default function StatusBadge({ status, kind = 'order' }) {
  const styles = kind === 'driver' ? DRIVER_STYLES : ORDER_STYLES
  const className = styles[status] ?? 'bg-slate-100 text-slate-700 ring-slate-200'

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset whitespace-nowrap ${className}`}
    >
      {status.replaceAll('_', ' ')}
    </span>
  )
}
