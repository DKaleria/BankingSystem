--liquibase formatted sql
--changeset Valeriya:07-03-2025-created-author-tbl runOnChange:false

CREATE TABLE IF NOT EXISTS incomes (
                         id SERIAL PRIMARY KEY,
                         username VARCHAR(100) NOT NULL,
                         amount DECIMAL NOT NULL,
                         source VARCHAR(100),
                         date DATE NOT NULL,
                         description TEXT
);
