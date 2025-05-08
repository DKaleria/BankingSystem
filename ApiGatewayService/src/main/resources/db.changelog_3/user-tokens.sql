--liquibase formatted sql
--changeset Valeriya:01-12-2024-created-author-tbl runOnChange:false

CREATE TABLE tokens (
                        id SERIAL PRIMARY KEY,
                        username VARCHAR(255) NOT NULL,
                        token TEXT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        expires_at TIMESTAMP
);