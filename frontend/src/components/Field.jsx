/**
 * Labelled input that renders the backend's per-field validation message underneath.
 *
 * The API returns `fieldErrors` keyed by the DTO property name, so passing that object
 * straight through means server-side validation is surfaced without duplicating the rules
 * in the browser.
 */
export default function Field({ label, name, error, children, ...inputProps }) {
  const describedBy = error ? `${name}-error` : undefined

  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-slate-700">{label}</span>
      {children ?? (
        <input
          name={name}
          aria-invalid={Boolean(error)}
          aria-describedby={describedBy}
          className={`w-full rounded-lg border px-3 py-2 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:ring-2 ${
            error
              ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-100'
              : 'border-slate-300 focus:border-indigo-400 focus:ring-indigo-100'
          }`}
          {...inputProps}
        />
      )}
      {error && (
        <span id={describedBy} className="mt-1 block text-xs text-rose-600">
          {error}
        </span>
      )}
    </label>
  )
}
