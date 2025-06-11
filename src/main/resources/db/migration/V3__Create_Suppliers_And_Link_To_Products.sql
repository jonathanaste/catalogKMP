-- V3__Create_Suppliers_And_Link_To_Products.sql

-- 1. Creamos la nueva tabla para los proveedores
CREATE TABLE suppliers (
                           id VARCHAR(128) PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           contact_person VARCHAR(255),
                           phone VARCHAR(50),
                           email VARCHAR(255) UNIQUE,
                           cbu VARCHAR(22),
                           alias_cbu VARCHAR(100),
                           notes TEXT
);

-- 2. Añadimos la columna 'supplier_id' a la tabla de productos
ALTER TABLE products
    ADD COLUMN supplier_id VARCHAR(128) REFERENCES suppliers(id);