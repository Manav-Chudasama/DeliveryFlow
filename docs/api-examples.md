# API examples

Worked requests and responses for every endpoint, including the failure cases.

All examples assume the backend is running on `http://localhost:8080`. For an interactive
version with the request bodies pre-filled, use **http://localhost:8080/swagger-ui.html**.

On Windows PowerShell, `curl` is an alias for `Invoke-WebRequest` and does not accept these
flags. Either call `curl.exe` explicitly, or use `Invoke-RestMethod`:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/orders -Method POST `
  -ContentType 'application/json' `
  -Body '{"customerId":1,"pickupAddress":"Bandra","deliveryAddress":"Andheri"}'
```

---

## Customers

### Create a customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rahul Sharma",
    "email": "rahul@example.com",
    "phone": "9876543210",
    "address": "Andheri West, Mumbai"
  }'
```

`201 Created`

```json
{
  "id": 1,
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "phone": "9876543210",
  "address": "Andheri West, Mumbai",
  "createdAt": "2026-08-16T15:07:05.764"
}
```

### List customers

```bash
curl http://localhost:8080/api/customers
```

### Get one customer

```bash
curl http://localhost:8080/api/customers/1
```

### Delete a customer

```bash
curl -X DELETE http://localhost:8080/api/customers/1
```

`204 No Content` on success. Refused if the customer has orders — see
[Deleting a customer with orders](#deleting-a-customer-with-orders).

---

## Drivers

### Create a driver

A new driver always starts `AVAILABLE`; the status is not accepted from the client.

```bash
curl -X POST http://localhost:8080/api/drivers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Amit Patel",
    "phone": "9876543211",
    "vehicleNumber": "MH01AB1234",
    "currentLocation": "Andheri, Mumbai"
  }'
```

`201 Created`

```json
{
  "id": 1,
  "name": "Amit Patel",
  "phone": "9876543211",
  "vehicleNumber": "MH01AB1234",
  "status": "AVAILABLE",
  "currentLocation": "Andheri, Mumbai",
  "latitude": null,
  "longitude": null,
  "createdAt": "2026-08-16T15:07:05.820"
}
```

### List drivers

```bash
curl http://localhost:8080/api/drivers
```

### List only assignable drivers

Backs the "Assign driver" dropdown in the UI.

```bash
curl http://localhost:8080/api/drivers/available
```

### Get one driver

```bash
curl http://localhost:8080/api/drivers/1
```

### Update driver status

Only `AVAILABLE` and `OFFLINE` are accepted. `BUSY` is owned by the order lifecycle.

```bash
curl -X PUT http://localhost:8080/api/drivers/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "OFFLINE"}'
```

### Update driver location

```bash
curl -X PUT http://localhost:8080/api/drivers/1/location \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 19.1197,
    "longitude": 72.9089,
    "currentLocation": "Powai, Mumbai"
  }'
```

`200 OK`

```json
{
  "id": 1,
  "name": "Amit Patel",
  "phone": "9876543211",
  "vehicleNumber": "MH01AB1234",
  "status": "AVAILABLE",
  "currentLocation": "Powai, Mumbai",
  "latitude": 19.1197,
  "longitude": 72.9089,
  "createdAt": "2026-08-16T15:07:05.820"
}
```

### Orders assigned to a driver

```bash
curl http://localhost:8080/api/drivers/1/orders
```

---

## Orders

### Create an order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "pickupAddress": "Phoenix Mall, Kurla",
    "deliveryAddress": "Andheri West, Mumbai"
  }'
```

`201 Created`

```json
{
  "id": 1,
  "orderNumber": "ORD-1001",
  "customerId": 1,
  "customerName": "Rahul Sharma",
  "driverId": null,
  "driverName": null,
  "pickupAddress": "Phoenix Mall, Kurla",
  "deliveryAddress": "Andheri West, Mumbai",
  "status": "CREATED",
  "allowedTransitions": ["ASSIGNED", "CANCELLED"],
  "createdAt": "2026-08-16T15:07:05.836",
  "updatedAt": "2026-08-16T15:07:05.836"
}
```

`allowedTransitions` is derived from the state machine on every response, which is what lets
the UI render only the actions the backend will accept.

### List orders

Newest first, with the customer and driver joined in a single query.

```bash
curl http://localhost:8080/api/orders
```

### Dashboard statistics

```bash
curl http://localhost:8080/api/orders/stats
```

`200 OK`

```json
{
  "totalOrders": 5,
  "pending": 2,
  "outForDelivery": 1,
  "delivered": 1,
  "cancelled": 1,
  "totalDrivers": 3,
  "availableDrivers": 1,
  "totalCustomers": 3
}
```

`pending` is the sum of `CREATED`, `ASSIGNED` and `PICKED_UP` — everything accepted but not
yet on its final leg.

### Assign a driver

```bash
curl -X PUT http://localhost:8080/api/orders/1/assign/1
```

`200 OK` — the order moves to `ASSIGNED` and the driver to `BUSY` in one transaction.

```json
{
  "id": 1,
  "orderNumber": "ORD-1001",
  "customerId": 1,
  "customerName": "Rahul Sharma",
  "driverId": 1,
  "driverName": "Amit Patel",
  "pickupAddress": "Phoenix Mall, Kurla",
  "deliveryAddress": "Andheri West, Mumbai",
  "status": "ASSIGNED",
  "allowedTransitions": ["PICKED_UP", "CANCELLED"],
  "createdAt": "2026-08-16T15:07:05.836",
  "updatedAt": "2026-08-16T15:07:05.836"
}
```

### Advance the status

```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "PICKED_UP"}'

curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "OUT_FOR_DELIVERY"}'

curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}'
```

After the final call the order is `DELIVERED` with `"allowedTransitions": []`, and the driver
has been released back to `AVAILABLE`:

```bash
curl http://localhost:8080/api/drivers/1
# → "status": "AVAILABLE"
```

### Cancel an order

```bash
curl -X DELETE http://localhost:8080/api/orders/1
```

`200 OK`. The row is retained with `"status": "CANCELLED"` and the assigned driver is released.
Cancellation is a soft state change, not a delete — an order a driver is part-way through is
history worth keeping.

---

## Failure cases

These are the responses worth checking, because they are where the business rules show up.

### Unknown entity → 404

```bash
curl http://localhost:8080/api/drivers/999
```

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Driver not found with id: 999",
  "path": "/api/drivers/999",
  "timestamp": "2026-08-16T15:07:05.992"
}
```

### Assigning a busy driver → 409

```bash
curl -X PUT http://localhost:8080/api/orders/2/assign/1
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot assign driver Amit Patel: driver is currently BUSY",
  "path": "/api/orders/2/assign/1",
  "timestamp": "2026-08-16T15:07:06.090"
}
```

### Illegal status transition → 409

```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CREATED"}'
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Invalid status transition: DELIVERED -> CREATED. Allowed from DELIVERED: none (terminal state)",
  "path": "/api/orders/1/status",
  "timestamp": "2026-08-16T15:07:06.003"
}
```

### Validation failure → 400

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name": "", "email": "not-an-email", "phone": "123", "address": ""}'
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/customers",
  "timestamp": "2026-08-16T15:07:06.022",
  "fieldErrors": {
    "email": "Email must be a valid address",
    "phone": "Phone must be exactly 10 digits",
    "name": "Name is required",
    "address": "Address is required"
  }
}
```

### Unknown enum value → 400

```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "FLYING"}'
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "'FLYING' is not a valid OrderStatus. Accepted values: CREATED, ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED",
  "path": "/api/orders/1/status",
  "timestamp": "2026-08-16T15:11:59.800"
}
```

Jackson's own message for this case leaks package names and stream offsets, so the handler
reformats it into something a client can act on.

### Duplicate unique value → 409

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name": "Duplicate", "email": "rahul@example.com", "phone": "9999900002", "address": "X"}'
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "A customer with email rahul@example.com already exists",
  "path": "/api/customers",
  "timestamp": "2026-08-16T15:07:06.137"
}
```

### Deleting a customer with orders

```bash
curl -X DELETE http://localhost:8080/api/customers/1
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete customer Rahul Sharma because they have existing orders",
  "path": "/api/customers/1",
  "timestamp": "2026-08-16T15:07:06.137"
}
```

Checked explicitly rather than letting a foreign-key violation surface as a 500.

### Setting a driver BUSY by hand → 409

```bash
curl -X PUT http://localhost:8080/api/drivers/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "BUSY"}'
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "BUSY is set automatically when a driver is assigned to an order and cannot be set manually",
  "path": "/api/drivers/1/status",
  "timestamp": "2026-08-16T15:07:06.153"
}
```

---

## Full walkthrough

The complete happy path, from empty database to delivered order.

```bash
# 1. Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"Rahul Sharma","email":"rahul@example.com","phone":"9876543210","address":"Andheri West, Mumbai"}'

# 2. Create a driver — starts AVAILABLE
curl -X POST http://localhost:8080/api/drivers \
  -H "Content-Type: application/json" \
  -d '{"name":"Amit Patel","phone":"9876543211","vehicleNumber":"MH01AB1234"}'

# 3. Create an order — starts CREATED
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"pickupAddress":"Bandra, Mumbai","deliveryAddress":"Andheri, Mumbai"}'

# 4. Assign the driver — order ASSIGNED, driver BUSY
curl -X PUT http://localhost:8080/api/orders/1/assign/1

# 5. Move through the lifecycle
curl -X PUT http://localhost:8080/api/orders/1/status -H "Content-Type: application/json" -d '{"status":"PICKED_UP"}'
curl -X PUT http://localhost:8080/api/orders/1/status -H "Content-Type: application/json" -d '{"status":"OUT_FOR_DELIVERY"}'
curl -X PUT http://localhost:8080/api/orders/1/status -H "Content-Type: application/json" -d '{"status":"DELIVERED"}'

# 6. Confirm the driver was released
curl http://localhost:8080/api/drivers/1
# → "status": "AVAILABLE"
```
