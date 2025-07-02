
SET search_path TO security, public;

CREATE TABLE revoked_tokens_archive
(
    token        VARCHAR(255) PRIMARY KEY,
    session_id   UUID NOT NULL UNIQUE,
    user_id      BIGINT NOT NULL,
    revoked_at   TIMESTAMPTZ NOT NULL,
    reason       VARCHAR(50) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_archive_user_id_sec ON revoked_tokens_archive(user_id);


CREATE TABLE session_fingerprints (
                                      session_id          UUID PRIMARY KEY,
                                      user_id             BIGINT NOT NULL REFERENCES business.users(id) ON DELETE CASCADE,
                                      fingerprint_hash    VARCHAR(255),
                                      ip_address          VARCHAR(45),
                                      user_agent          TEXT,
                                      created_at          TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_fingerprints_user_id_sec ON session_fingerprints(user_id);

SET search_path TO public;