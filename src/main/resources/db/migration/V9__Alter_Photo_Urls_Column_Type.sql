-- V9__Alter_Photo_Urls_Column_Type.sql
-- Alters the photo_urls column in the product_reviews table from a text array (TEXT[])
-- to a simple TEXT type. This aligns the schema with the application's method
-- of storing the URLs as a single, comma-separated string.

ALTER TABLE product_reviews
    ALTER COLUMN photo_urls TYPE TEXT;