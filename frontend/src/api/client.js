const BASE = '/api'

/**
 * Error carrying the backend's structured failure body.
 *
 * The API always answers with { status, error, message, path, timestamp } and adds
 * `fieldErrors` on validation failures, so the UI can show one banner message and still
 * highlight the individual inputs that were rejected.
 */
export class ApiError extends Error {
  constructor(status, message, fieldErrors) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors ?? null
  }
}

async function request(path, { method = 'GET', body } = {}) {
  let response
  try {
    response = await fetch(`${BASE}${path}`, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    // fetch only rejects on a transport failure, which here means the backend is down.
    throw new ApiError(0, 'Cannot reach the server. Is the Spring Boot app running on :8080?')
  }

  if (response.status === 204) return null

  const text = await response.text()
  const payload = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new ApiError(
      response.status,
      payload?.message || `Request failed with status ${response.status}`,
      payload?.fieldErrors,
    )
  }

  return payload
}

export const api = {
  customers: {
    list: () => request('/customers'),
    get: (id) => request(`/customers/${id}`),
    create: (data) => request('/customers', { method: 'POST', body: data }),
    remove: (id) => request(`/customers/${id}`, { method: 'DELETE' }),
  },
  drivers: {
    list: () => request('/drivers'),
    available: () => request('/drivers/available'),
    get: (id) => request(`/drivers/${id}`),
    create: (data) => request('/drivers', { method: 'POST', body: data }),
    setStatus: (id, status) => request(`/drivers/${id}/status`, { method: 'PUT', body: { status } }),
    setLocation: (id, data) => request(`/drivers/${id}/location`, { method: 'PUT', body: data }),
    orders: (id) => request(`/drivers/${id}/orders`),
  },
  orders: {
    list: () => request('/orders'),
    get: (id) => request(`/orders/${id}`),
    stats: () => request('/orders/stats'),
    create: (data) => request('/orders', { method: 'POST', body: data }),
    assign: (id, driverId) => request(`/orders/${id}/assign/${driverId}`, { method: 'PUT' }),
    setStatus: (id, status) => request(`/orders/${id}/status`, { method: 'PUT', body: { status } }),
    cancel: (id) => request(`/orders/${id}`, { method: 'DELETE' }),
  },
}
