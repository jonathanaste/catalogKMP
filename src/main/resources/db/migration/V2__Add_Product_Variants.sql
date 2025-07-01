-- V2__Add_Product_Variants.sql

-- Add a column to products table to indicate if it has variants
ALTER TABLE products
    ADD COLUMN has_variants BOOLEAN NOT NULL DEFAULT FALSE;

-- Create the product_variants table
CREATE TABLE product_variants (
    id VARCHAR(128) PRIMARY KEY,
    product_id VARCHAR(128) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100) UNIQUE, -- Stock Keeping Unit, can be nullable if not strictly enforced initially
    name VARCHAR(255), -- e.g., "Large / Red", or can be derived from attributes
    price DOUBLE PRECISION NOT NULL, -- Each variant has its own price
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    attributes JSONB, -- For storing key-value pairs like {"color": "Red", "size": "L"}
    image_url VARCHAR(1024), -- Optional: if variant has a specific image
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Optional: Add an index on product_id for faster lookups of variants for a product
CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants(product_id);

-- Optional: Trigger to update updated_at timestamp (PostgreSQL specific)
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_product_variants_updated_at
BEFORE UPDATE ON product_variants
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

-- Note: If the base product's price and stock_quantity become just placeholders
-- when variants exist, you might consider making them nullable or adjusting logic.
-- For now, we'll manage this at the application layer.
-- The 'has_variants' flag will help the application know how to treat the base product's fields.

-- Also, if a product transitions from no variants to having variants,
-- its base 'stock_quantity' and 'price' might need to be handled carefully
-- (e.g., set base stock to 0, or base price becomes indicative).
-- This logic will be in the application/repository layer.
