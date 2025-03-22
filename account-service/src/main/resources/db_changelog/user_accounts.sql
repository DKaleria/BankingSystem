--liquibase formatted sql
--changeset Valeriya:03-12-2024-created-author-tbl runOnChange:false

CREATE TABLE accounts (
                          id BIGINT PRIMARY KEY,
                          username VARCHAR(20) UNIQUE NOT NULL,
                          firstname VARCHAR(20),
                          lastname VARCHAR(20),
                          email VARCHAR(20) UNIQUE NOT NULL,
                          birth_date DATE,
                          password VARCHAR(120) NOT NULL,
                          registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          active BOOLEAN DEFAULT TRUE,
                          role VARCHAR(10) CHECK (role IN ('USER', 'ADMIN', 'MANAGER'))
);