-- V1__Create_Initial_Tables.sql

CREATE TABLE categories (
                            id VARCHAR(128) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            image_url VARCHAR(1024)
);

CREATE TABLE products (
                          id VARCHAR(128) PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT NOT NULL,
                          price DOUBLE PRECISION NOT NULL,
                          main_image_url VARCHAR(1024) NOT NULL,
                          category_id VARCHAR(128) NOT NULL REFERENCES categories(id),
                          in_stock BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE users (
                       id VARCHAR(128) PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(512) NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL
);

CREATE TABLE cart_items (
                            id SERIAL PRIMARY KEY,
                            user_id VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            product_id VARCHAR(128) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                            quantity INTEGER NOT NULL,
                            UNIQUE(user_id, product_id)
);

CREATE TABLE orders (
                        id VARCHAR(128) PRIMARY KEY,
                        user_id VARCHAR(128) NOT NULL REFERENCES users(id),
                        order_date BIGINT NOT NULL,
                        status VARCHAR(100) NOT NULL,
                        total DOUBLE PRECISION NOT NULL,
                        payment_method VARCHAR(100) NOT NULL,
                        shipping_method VARCHAR(100) NOT NULL
);

CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,
                             order_id VARCHAR(128) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id VARCHAR(128) NOT NULL, -- No hay referencia por si el producto se borra del catálogo
                             product_name VARCHAR(255) NOT NULL,
                             quantity INTEGER NOT NULL,
                             unit_price DOUBLE PRECISION NOT NULL
);