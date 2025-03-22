--liquibase formatted sql
--changeset Valeriya:07-03-2025-created-author-tbl runOnChange:false

CREATE TABLE incomes (
                        income_id UUID PRIMARY KEY,
                        user_id UUID REFERENCES users(id),
                        amount DECIMAL NOT NULL,
                        source VARCHAR(100),
                        date DATE NOT NULL,
                        description TEXT
);