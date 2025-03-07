--liquibase formatted sql
--changeset Valeriya:03-12-2024-created-author-tbl runOnChange:false

CREATE TABLE accounts (
                          id SERIAL PRIMARY KEY,
                          username VARCHAR(255) UNIQUE NOT NULL
);