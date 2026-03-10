-- Seed initial users for local/dev authentication.
-- Uses PostgreSQL pgcrypto bcrypt hashes compatible with Spring BCryptPasswordEncoder.
INSERT INTO users (email, role, password_hash)
VALUES
    ('admin', 'ADMIN', crypt('admin', gen_salt('bf'))),
    ('customer', 'CUSTOMER', crypt('customer', gen_salt('bf')))
ON CONFLICT (email) DO NOTHING;
