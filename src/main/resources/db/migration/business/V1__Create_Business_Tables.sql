-- V1__Create_Business_Domain.sql

CREATE SCHEMA IF NOT EXISTS business;
SET search_path TO business;

CREATE TYPE business.role_type_enum AS ENUM (
    'ROLE_CLIENT',
    'ROLE_ADMIN',
    'ROLE_TECH_SUPPORT',
    'ROLE_MANAGER',
    'ROLE_SECURITY_OFFICER',
    'ROLE_SECURITY_SUPERVISOR',
    'ROLE_SECURITY_TOP_SUPERVISOR',
    'ROLE_TOP_MANAGEMENT'
    );

CREATE TABLE accounts (
                          id                BIGSERIAL PRIMARY KEY,
                          balance           DECIMAL(19, 2) NOT NULL,
                          initial_balance   DECIMAL(19, 2) NOT NULL,
                          version           BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE users (
                       id               BIGSERIAL PRIMARY KEY,
                       username         VARCHAR(255) NOT NULL UNIQUE,
                       password         VARCHAR(255) NOT NULL,
                       date_of_birth    DATE NOT NULL,
                       account_id       BIGINT NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
                       version          BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE user_roles (
                            user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role        role_type_enum NOT NULL,
                            PRIMARY KEY (user_id, role)
);

CREATE TABLE email_data (
                            id                BIGSERIAL PRIMARY KEY,
                            email             VARCHAR(255) NOT NULL UNIQUE,
                            user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            version           BIGINT DEFAULT 0 NOT NULL
);

CREATE TABLE phone_data (
                            id                 BIGSERIAL PRIMARY KEY,
                            phone              VARCHAR(20) NOT NULL UNIQUE,
                            user_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            version            BIGINT DEFAULT 0 NOT NULL
);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Индекс для быстрого поиска по LIKE 'text%'
CREATE INDEX IF NOT EXISTS idx_users_username_trgm ON users USING GIN (LOWER(username) gin_trgm_ops);

-- Индексы для быстрых проверок на уникальность и для JOIN'ов
CREATE INDEX IF NOT EXISTS idx_email_data_email ON email_data(email);
CREATE INDEX IF NOT EXISTS idx_phone_data_phone ON phone_data(phone);
CREATE INDEX IF NOT EXISTS idx_users_account_id ON users(account_id);
CREATE INDEX IF NOT EXISTS idx_email_data_user_id ON email_data(user_id);
CREATE INDEX IF NOT EXISTS idx_phone_data_user_id ON phone_data(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);