-- V6__Create_Product_Questions_And_Answers_Tables.sql
-- Creates the tables for the product Question & Answer system.

-- Table to store the questions asked by users
CREATE TABLE product_questions (
                                   id VARCHAR(128) PRIMARY KEY,
                                   product_id VARCHAR(128) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                   user_id VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   user_name VARCHAR(255) NOT NULL,
                                   question_text TEXT NOT NULL,
                                   date BIGINT NOT NULL
);

-- Table to store the answers provided by admins
CREATE TABLE question_answers (
                                  question_id VARCHAR(128) PRIMARY KEY REFERENCES product_questions(id) ON DELETE CASCADE,
                                  answer_text TEXT NOT NULL,
                                  date BIGINT NOT NULL
);

-- Index for faster retrieval of questions for a product
CREATE INDEX idx_product_questions_product_id ON product_questions(product_id);