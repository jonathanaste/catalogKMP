-- V3__Evolve_Product_Schema.sql
-- Evolves the products table to align with v2.0 specifications.

-- Renaming 'stock_quantity' to 'current_stock' for consistency with the roadmap.
ALTER TABLE products RENAME COLUMN stock_quantity TO current_stock;

-- Adding new columns as defined in the technical document.
ALTER TABLE products ADD COLUMN sku VARCHAR(255);
ALTER TABLE products ADD COLUMN sale_price DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN weight_kg DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0;
ALTER TABLE products ADD COLUMN review_count INTEGER NOT NULL DEFAULT 0;

-- Using a PostgreSQL text array to store multiple image URLs.
ALTER TABLE products ADD COLUMN additional_image_urls TEXT;

-- It's good practice to add a unique constraint to the SKU.
ALTER TABLE products ADD CONSTRAINT products_sku_unique UNIQUE (sku);

-- Back-populate a simple SKU from the product name for existing products to satisfy the constraint.
-- This is a placeholder; real SKUs should be updated manually via the admin panel.
UPDATE products SET sku = 'SKU-' || id WHERE sku IS NULL;

-- Now that existing rows have a value, we can enforce the NOT NULL constraint.
ALTER TABLE products ALTER COLUMN sku SET NOT NULL;