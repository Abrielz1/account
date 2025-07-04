-- V3__Create_Security_Tables.sql
CREATE SCHEMA IF NOT EXISTS security;
SET search_path TO security;

CREATE TABLE auth_sessions (
                               id                  UUID PRIMARY KEY,
                               user_id             BIGINT NOT NULL, -- FK мы не делаем для изоляции
                               refresh_token       VARCHAR(255) NOT NULL UNIQUE,
                               status              VARCHAR(50) NOT NULL,
                               fingerprint_hash    VARCHAR(255) NOT NULL,
                               ip_address          VARCHAR(45),
                               user_agent          TEXT,
                               created_at          TIMESTAMPTZ NOT NULL,
                               expires_at          TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_user_id ON auth_sessions(user_id);


CREATE TABLE revoked_tokens_archive (
                                        token        VARCHAR(255) PRIMARY KEY,
                                        session_id   UUID NOT NULL UNIQUE,
                                        user_id      BIGINT NOT NULL,
                                        revoked_at   TIMESTAMPTZ NOT NULL,
                                        reason       VARCHAR(50) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_archive_user_id ON revoked_tokens_archive(user_id);