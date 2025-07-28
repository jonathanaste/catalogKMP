-- V11__Add_Reseller_Attribution_To_Orders.sql
-- Adds a column to the orders table to link a sale to a specific reseller.

ALTER TABLE orders
    ADD COLUMN reseller_id VARCHAR(128) REFERENCES users(id) ON DELETE SET NULL;

-- An index on this new column will be useful for quickly finding all orders for a reseller.
CREATE INDEX idx_orders_reseller_id ON orders(reseller_id);