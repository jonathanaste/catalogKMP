-- V5__Create_Product_Reviews_Table.sql
-- Creates the table for storing product reviews and ratings.

CREATE TABLE product_reviews (
                                 id VARCHAR(128) PRIMARY KEY,
                                 product_id VARCHAR(128) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                 user_id VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 user_name VARCHAR(255) NOT NULL,
                                 rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                                 title VARCHAR(255) NOT NULL,
                                 comment TEXT NOT NULL,
                                 photo_urls TEXT[] DEFAULT ARRAY[]::TEXT[], -- Using PostgreSQL array for photo URLs
                                 date BIGINT NOT NULL
);

-- Adding an index for faster retrieval of reviews for a specific product
CREATE INDEX idx_product_reviews_product_id ON product_reviews(product_id);