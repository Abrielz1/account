CREATE SCHEMA IF NOT EXISTS business;
SET search_path TO business;

CREATE TABLE accounts (
                          id                BIGSERIAL PRIMARY KEY,
                          balance           DECIMAL(19, 2) NOT NULL,
                          initial_balance   DECIMAL(19, 2) NOT NULL,
                          version           BIGINT DEFAULT 0 NOT NULL,
                          CONSTRAINT balance_non_negative CHECK (balance >= 0)
);
COMMENT ON TABLE accounts IS 'Счета пользователей с балансами';

CREATE TABLE users (
                       id               BIGSERIAL PRIMARY KEY,
                       username         VARCHAR(255) NOT NULL UNIQUE,
                       password         VARCHAR(255) NOT NULL,
                       date_of_birth    DATE NOT NULL,
                       account_id       BIGINT NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
                       version          BIGINT DEFAULT 0 NOT NULL
);
COMMENT ON TABLE users IS 'Основные данные пользователей';


-- Справочник ролей
CREATE TABLE roles (
                       id          SMALLINT PRIMARY KEY,
                       name        VARCHAR(50) NOT NULL UNIQUE
);
COMMENT ON TABLE roles IS 'Справочник ролей в системе';

CREATE TABLE user_roles (
                            id          BIGSERIAL PRIMARY KEY,
                            user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id     SMALLINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            UNIQUE (user_id, role_id)
);
COMMENT ON TABLE user_roles IS 'Связь между пользователями и их ролями';

CREATE TABLE email_data (
                            id                BIGSERIAL PRIMARY KEY,
                            email             VARCHAR(255) NOT NULL UNIQUE,
                            user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            version           BIGINT DEFAULT 0 NOT NULL
);
COMMENT ON TABLE email_data IS 'Email-адреса пользователей';


CREATE TABLE phone_data (
                            id                 BIGSERIAL PRIMARY KEY,
                            phone              VARCHAR(20) NOT NULL UNIQUE,
                            user_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            version            BIGINT DEFAULT 0 NOT NULL
);
COMMENT ON TABLE phone_data IS 'Телефонные номера пользователей';

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_users_username_trgm ON users USING GIN (LOWER(username) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_email_data_email ON email_data(email);
CREATE INDEX IF NOT EXISTS idx_phone_data_phone ON phone_data(phone);