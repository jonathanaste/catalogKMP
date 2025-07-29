-- V12__Create_Marketing_Materials_Table.sql
-- Creates a table to store links to marketing resources for resellers.

CREATE TABLE marketing_materials (
                                     id VARCHAR(128) PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
                                     description TEXT,
                                     asset_url VARCHAR(1024) NOT NULL, -- The direct URL to the image, PDF, etc.
                                     asset_type VARCHAR(50) NOT NULL,  -- e.g., 'IMAGE', 'BANNER', 'PDF'
                                     date_added BIGINT NOT NULL
);