
-- Создаем схему и переключаемся на нее
CREATE SCHEMA IF NOT EXISTS business;
SET search_path TO business;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Создаем кастомные типы ENUM, специфичные для этого домена
CREATE TYPE business.role_type_enum AS ENUM (
    'ROLE_CLIENT',
    'ROLE_ADMIN',
    'ROLE_TECH_SUPPORT',
    'ROLE_MANAGER',
    'ROLE_SENIOR_MANAGER',
    'ROLE_TOP_MANAGEMENT',
    'ROLE_SECURITY_OFFICER',
    'ROLE_SECURITY_SUPERVISOR',
    'ROLE_SECURITY_TOP_SUPERVISOR'
    );

CREATE TYPE business.account_type_enum AS ENUM (
    'PERSONAL_CHECKING',    -- Личный расчетный счет
    'PERSONAL_SAVINGS',     -- Личный накопительный
    'SHARED_FAMILY_BUDGET'  -- Общий семейный
    );

CREATE TYPE business.ownership_role_enum AS ENUM (
    'OWNER',      -- Владелец, полные права
    'CO_OWNER',   -- Со-владелец, полные права
    'EDITOR',     -- Может делать транзакции, но не управлять участниками
    'VIEWER'      -- Только просмотр
    );

-- ============================ ТАБЛИЦЫ ============================

-- Абстрактная база для всех пользователей
CREATE TABLE users (
                       id                     BIGSERIAL PRIMARY KEY,
                       user_type              VARCHAR(31) NOT NULL, -- Колонка-дискриминатор ('EMPLOYEE', 'CUSTOMER')
                       username               VARCHAR(255) NOT NULL UNIQUE,
                       date_of_birth          DATE NOT NULL,
                       last_login_timestamp   TIMESTAMPTZ,
                       enabled                BOOLEAN DEFAULT FALSE,
                       registration_timestamp TIMESTAMPTZ,
                       version                BIGINT DEFAULT 0 NOT NULL
);

-- Наследник: Сотрудники
CREATE TABLE employees (
                           id                      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                           employee_internal_id    VARCHAR(100) UNIQUE,
                           position                VARCHAR(255)
);

-- Наследник: Клиенты
CREATE TABLE customers (
                           id                      BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                           loyalty_status          VARCHAR(50) -- Пока строкой для простоты
);

-- Роли пользователей (простая связь с users)
CREATE TABLE user_roles (
                            user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role        role_type_enum NOT NULL,
                            PRIMARY KEY (user_id, role)
);

-- Контактные данные (тоже связаны с базовым user)
CREATE TABLE email_data ( /* ... id, email, user_id ... */ );
CREATE TABLE phone_data ( /* ... id, phone, user_id ... */ );
CREATE TABLE password_data ( /* ... id, phone, user_id ... */ );


-- Счета
CREATE TABLE accounts (
                          id                        BIGSERIAL PRIMARY KEY,
                          account_type              account_type_enum NOT NULL, -- PERSONAL_*, SHARED_*
                          account_name              VARCHAR(255) NOT NULL,
                          balance                   DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
                          status                    VARCHAR(50) NOT NULL, -- 'ACTIVE', 'FROZEN', 'CLOSED'
                          version           B       IGINT DEFAULT 0 NOT NULL,
                          non_withdrawable_balance  DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    -- НОВОЕ ПОЛЕ: Владелец. ЗАПОЛНЯЕТСЯ ТОЛЬКО ДЛЯ ПЕРСОНАЛЬНЫХ СЧЕТОВ.
                          owner_user_id     BIGINT REFERENCES users(id) ON DELETE SET NULL
);

-- Промежуточная таблица для связи M2M User <-> Account
CREATE TABLE account_memberships (
                                     id                BIGSERIAL PRIMARY KEY,
                                     user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                     account_id        BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                                     member_role       ownership_role_enum NOT NULL,
                                     UNIQUE (user_id, account_id)
);


-- === ВСЕ ИНДЕКСЫ ===
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_users_username_trgm ON users USING GIN (LOWER(username) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_accounts_owner ON accounts(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_memberships_user ON account_memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_memberships_account ON account_memberships(account_id);