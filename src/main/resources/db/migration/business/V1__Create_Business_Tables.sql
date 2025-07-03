
CREATE SCHEMA IF NOT EXISTS business;
SET search_path TO business;

-- Сначала создаем тип ENUM для ролей
CREATE TYPE user_role_enum AS ENUM (
    'CLIENT', 'ADMIN', 'TECH_SUPPORT', 'MANAGER',
    'SECURITY_OFFICER', 'SECURITY_SUPERVISOR',
    'SECURITY_TOP_SUPERVISOR', 'TOP_MANAGEMENT'
    );

-- Создаем таблицы...
CREATE TABLE accounts (
                          id                BIGSERIAL PRIMARY KEY,
                          balance           DECIMAL(19, 2) NOT NULL CONSTRAINT balance_non_negative CHECK (balance >= 0),
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

-- Таблица для связи с ролями (используем наш ENUM)
CREATE TABLE user_roles (
                            user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role        user_role_enum NOT NULL,
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

-- Добавляем расширения и индексы
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_users_username_trgm ON users USING GIN (LOWER(username) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_email_data_email ON email_data(email);
CREATE INDEX IF NOT EXISTS idx_phone_data_phone ON phone_data(phone);