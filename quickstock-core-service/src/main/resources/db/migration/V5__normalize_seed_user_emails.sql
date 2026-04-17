-- Normalize legacy seed users to email-formatted values so auth seed data matches the published API contract.
-- Keep this forward-only: update existing legacy rows when possible, ensure canonical rows exist,
-- then remove any remaining non-email legacy seed rows.

UPDATE users
SET email = 'admin@quickstock.local'
WHERE email = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM users u
      WHERE u.email = 'admin@quickstock.local'
  );

UPDATE users
SET email = 'customer@quickstock.local'
WHERE email = 'customer'
  AND NOT EXISTS (
      SELECT 1
      FROM users u
      WHERE u.email = 'customer@quickstock.local'
  );

INSERT INTO users (email, role, password_hash)
SELECT 'admin@quickstock.local', 'ADMIN', crypt('admin', gen_salt('bf'))
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@quickstock.local'
);

INSERT INTO users (email, role, password_hash)
SELECT 'customer@quickstock.local', 'CUSTOMER', crypt('customer', gen_salt('bf'))
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'customer@quickstock.local'
);

DELETE FROM users
WHERE email IN ('admin', 'customer');

