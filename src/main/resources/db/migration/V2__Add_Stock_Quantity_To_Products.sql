-- V2__Add_Stock_Quantity_To_Products.sql

-- Primero eliminamos la columna booleana que ya no usaremos
ALTER TABLE products DROP COLUMN in_stock;

-- Luego, añadimos la nueva columna numérica para la cantidad de stock
-- La ponemos como NOT NULL y con un valor por defecto de 0 para los productos existentes
ALTER TABLE products ADD COLUMN stock_quantity INTEGER NOT NULL DEFAULT 0;