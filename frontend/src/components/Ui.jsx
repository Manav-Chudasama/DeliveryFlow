/** Small presentational pieces shared across the pages. */

export function Button({ variant = 'primary', className = '', ...props }) {
  const variants = {
    primary: 'bg-indigo-600 text-white hover:bg-indigo-500 disabled:bg-indigo-300',
    secondary: 'bg-white text-slate-700 ring-1 ring-slate-300 hover:bg-slate-50 disabled:text-slate-400',
    danger: 'bg-white text-rose-600 ring-1 ring-rose-200 hover:bg-rose-50 disabled:text-rose-300',
  }

  return (
    <button
      className={`inline-flex items-center justify-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed ${variants[variant]} ${className}`}
      {...props}
    />
  )
}

export function Card({ title, actions, children }) {
  return (
    <section className="rounded-xl bg-white shadow-sm ring-1 ring-slate-200">
      {(title || actions) && (
        <header className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-5 py-3.5">
          <h2 className="text-sm font-semibold text-slate-900">{title}</h2>
          {actions}
        </header>
      )}
      {children}
    </section>
  )
}

/** Tables scroll inside their own container so the page never scrolls sideways. */
export function TableWrap({ children }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[640px] border-collapse text-sm">{children}</table>
    </div>
  )
}

export function Th({ children, className = '' }) {
  return (
    <th className={`border-b border-slate-200 px-5 py-2.5 text-left text-xs font-semibold tracking-wide text-slate-500 uppercase ${className}`}>
      {children}
    </th>
  )
}

export function Td({ children, className = '' }) {
  return <td className={`border-b border-slate-100 px-5 py-3 text-slate-700 ${className}`}>{children}</td>
}

export function Banner({ tone = 'error', children, onDismiss }) {
  if (!children) return null

  const tones = {
    error: 'bg-rose-50 text-rose-800 ring-rose-200',
    success: 'bg-emerald-50 text-emerald-800 ring-emerald-200',
  }

  return (
    <div className={`mb-4 flex items-start justify-between gap-3 rounded-lg px-4 py-3 text-sm ring-1 ring-inset ${tones[tone]}`}>
      <span>{children}</span>
      {onDismiss && (
        <button type="button" onClick={onDismiss} className="shrink-0 font-medium opacity-70 hover:opacity-100">
          Dismiss
        </button>
      )}
    </div>
  )
}

export function EmptyRow({ colSpan, children }) {
  return (
    <tr>
      <td colSpan={colSpan} className="px-5 py-10 text-center text-sm text-slate-500">
        {children}
      </td>
    </tr>
  )
}
