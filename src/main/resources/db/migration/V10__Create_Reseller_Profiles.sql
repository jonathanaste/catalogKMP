-- V10__Create_Reseller_Profiles.sql
-- Creates the table to store specific data for users with the 'RESELLER' role.

CREATE TABLE reseller_profiles (
    -- This establishes a 1-to-1 relationship with the users table.
    -- If a user is deleted, their reseller profile is also deleted.
                                   user_id VARCHAR(128) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,

    -- The unique, URL-friendly slug for the reseller's digital store (e.g., "juana-perez").
    -- This is critical for sales attribution as per the roadmap.
                                   unique_store_slug VARCHAR(100) NOT NULL UNIQUE,

    -- The commission percentage this reseller earns on sales.
                                   commission_rate DOUBLE PRECISION NOT NULL DEFAULT 20.0,

    -- A flag for administrators to activate or deactivate a reseller's account.
                                   is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- An index on the slug is crucial for fast lookups when attributing sales from a store URL.
CREATE INDEX idx_reseller_profiles_slug ON reseller_profiles(unique_store_slug);