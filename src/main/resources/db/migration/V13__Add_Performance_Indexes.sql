-- V13__Add_Performance_Indexes.sql
-- Adds indexes to critical columns to optimize query performance for the reseller portal.

-- Optimizes fetching all orders for a specific reseller.
-- This is heavily used in the dashboard and customer CRM.
CREATE INDEX IF NOT EXISTS idx_orders_reseller_id ON orders(reseller_id);

-- Optimizes filtering orders by their status, which is done in almost every
-- commission and sales calculation.
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- Optimizes the initial user lookup when a reseller logs in or their profile is fetched.
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);