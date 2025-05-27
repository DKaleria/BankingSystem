--liquibase formatted sql
--changeset Valeriya:07-03-2025-created-author-tbl runOnChange:false

CREATE TABLE IF NOT EXISTS monthly_reports (
                                 id SERIAL PRIMARY KEY,
                                 username VARCHAR(255) NOT NULL,
                                 month INT NOT NULL,
                                 year INT NOT NULL,
                                 total_income DECIMAL(15, 2) NOT NULL,
                                 total_expense DECIMAL(15, 2) NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);