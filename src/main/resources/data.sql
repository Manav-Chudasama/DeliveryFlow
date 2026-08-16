-- Seed data so a fresh database is not empty.
-- Runs after Hibernate creates the schema (spring.jpa.defer-datasource-initialization=true).
-- INSERT IGNORE keeps restarts idempotent: the unique keys on email / phone / vehicle_number
-- mean re-running this on an already-seeded database is a no-op rather than an error.

INSERT IGNORE INTO customers (id, name, email, phone, address, created_at) VALUES
  (1, 'Rahul Sharma',  'rahul@example.com',  '9876543210', 'Andheri West, Mumbai',  NOW()),
  (2, 'Priya Nair',    'priya@example.com',  '9876543220', 'Koramangala, Bengaluru', NOW()),
  (3, 'Vikram Desai',  'vikram@example.com', '9876543230', 'Bandra East, Mumbai',   NOW());

INSERT IGNORE INTO drivers (id, name, phone, vehicle_number, status, current_location, created_at) VALUES
  (1, 'Amit Patel',   '9876543211', 'MH01AB1234', 'AVAILABLE', 'Andheri, Mumbai',    NOW()),
  (2, 'Neha Kulkarni','9876543212', 'MH02CD5678', 'AVAILABLE', 'Dadar, Mumbai',      NOW()),
  (3, 'Suresh Rao',   '9876543213', 'KA05EF9012', 'OFFLINE',   'Indiranagar, Bengaluru', NOW());
