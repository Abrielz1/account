-- V2__Create_Security_Domain.sql

-- Создаем "секьюрную" схему, если она еще не существует
CREATE SCHEMA IF NOT EXISTS security;
SET search_path TO security;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =================================================================================
--      РАЗДЕЛ 1: СОЗДАЕМ ВСЕ КАСТОМНЫЕ ТИПЫ (ENUMs) ЗАРАНЕЕ
--      (Используем "параноидальную" обертку DO...EXCEPTION для идемпотентности)
-- =================================================================================

-- Типы для Сессий
DO $$ BEGIN CREATE TYPE security.session_status_enum AS ENUM (
    'STATUS_ACTIVE', 'STATUS_COMPROMISED', 'STATUS_POTENTIAL_COMPROMISED',
    'STATUS_REVOKED_BY_USER', 'STATUS_REVOKED_BY_SYSTEM', 'STATUS_RED_ALERT'
    ); EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN CREATE TYPE security.revocation_reason_enum AS ENUM (
    'REASON_USER_LOGOUT', 'REASON_TOKEN_ROTATED', 'REASON_ADMIN_ACTION', 'REASON_EXPIRED',
    'REASON_PASSWORD_CHANGE', 'REASON_RED_ALERT', 'REASON_REVOKED_BY_USER_ON_ALL_DEVICES_SECURITY_ATTENTION'
    ); EXCEPTION WHEN duplicate_object THEN null; END $$;

-- Типы для Инцидентов
DO $$ BEGIN CREATE TYPE security.incident_type_enum AS ENUM (
    'REFRESH_TOKEN_REPLAY_ATTACK', 'UNTRUSTED_DEVICE_LOGIN_ATTEMPT', 'TOKEN_BINDING_FAILURE',
    'SUSPICIOUS_TOKEN_PAYLOAD', 'VERIFICATION_ATTEMPTS_EXCEEDED'
    ); EXCEPTION WHEN duplicate_object THEN null; END $$;

-- Типы для Банов
DO $$ BEGIN CREATE TYPE security.banned_entity_type_enum AS ENUM (
    'USER_ID', 'IP_ADDRESS', 'FINGERPRINT'
    ); EXCEPTION WHEN duplicate_object THEN null; END $$;

-- <<<--- ТВОИ "ПОТЕРЯННЫЕ" ENUM-ы для Админских Ордеров ---
DO $$ BEGIN CREATE TYPE security.order_type_enum AS ENUM (
    'VERIFY_REGISTRATION', 'FREEZE_ACCOUNT', 'UNFREEZE_ACCOUNT', 'CLOSE_ACCOUNT', 'BAN_USER', 'UNBAN_USER', 'REVOKE_ALL_SESSIONS'
    ); EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN CREATE TYPE security.order_basis_enum AS ENUM (
    'INTERNAL_REQUEST', 'COURT_ORDER', 'LAW_ENFORCEMENT_REQUEST', 'TOP_MANAGEMENT_DIRECTIVE'
    ); EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN CREATE TYPE security.registration_status_enum AS ENUM (
    'PENDING_EMAIL_VERIFICATION', 'PENDING_SECURITY_APPROVAL', 'APPROVED, REJECTED', 'FINALIZED, EXPIRED'
    ); EXCEPTION WHEN duplicate_object THEN null; END $$;

-- =================================================================================
--      РАЗДЕЛ 2: СОЗДАЕМ ВСЕ ТАБЛИЦЫ
--
-- =================================================================================

-- Таблица 0 -- Тут лежит зашифрованный юзер, 24 часа, если не подтвердил, ьаблица ставится в блок.

CREATE TABLE IF NOT EXISTS security.registration_requests(
                                                             id                      UUID PRIMARY KEY,
                                                             status                  security.registration_status_enum NOT NULL,

                                                             is_email_sent           BOOLEAN DEFAULT FALSE NOT NULL,
                                                             is_email_verified       BOOLEAN DEFAULT FALSE NOT NULL,
                                                             is_security_approved    BOOLEAN DEFAULT FALSE NOT NULL,
                                                             is_finalized            BOOLEAN DEFAULT FALSE NOT NULL, -- рега прошла подтверждение email
                                                             is_expired              BOOLEAN DEFAULT FALSE NOT NULL, -- 24 ссылка не кликнута, тогда в TRUE
                                                             is_rejected             BOOLEAN DEFAULT FALSE NOT NULL,
                                                             is_blocked              BOOLEAN DEFAULT FALSE NOT NULL, -- админ или сб поставила бан
                                                             is_deleted              BOOLEAN DEFAULT FALSE NOT NULL, -- если рега прошла успешно

    -- Пароль - ХЭШ те (bcrypt)
                                                             password_hash           TEXT NOT NULL,

    -- ХРАНИМ ХЭШ те
                                                             email_hash              TEXT NOT NULL UNIQUE, -- Хэш SHA-256 + соль
                                                             phone_hash              TEXT UNIQUE,
                                                             username                VARCHAR(255) NOT NULL UNIQUE,

    -- Токен для подтверждения по почте
                                                             verification_token      TEXT NOT NULL UNIQUE,
    -- дедлайн подтверждения 1 сутки!                                                         expires_at              TIMESTAMPTZ NOT NULL,

    -- Метаданные для аудита
                                                             created_at              TIMESTAMPTZ NOT NULL,
                                                             fingerprint_hash        TEXT,
                                                             ip_address              VARCHAR(45)


);
COMMENT ON TABLE security.registration_requests IS 'таблицка для потенциального клиента или сотрудника, хранит хеши, деействует 24 часа, потом блочится или удалятся софтово';

-- Таблица 1: Активные сессии
CREATE TABLE IF NOT EXISTS security.auth_sessions (
                                                      id                      UUID PRIMARY KEY,
                                                      user_id                 BIGINT NOT NULL,
                                                      refresh_token           TEXT NOT NULL UNIQUE,
                                                      access_token            TEXT NOT NULL UNIQUE,
                                                      status                  security.session_status_enum NOT NULL,
                                                      fingerprint             TEXT,
                                                      fingerprint_hash        TEXT, -- Сразу добавляем поле для HMAC-хэша
                                                      ip_address              VARCHAR(45),
                                                      user_agent              TEXT,
                                                      created_at              TIMESTAMPTZ NOT NULL,
                                                      expires_at              TIMESTAMPTZ NOT NULL,
                                                      revoked_at              TIMESTAMPTZ,
                                                      revocation_reason       security.revocation_reason_enum,
                                                      is_deleted              BOOLEAN DEFAULT FALSE NOT NULL,
                                                      version                 BIGINT
);
COMMENT ON TABLE security.auth_sessions IS 'Горячее хранилище активных сессий. Потенциальный кандидат на партиционирование.';

-- Таблица 2: Архив отозванных сессий
CREATE TABLE IF NOT EXISTS security.revoked_sessions_archive (
                                                                 session_id             UUID PRIMARY KEY,
                                                                 refresh_token          TEXT NOT NULL UNIQUE,
                                                                 access_token           TEXT NOT NULL UNIQUE,
                                                                 user_id                BIGINT NOT NULL,
                                                                 fingerprint            TEXT,
                                                                 ip_address             VARCHAR(45),
                                                                 user_agent             TEXT,
                                                                 created_at             TIMESTAMPTZ NOT NULL,
                                                                 expires_at             TIMESTAMPTZ NOT NULL,
                                                                 revoked_at             TIMESTAMPTZ NOT NULL,
                                                                 reason                 security.revocation_reason_enum NOT NULL,
                                                                 status                 security.session_status_enum NOT NULL
);
COMMENT ON TABLE security.revoked_sessions_archive IS 'Холодный, долгосрочный архив всех завершенных сессий для аудита.';

-- Таблица 3: Журнал Аудита (связан с сессией)
CREATE TABLE IF NOT EXISTS security.session_audit_log (
                                                          session_id        UUID PRIMARY KEY,
                                                          user_id           BIGINT NOT NULL,
                                                          fingerprint_hash  TEXT,
                                                          ip_address        VARCHAR(45),
                                                          user_agent        TEXT,
                                                          created_at        TIMESTAMPTZ NOT NULL,
                                                          is_compromised    BOOLEAN DEFAULT FALSE NOT NULL
);
COMMENT ON TABLE security.session_audit_log IS 'Журнал ключевых событий и статусов для каждой сессии.';

--ТАБЛИЦА-ПРОФИЛЬ 4: "ВЛАДЕЛЕЦ" ФИНГЕРПРИНТОВ
--      (Архитектура 1 Юзер -> 1 Профиль)
CREATE TABLE IF NOT EXISTS security.user_fingerprint_profiles (
    -- ID пользователя - это и есть ПЕРВИЧНЫЙ КЛЮЧ.
                                                                  user_id                 BIGINT PRIMARY KEY,

    -- Временные метки для аудита самого профиля
                                                                  created_at              TIMESTAMPTZ NOT NULL,
                                                                  last_updated_at         TIMESTAMPTZ NOT NULL,
                                                                  version                 BIGINT DEFAULT 0 NOT NULL
);
COMMENT ON TABLE security.user_fingerprint_profiles IS 'Родительская сущность-профиль для группировки доверенных устройств пользователя.';

--ТАБЛИЦА-СПРАВОЧНИК 5: "ДОВЕРЕННЫЕ УСТРОЙСТВА"
CREATE TABLE IF NOT EXISTS security.trusted_fingerprints (
    -- Суррогатный первичный ключ. Проще для JPA.
                                                             id                      BIGSERIAL PRIMARY KEY,

    -- Внешний ключ к "профилю" пользователя. Обеспечивает связь One-to-Many.
    -- ON DELETE CASCADE: Если удаляем профиль юзера - все его устройства тоже удаляются.
                                                             profile_user_id         BIGINT NOT NULL REFERENCES security.user_fingerprint_profiles(user_id) ON DELETE CASCADE,

    -- --- "ИДЕНТИФИКАТОРЫ" УСТРОЙСТВА ---
    -- Сам хэш фингерпринта. Должен быть уникальным в рамках ОДНОГО пользователя.
                                                             fingerprint             TEXT NOT NULL,

    -- --- "ЧЕЛОВЕЧЕСКАЯ" ИНФОРМАЦИЯ (для UI "Мои устройства") ---
    -- "Мой iPhone 15 Pro", "Рабочий ноутбук Dell"
                                                             device_name             VARCHAR(255),

    -- Последний известный User-Agent
                                                             user_agent              TEXT,

    -- Последний известный IP-адрес
                                                             ip_address              VARCHAR(45),

    -- --- МЕТАДАННЫЕ и СТАТУС ---
    -- Когда мы ВПЕРВЫЕ увидели это устройство
                                                             first_seen_at           TIMESTAMPTZ NOT NULL,

    -- Когда ПОСЛЕДНИЙ РАЗ подтверждали доверие к этому устройству
                                                             last_seen_at            TIMESTAMPTZ NOT NULL,

    -- Флаг доверия. Позволяет пользователю или СБ "отозвать" доверие.
                                                             is_trusted              BOOLEAN DEFAULT true NOT NULL,

    -- Версионирование для Optimistic Locking
                                                             version                 BIGINT DEFAULT 0 NOT NULL,

    -- --- КОНСТРЕЙНТЫ ---
    -- Гарантируем, что у ОДНОГО юзера (профиля) не может быть ДВУХ одинаковых фингерпринтов.
                                                             UNIQUE (profile_user_id, fingerprint)
);
COMMENT ON TABLE security.trusted_fingerprints IS 'Справочник доверенных устройств (фингерпринтов) для каждого пользователя.';

-- Таблица 6: "Таблица Хакера" - Таблица Инцидентов
CREATE TABLE IF NOT EXISTS security.security_incidents (
                                                           id                      BIGSERIAL PRIMARY KEY,
                                                           type                    security.incident_type_enum NOT NULL,
                                                           timestamp               TIMESTAMPTZ NOT NULL,
                                                           summary                 TEXT,
                                                           involved_user_id        BIGINT,
                                                           source_session_id       UUID,
                                                           source_ip_address       VARCHAR(45),
                                                           source_fingerprint_hash TEXT,
                                                           status                  VARCHAR(50) DEFAULT 'DETECTED' NOT NULL,
                                                           is_deleted              BOOLEAN DEFAULT FALSE NOT NULL
);
COMMENT ON TABLE security.security_incidents IS 'Журнал заведенных "Уголовных Дел"';

-- Таблица 7: "Таблица Хакера" - Журнал Инцидентов
CREATE TABLE IF NOT EXISTS security.security_incidents_log (
    -- ... (все поля, как и были: id, incident_type, user_id...)
                                                               id                      BIGSERIAL PRIMARY KEY,
                                                               incident_type           security.incident_type_enum NOT NULL,
                                                               user_id                 BIGINT,
                                                               incident_timestamp      TIMESTAMPTZ NOT NULL,
                                                               status                  VARCHAR(50) DEFAULT 'DETECTED' NOT NULL
);
COMMENT ON TABLE security.security_incidents_log IS 'Журнал зафиксированных атак и аномалий.';

-- Таблица 8: "Улик" (Деталей Инцидента)
CREATE TABLE IF NOT EXISTS security.security_incident_details (
                                                                  id                      BIGSERIAL PRIMARY KEY,

    -- Связь Many-to-One: МНОГО "улик" -> ОДИН "инцидент"
                                                                  incident_id             BIGSERIAL NOT NULL REFERENCES security.security_incidents_log(id) ON DELETE CASCADE,

    -- "Ключ" улики ("attack_token", "request_fingerprint"...)
                                                                  detail_key              VARCHAR(255) NOT NULL,

    -- "Значение" улики
                                                                  detail_value            TEXT,

                                                                  UNIQUE (incident_id, detail_key)
);
COMMENT ON TABLE security.security_incident_details IS ':Журнал собственно улик, те деталей инцидента';

-- Таблица 9: "Таблица Банов"
CREATE TABLE IF NOT EXISTS security.banned_entities (
                                                        id                      BIGSERIAL PRIMARY KEY,
                                                        entity_type             security.banned_entity_type_enum NOT NULL,
                                                        entity_value            TEXT NOT NULL,
                                                        ban_reason              TEXT NOT NULL,
                                                        banned_at               TIMESTAMPTZ NOT NULL,
                                                        expires_at              TIMESTAMPTZ,
                                                        banned_user_id          BIGINT,
                                                        banned_by_employee_id   BIGINT,
                                                        is_active               BOOLEAN DEFAULT true,

                                                        UNIQUE (entity_type, entity_value) WHERE is_active = true
);
COMMENT ON TABLE security.banned_entities IS 'Централизованная таблица блокировок (банхаммер).';

-- Таблица 10: "Таблица Деталей Банов"
CREATE TABLE IF NOT EXISTS security.ban_context_details (
                                                            id                      BIGSERIAL PRIMARY KEY,

    -- Связь с баном
                                                            ban_id                  BIGINT NOT NULL REFERENCES security.banned_entities(id) ON DELETE CASCADE,

    -- Контекстные "улики"
                                                            detail_key              VARCHAR(255) NOT NULL,
                                                            detail_value            TEXT,

                                                            UNIQUE (ban_id, detail_key)
);
COMMENT ON TABLE  security.ban_context_details IS 'Контекст Бана';

-- Таблица 11: Админские "Ордера"
CREATE TABLE IF NOT EXISTS security.admin_action_orders (
                                                            id                      UUID PRIMARY KEY,
                                                            order_type              security.order_type_enum NOT NULL,
                                                            basis_type              security.order_basis_enum NOT NULL,
                                                            basis_document_ref      TEXT,
                                                            target_user_id          BIGINT,
                                                            target_account_id       BIGINT,
                                                            status                  VARCHAR(50) NOT NULL,
                                                            created_by_employee_id  BIGINT NOT NULL,
                                                            created_at              TIMESTAMPTZ NOT NULL,
                                                            notes                   TEXT
);
COMMENT ON TABLE security.admin_action_orders IS 'Журнал административных приказов для выполнения СБ.';

-- ТАБЛИЦА 12:СВЯЗЬ!
CREATE TABLE IF NOT EXISTS security.incident_to_block_links (
                                                                incident_id         BIGINT NOT NULL REFERENCES security.security_incidents(id) ON DELETE CASCADE,
                                                                blocked_target_id   BIGINT NOT NULL REFERENCES security.blocked_targets(id) ON DELETE CASCADE,
                                                                PRIMARY KEY (incident_id, blocked_target_id)
);
COMMENT ON TABLE security.incident_to_block_links IS 'СВЯЗУЮЩИЙ, "МОСТ" между "Делами" и "Приговорами. Главная таблица банов."';

-- ТАБЛИЦА-СПИСОК 13: "ПРИГОВОРОВ": Blocked Targets
CREATE TABLE IF NOT EXISTS security.blocked_targets (
                                                        id                      BIGSERIAL PRIMARY KEY,
                                                        -- ТИП того, ЧТО, мы баним (IP, USER_ID, etc)
                                                        target_type             security.banned_entity_type_enum NOT NULL,
                                                        --ЗНАЧЕНИЕ того, ЧТО, мы баним (сам "1.2.3.4")
                                                        target_value            TEXT NOT NULL,
                                                        expires_at              TIMESTAMPTZ, -- null = бан навсегда
                                                        reason                  TEXT, -- "Replay Attack", "Manual Ban by Admin"
                                                        --ID "уголовного дела", которое спровоцировало этот бан. СЛАБАЯ, СВЯЗЬ.
                                                        triggering_incident_id  BIGINT REFERENCES security.security_incidents(id), -- Слабая, связь
                                                       affected_user_id        BIGINT, --КАКОЙ, ЮЗЕР был "целью" этой атаки? (может быть null)
                                                        affected_session_id     UUID,-- null = навсегда
                                                        is_deleted              BOOLEAN DEFAULT FALSE NOT NULL, -- софт удаление

                                                        UNIQUE (target_type, target_value) WHERE (is_deleted = false)   -- НЕЛЬЗЯ иметь ДВА АКТИВНЫХ бана на ОДНУ И ТУ ЖЕ ЦЕЛЬ
);
COMMENT ON TABLE security.incident_to_block_links IS 'СВЯЗУЮЩИЙ, "МОСТ" между "Делами" и "Приговорами"';

-- ТАБЛИЦА 14: "ВЕЩДОКОВ" (Чистая, и со ссылкой ТОЛЬКО на Инцидент)
CREATE TABLE IF NOT EXISTS security.incident_evidences (
                                                           id                      BIGSERIAL PRIMARY KEY,
                                                           incident_id             BIGINT NOT NULL REFERENCES security.security_incidents(id) ON DELETE CASCADE,
                                                           evidence_key            VARCHAR(255) NOT NULL,
                                                           evidence_value          TEXT,
                                                           is_deleted              BOOLEAN DEFAULT FALSE NOT NULL,
                                                           UNIQUE (incident_id, evidence_key)
);
COMMENT ON TABLE security.incident_evidences IS 'Конкретные "улики", приложенные к "Делу"';

-- =================================================================================
--      ТАБЛИЦА-СПРАВОЧНИК 15: "Холодный" Черный Список для ACCESS-токенов
-- =================================================================================
CREATE TABLE IF NOT EXISTS security.black_listed_access_tokens (
                                                                 token                   TEXT PRIMARY KEY,
                                                                 user_id                 BIGINT NOT NULL,
                                                                 session_id              UUID NOT NULL,
                                                                 created_at              TIMESTAMPTZ NOT NULL,
                                                                 original_expiry_date    TIMESTAMPTZ NOT NULL,
                                                                 revoked_at              TIMESTAMPTZ NOT NULL,
                                                                 reason                  security.revocation_reason_enum NOT NULL
);
COMMENT ON TABLE security.black_listed_access_tokens IS 'Персистентный, "холодный" черный список Access-токенов для быстрой проверки и аудита.';

-- =================================================================================
--      ТАБЛИЦА-СПРАВОЧНИК 16: "Холодный" Черный Список для REFRESH-токенов
-- =================================================================================
CREATE TABLE IF NOT EXISTS security.black_listed_refresh_tokens (
    -- Сам токен - это и есть первичный ключ. Мгновенный, сука, поиск.
                                                                  token                   TEXT PRIMARY KEY,

    -- ID пользователя. Критически важно для расследований СБ.
                                                                  user_id                 BIGINT NOT NULL,

    -- ID сессии. Позволяет связать бан с конкретной сессией.
                                                                  session_id              UUID NOT NULL,

    -- Время, когда этот токен был СОЗДАН (не отозван!)
                                                                  created_at              TIMESTAMPTZ NOT NULL,

    -- Время, когда этот токен ДОЛЖЕН БЫЛ ИСТЕЧЬ. Полезно для аналитики.
                                                                  original_expiry_date    TIMESTAMPTZ NOT NULL,

    -- Точное, бл***, время, когда токен попал в этот список.
                                                                  revoked_at              TIMESTAMPTZ NOT NULL,

    -- Причина, сука, отзыва. Чтобы отличать штатный выход от "Красной Тревоги".
                                                                  reason                  security.revocation_reason_enum NOT NULL
);

COMMENT ON TABLE security.black_listed_refresh_tokens IS 'Персистентный, "холодный" черный список Refresh-токенов. Служит "источником правды" для быстрой проверки и аудита скомпрометированных токенов.';

CREATE TABLE IF NOT EXISTS white_listed_access_tokens(
    token                   VARCHAR PRIMARY KEY UNIQUE NOT NULL,
    user_id                 BIGINT NOT NULL,
    sessionId               uuid NOT NULL,
    fingerprint_hash        VARCHAR NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL,
    original_expiry_date    TIMESTAMPTZ NOT NULL,
    revoked_at              TIMESTAMPTZ NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT false,
    reason                  security.revocation_reason_enum NOT NULL
);
COMMENT ON TABLE security.white_listed_access_tokens IS 'мат вьюха для акцесс токена, что б не тащить всю сессию';

CREATE TABLE IF NOT EXISTS white_listed_refresh_tokens(
   token                   VARCHAR PRIMARY KEY UNIQUE NOT NULL,
   user_id                 BIGINT NOT NULL,
   sessionId               uuid NOT NULL,
   fingerprint_hash        VARCHAR NOT NULL,
   created_at              TIMESTAMPTZ NOT NULL,
   original_expiry_date    TIMESTAMPTZ NOT NULL,
   revoked_at              TIMESTAMPTZ NOT NULL,
   is_active               BOOLEAN NOT NULL DEFAULT false,
   reason                  security.revocation_reason_enum NOT NULL
);
COMMENT ON TABLE security.white_listed_refresh_tokens IS 'мат вьюха для ркфреш токена, что б не тащить всю сессию';

COMMENT ON TABLE security.blocked_targets IS 'Централизованная,  "Книга Приговоров". Главная таблица банов.';
-- =================================================================================
--      РАЗДЕЛ 3: СОЗДАЕМ ВСЕ ИНДЕКСЫ В КОНЦЕ
--      (Для лучшей читаемости и управления)
-- =================================================================================
-- Индексы для auth_sessions
CREATE INDEX IF NOT EXISTS idx_auth_sessions_user_id ON security.auth_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_status ON security.auth_sessions(status);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_expires_at ON security.auth_sessions(expires_at);


-- Индексы для security_incidents_log
CREATE INDEX IF NOT EXISTS idx_incidents_user_id ON security.security_incidents_log(user_id);
CREATE INDEX IF NOT EXISTS idx_incidents_ip_address ON security.security_incidents_log(ip_address);

-- Индексы для banned_entities
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_banned_entity ON security.banned_entities(entity_type, entity_value) WHERE is_active = true;

-- Индексы для admin_action_orders
CREATE INDEX IF NOT EXISTS idx_admin_orders_user ON security.admin_action_orders(target_user_id);
CREATE INDEX IF NOT EXISTS idx_admin_orders_status ON security.admin_action_orders(status);


-- Индекс для быстрого поиска всех устройств одного юзера
CREATE INDEX IF NOT EXISTS idx_trusted_fingerprints_profile_id ON security.trusted_fingerprints(profile_user_id);
-- Индекс для быстрой проверки "А существует ли такой фингерпринт ВООБЩЕ?" (для Redis)
CREATE INDEX IF NOT EXISTS idx_trusted_fingerprints_fingerprint ON security.trusted_fingerprints(fingerprint);
CREATE INDEX IF NOT EXISTS idx_incident_details_incident_id ON security.security_incident_details(incident_id);
CREATE INDEX IF NOT EXISTS idx_ban_details_ban_id ON security.ban_context_details(ban_id);

-- 1. "Главный" индекс для быстрой, уникальной проверки (уже был, но важен)
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_banned_entity
    ON security.banned_entities(entity_type, entity_value) WHERE is_active = true;

-- 2. ИНДЕКС ДЛЯ АНАЛИТИКИ №1: "Кто кого забанил?"
--    Поиск по ID сотрудника, который выписал бан
CREATE INDEX IF NOT EXISTS idx_banned_entities_banned_by
    ON security.banned_entities(banned_by_employee_id);

-- 3. ИНДЕКС ДЛЯ АНАЛИТИКИ №2: "Все баны, связанные с юзером"
--    Поиск по ID пользователя, который был затронут баном
CREATE INDEX IF NOT EXISTS idx_banned_entities_banned_user
    ON security.banned_entities(banned_user_id);

-- 4. ИНДЕКС ДЛЯ АНАЛИТИКИ №3: "Статистика по причинам и типам"
--    СОСТАВНОЙ (COMPOSITE) ИНДЕКС для сложных запросов.
--    Позволяет эффективно фильтровать СРАЗУ по типу, причине и статусу.
CREATE INDEX IF NOT EXISTS idx_banned_entities_type_reason_status
    ON security.banned_entities(entity_type, ban_reason, is_active);
-- 5. индекс по типу и значению блокировки
CREATE INDEX IF NOT EXISTS idx_blocked_targets_type_value ON security.blocked_targets(target_type, target_value);

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_blocked_target
    ON security.blocked_targets(target_type, target_value)
    WHERE (is_deleted = false);

-- Индекс для возможного поиска по пользователю (для аналитики СБ)
CREATE INDEX IF NOT EXISTS idx_blacklisted_access_token_user_id ON security.black_listed_access_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_blacklisted_access_token_token ON security.black_listed_access_tokens(token);

CREATE INDEX IF NOT EXISTS idx_blacklisted_access_token_fingerprints_hash ON security.black_listed_access_tokens(fingerprint_hash);

-- Главный индекс (PK) у нас уже есть по самому токену.

-- Дополнительный индекс для аналитики СБ: "Покажи все токены, отозванные для этого юзера".
CREATE INDEX IF NOT EXISTS idx_blacklisted_refresh_token_user_id ON security.black_listed_refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_blacklisted_refresh_token_token ON security.black_listed_access_tokens(token);

CREATE INDEX IF NOT EXISTS idx_blacklisted_refresh_token_fingerprints_hash ON security.black_listed_access_tokens(fingerprint_hash);


-- Индекс по сессии, на всякий случай.
CREATE INDEX IF NOT EXISTS idx_blacklist_refresh_token_session_id ON security.black_listed_refresh_tokens(session_id);

CREATE INDEX IF NOT EXISTS idx_blacklist_access_token_session_id ON security.black_listed_access_tokens(session_id);


CREATE INDEX IF NOT EXISTS idx_white_listed_access_token_user_id ON security.white_listed_access_tokens(user_id);

CREATE INDEX IF NOT EXISTS idxwhite_listed_access_token_session_id ON security.white_listed_access_tokens(session_id);

CREATE INDEX IF NOT EXISTS idx_white_listed_access_token_is_active ON  security.white_listed_access_tokens(is_active);

CREATE INDEX IF NOT EXISTS idx_white_listed_access_token_session_id ON security.white_listed_access_tokens(session_id);


CREATE INDEX IF NOT EXISTS idx_white_listed_refresh_token_user_id ON security.white_listed_refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_white_listed_refresh_token_session_id ON security.white_listed_refresh_tokens(session_id);

CREATE INDEX IF NOT EXISTS idx_white_listed_refresh_token_is_active ON  security.white_listed_refresh_tokens(is_active);

CREATE INDEX IF NOT EXISTS idx_white_listed_refresh_token_session_id ON security.white_listed_refresh_tokens(session_id);


CREATE INDEX IF NOT EXISTS idx_reg_req_email_hash ON security.registration_requests(email_hash);
CREATE INDEX IF NOT EXISTS idx_reg_req_verification_token ON security.registration_requests(verification_token);