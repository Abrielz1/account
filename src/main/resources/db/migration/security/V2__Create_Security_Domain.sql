-- V2__Create_Security_Domain.sql

CREATE SCHEMA IF NOT EXISTS security;
SET search_path TO security;

-- --- ТИПЫ ENUM ДЛЯ SECURITY ---
CREATE TYPE security.order_type_enum AS ENUM (
    'FREEZE_ACCOUNT',
    'UNFREEZE_ACCOUNT',
    'BAN_USER'
    );

-- --- НОВЫЕ ТИПЫ ДЛЯ ОРДЕРОВ ---
CREATE TYPE security.order_basis_enum AS ENUM (
    'INTERNAL_REQUEST',
    'COURT_ORDER',
    'LAW_ENFORCEMENT_REQUEST'
    );

CREATE TYPE security.order_basis_enum AS ENUM (
    'INTERNAL_REQUEST',         -- Запрос от другого сотрудника/отдела
    'COURT_ORDER',              -- Решение суда
    'LAW_ENFORCEMENT_REQUEST',    -- Запрос от "ФБР", etc
    'TOP_MANAGEMENT_DIRECTIVE'    -- Прямое указание руководства
    );

-- === ТИПЫ ENUM ===
CREATE TYPE security.session_status_enum AS ENUM ( 'ACTIVE',
                                                   'REVOKED_BY_USER',
                                                   'REVOKED_BY_SYSTEM',
                                                   'RED_ALERT' );

CREATE TYPE security.revocation_reason_enum AS ENUM ( 'USER_LOGOUT',
                                                      'TOKEN_ROTATED',
                                                      'ADMIN_ACTION',
                                                      'PASSWORD_CHANGE',
                                                      'COMPROMISED' );

-- === ТАБЛИЦА АКТИВНЫХ СЕССИЙ ===
CREATE TABLE auth_sessions (
                               id                 UUID PRIMARY KEY,
                               user_id            BIGINT NOT NULL,
                               refresh_token      TEXT NOT NULL UNIQUE,
                               access_token       TEXT NOT NULL UNIQUE,
                               status             session_status_enum NOT NULL,
                               fingerprint        TEXT,
                               ip_address         VARCHAR(45),
                               user_agent         TEXT,
                               created_at         TIMESTAMPTZ NOT NULL,
                               expires_at         TIMESTAMPTZ NOT NULL,
                               revoked_at          TIMESTAMPTZ NOT NULL,
                               reason                   revocation_reason_enum NOT NULL
);

-- === ТАБЛИЦА-АРХИВ ОТОЗВАННЫХ ТОКЕНОВ ===
CREATE TABLE revoked_tokens_archive (
                                        refresh_token_value      TEXT PRIMARY KEY, -- Renamed from 'token' for clarity
                                        accesses_token_value     TEXT,
                                        session_id               UUID NOT NULL,
                                        user_id                  BIGINT NOT NULL,
                                        fingerprint              TEXT,
                                        revoked_at               TIMESTAMPTZ NOT NULL,
                                        session_status           session_status_enum NOT NULL,
                                        reason                   revocation_reason_enum NOT NULL
);

-- === ТАБЛИЦА АУДИТА И ФИНГЕРПРИНТОВ ===
CREATE TABLE session_audit_log (
                                   session_id        UUID PRIMARY KEY,
                                   user_id           BIGINT NOT NULL,
                                   fingerprint       TEXT,
                                   ip_address        VARCHAR(45),
                                   user_agent        TEXT,
                                   created_at        TIMESTAMPTZ NOT NULL,
                                   is_compromised    BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE admin_action_orders (
                                     id                      UUID PRIMARY KEY,
                                     order_type              order_type_enum NOT NULL,
                                     basis_type              order_basis_enum NOT NULL,
                                     basis_document_ref      TEXT,
                                     target_user_id          BIGINT,
                                     target_account_id       BIGINT,
                                     status                  VARCHAR(50) NOT NULL, -- 'PENDING', 'APPROVED', 'REJECTED', 'EXECUTED'
                                     created_by_employee_id  BIGINT NOT NULL,
                                     created_at              TIMESTAMPTZ NOT NULL,
                                     notes                   TEXT
);

CREATE TABLE session_audit_log (
                                   session_id          UUID PRIMARY KEY,
                                   user_id             BIGINT NOT NULL,
                                   fingerprint         TEXT,
                                   ip_address          VARCHAR(45),
                                   user_agent          TEXT,
                                   created_at          TIMESTAMPTZ NOT NULL,
                                   is_compromised      BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_orders_user ON admin_action_orders(target_user_id);
CREATE INDEX IF NOT EXISTS idx_admin_orders_status ON admin_action_orders(status);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_user_id ON auth_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_expires_at ON auth_sessions(expires_at); -- Для будущей очистки
CREATE INDEX IF NOT EXISTS idx_revoked_tokens_session_id ON revoked_tokens_archive(session_id);
CREATE INDEX IF NOT EXISTS idx_session_audit_log_user_id ON session_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_session_audit_log_created_at ON session_audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_session_fingerprint ON auth_sessions(fingerprint);






