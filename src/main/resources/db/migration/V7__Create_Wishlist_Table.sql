-- V7__Create_Wishlist_Table.sql
-- Creates the table to store items in a user's wishlist.

CREATE TABLE wishlist_items (
    -- Foreign key to the users table. If a user is deleted, their wishlist items are also deleted.
                                user_id VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Foreign key to the products table. If a product is deleted, it's removed from all wishlists.
                                product_id VARCHAR(128) NOT NULL REFERENCES products(id) ON DELETE CASCADE,

    -- The date the item was added, stored as a UNIX timestamp.
                                date_added BIGINT NOT NULL,

    -- A composite primary key to ensure a user cannot add the same product to their wishlist more than once.
                                PRIMARY KEY (user_id, product_id)
);

-- An index can be useful for quickly finding all users who have a specific product on their wishlist.
CREATE INDEX idx_wishlist_items_product_id ON wishlist_items(product_id);