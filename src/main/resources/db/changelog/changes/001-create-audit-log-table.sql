--liquibase formatted sql

-- changeset ai-assistant:002-create-audit-log-table
-- Создание таблицы аудита взаимодействий (лог запросов/ответов)

-- 1. Enum для типа полезной нагрузки
CREATE TYPE payload_type_log AS ENUM ('TEXT', 'VOICE', 'PHOTO', 'DOCUMENT', 'UNKNOWN');

-- 2. Таблица audit_log
CREATE TABLE audit_log (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- уникальный идентификатор записи
    user_id           BIGINT NOT NULL,                             -- Telegram ID пользователя
    chat_id           BIGINT NOT NULL,                             -- Telegram Chat ID (идентификатор чата)
    session_id        UUID,                                         -- идентификатор сессии/цепочки диалога
    source            TEXT NOT NULL DEFAULT 'telegram',             -- источник (telegram, web, api и т.п.)
    payload_type_log  TEXT NOT NULL DEFAULT 'TEXT',         -- тип полезной нагрузки
    payload           TEXT,                                         -- тело запроса или ответа (в зависимости от шага)
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),           -- время создания записи
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()            -- время последнего обновления
);

-- 3. Комментарии к таблице и колонкам
COMMENT ON TABLE audit_log IS 'Аудит взаимодействий — сохраняет все запросы и ответы по шагам, связанных с пользователем Telegram.';
COMMENT ON COLUMN audit_log.id IS 'UUID — уникальный идентификатор записи.';
COMMENT ON COLUMN audit_log.user_id IS 'Telegram ID пользователя (владелец действия).';
COMMENT ON COLUMN audit_log.chat_id IS 'Telegram Chat ID (куда или откуда поступило сообщение).';
COMMENT ON COLUMN audit_log.session_id IS 'UUID сессии (цепочка запросов/ответов в рамках одного диалога).';
COMMENT ON COLUMN audit_log.source IS 'Источник события: telegram, web, api и т.д.';
COMMENT ON COLUMN audit_log.payload_type_log IS 'Тип полезной нагрузки: text, voice, photo, document, unknown.';
COMMENT ON COLUMN audit_log.payload IS 'Тело запроса или ответа (JSON или текст).';
COMMENT ON COLUMN audit_log.created_at IS 'Метка времени создания записи.';
COMMENT ON COLUMN audit_log.updated_at IS 'Метка времени последнего обновления.';
