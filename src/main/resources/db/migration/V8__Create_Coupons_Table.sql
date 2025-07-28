-- V8__Create_Coupons_Table.sql
-- Creates the table for managing discount coupons.

CREATE TABLE coupons (
    -- The unique, user-facing coupon code (e.g., 'SUMMER25'). Case-sensitive.
    code VARCHAR(100) PRIMARY KEY,

    -- An internal description for administrators (e.g., 'Summer Sale 2025 - 25% off').
    description TEXT NOT NULL,

    -- The type of discount: 'PERCENTAGE' or 'FIXED_AMOUNT'.
    discount_type VARCHAR(50) NOT NULL,

    -- The value of the discount. E.g., 25.0 for a percentage, or 500.0 for a fixed amount.
    discount_value DOUBLE PRECISION NOT NULL,

    -- Optional expiration date, stored as a UNIX timestamp. NULL means it never expires.
    expiration_date BIGINT,

    -- The total number of times this coupon can be used across all users. NULL for unlimited.
    usage_limit INTEGER,

    -- A counter for how many times the coupon has been used.
    usage_count INTEGER NOT NULL DEFAULT 0,

    -- A simple flag to quickly enable or disable the coupon.
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- It can be beneficial to add an extra table to track which user used which coupon on which order.
CREATE TABLE order_coupons (
    order_id VARCHAR(128) NOT NULL REFERENCES orders(id),
    coupon_code VARCHAR(100) NOT NULL REFERENCES coupons(code),
    -- The actual amount that was discounted on this specific order.
    discount_amount DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (order_id, coupon_code)
);

-- Also, we need to add columns to the 'orders' table to store discount information.
ALTER TABLE orders
    ADD COLUMN coupon_code VARCHAR(100) REFERENCES coupons(code),
    ADD COLUMN discount_amount DOUBLE PRECISION DEFAULT 0.0;