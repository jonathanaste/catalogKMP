-- V4__Enhance_Order_Schema.sql
-- Enhances the orders table to align with the v2.0 specification for checkout.

-- 1. Alter the existing 'orders' table
ALTER TABLE orders
    ADD COLUMN shipping_address JSONB, -- Using JSONB to store the complex address object
    ADD COLUMN mp_preference_id VARCHAR(255); -- To store the Mercado Pago preference ID

-- 2. Update the status column to use the new default status from the roadmap
-- We'll also change the existing statuses to the new default.
ALTER TABLE orders
    ALTER COLUMN status SET DEFAULT 'PENDING_PAYMENT';

-- Update existing orders to a known status if they are in the old "PROCESANDO" state.
UPDATE orders SET status = 'PAID' WHERE status = 'PROCESANDO';