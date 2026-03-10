-- Seed core catalog with 3 products and corresponding inventory rows.
WITH seeded_products AS (
    INSERT INTO products (sku, name, price, currency, active)
        VALUES ('SKU-APPLE-IPHONE-15', 'Apple iPhone 15 128GB', 999.00, 'USD', true),
               ('SKU-SAMSUNG-S24-256', 'Samsung Galaxy S24 256GB', 1099.00, 'USD', true),
               ('SKU-SONY-WH1000XM5', 'Sony WH-1000XM5 Headphones', 399.00, 'USD', true)
        ON CONFLICT (sku) DO UPDATE
            SET name = EXCLUDED.name,
                price = EXCLUDED.price,
                currency = EXCLUDED.currency,
                active = EXCLUDED.active,
                updated_at = now()
        RETURNING id, sku)
INSERT
INTO inventory (product_id, available_qty, reserved_qty, version, updated_at)
SELECT sp.id,
       CASE sp.sku
           WHEN 'SKU-APPLE-IPHONE-15' THEN 25
           WHEN 'SKU-SAMSUNG-S24-256' THEN 20
           WHEN 'SKU-SONY-WH1000XM5' THEN 40
           END AS available_qty,
       0       AS reserved_qty,
       0       AS version,
       now()   AS updated_at
FROM seeded_products sp
ON CONFLICT (product_id) DO UPDATE
    SET available_qty = EXCLUDED.available_qty,
        reserved_qty  = EXCLUDED.reserved_qty,
        version       = EXCLUDED.version,
        updated_at    = EXCLUDED.updated_at;
