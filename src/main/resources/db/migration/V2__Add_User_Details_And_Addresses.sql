-- V2__Add_User_Details_And_Addresses.sql
-- Aligns the database schema with the v2.0 technical document specifications.

-- 1. Alter the existing 'users' table.
-- We add the new detailed fields for the user's name and phone.
ALTER TABLE users ADD COLUMN first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN last_name VARCHAR(255);
ALTER TABLE users ADD COLUMN phone VARCHAR(50);

-- This command attempts to populate the new name fields from the old 'name' column.
-- It splits the name on the first space, which is a reasonable default.
UPDATE users SET first_name = split_part(name, ' ', 1), last_name = substring(name from position(' ' in name) + 1);

-- Now that the data is migrated, we can enforce the NOT NULL constraint and drop the old column.
ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE users DROP COLUMN name;


-- 2. Create the new 'addresses' table.
-- This table stores shipping addresses associated with a user account.
CREATE TABLE addresses (
                           id VARCHAR(128) PRIMARY KEY,
                           user_id VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           alias VARCHAR(100) NOT NULL,
                           street VARCHAR(255) NOT NULL,
                           "number" VARCHAR(50) NOT NULL,
                           postal_code VARCHAR(50) NOT NULL,
                           city VARCHAR(100) NOT NULL,
                           state VARCHAR(100) NOT NULL,
                           is_default BOOLEAN NOT NULL DEFAULT FALSE,
                           UNIQUE(user_id, alias) -- A user cannot have two addresses with the same alias.
);