-- V1__Create_Security_Tables.sql
CREATE SCHEMA IF NOT EXISTS security;
SET search_path TO security;

-- Архив отозванных refresh-токенов
CREATE TABLE revoked_tokens_archive (
                                        token        VARCHAR(255) PRIMARY KEY,
                                        session_id   UUID NOT NULL UNIQUE,
                                        user_id      BIGINT NOT NULL,
                                        revoked_at   TIMESTAMPTZ NOT NULL,
                                        reason       VARCHAR(50) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_archive_user_id ON revoked_tokens_archive(user_id);


-- Заглушка для фингерпринтов
CREATE TABLE session_fingerprints (
                                      session_id          UUID PRIMARY KEY,
    -- ВАЖНО: Мы НЕ МОЖЕМ сделать FOREIGN KEY на другую базу.
    -- Поэтому здесь user_id - это просто числовое поле для связи.
                                      user_id             BIGINT NOT NULL,
                                      fingerprint_hash    VARCHAR(255),
                                      ip_address          VARCHAR(45),
                                      user_agent          TEXT,
                                      created_at          TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_fingerprints_user_id ON session_fingerprints(user_id);