--liquibase formatted sql
--changeset Valeriya:01-12-2024-created-author-tbl runOnChange:false

CREATE TABLE IF NOT EXISTS users (
                       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                       username VARCHAR(20) UNIQUE NOT NULL,
                       birth_date DATE,
                       firstname VARCHAR(20),
                       lastname VARCHAR(20),
                       password VARCHAR(120) NOT NULL,
                       email VARCHAR(255),
                       role VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'ADMIN', 'MANAGER'))
);