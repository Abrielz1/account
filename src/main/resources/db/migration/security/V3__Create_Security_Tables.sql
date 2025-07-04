-- V3__Create_Security_Domain.sql

CREATE SCHEMA IF NOT EXISTS security;
SET search_path TO security;

-- --- Типы, специфичные для домена security ---
CREATE TYPE security.session_status_enum AS ENUM (
    'ACTIVE',
    'REVOKED_BY_USER',
    'REVOKED_BY_SYSTEM',
    'RED_ALERT'
    );

CREATE TYPE security.revocation_reason_enum AS ENUM (
    'USER_LOGOUT',
    'TOKEN_ROTATED',
    'ADMIN_ACTION',
    'PASSWORD_CHANGE',
    'RED_ALERT'
    );


-- --- Таблицы домена security ---
CREATE TABLE auth_sessions (
                               id                  UUID PRIMARY KEY,
                               user_id             BIGINT NOT NULL, -- Логическая связь
                               refresh_token       VARCHAR(255) NOT NULL UNIQUE,
                               status              session_status_enum NOT NULL,
                               fingerprint_hash    VARCHAR(255),
                               ip_address          VARCHAR(45),
                               user_agent          TEXT,
                               created_at          TIMESTAMPTZ NOT NULL,
                               expires_at          TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_user_id ON auth_sessions(user_id);


CREATE TABLE revoked_tokens_archive (
                                        token        VARCHAR(255) PRIMARY KEY,
                                        session_id   UUID NOT NULL, -- Не UNIQUE, т.к. из одной сессии может быть несколько ротаций
                                        user_id      BIGINT NOT NULL,
                                        revoked_at   TIMESTAMPTZ NOT NULL,
                                        reason       revocation_reason_enum NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_archive_user_id ON revoked_tokens_archive(user_id);