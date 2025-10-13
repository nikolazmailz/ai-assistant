--liquibase formatted sql

-- changeset ai-assistant:004-create-system-prompt
-- Таблица системных предпромптов (system_prompt)

-- (опц.) функция gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE system_prompt (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- уникальный идентификатор предпромпта
    name        TEXT NOT NULL,                               -- человекочитаемое имя/ключ предпромпта
    description TEXT,                                        -- описание назначения/контекста
    content     TEXT NOT NULL,                               -- сам системный промпт (текст)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),          -- когда создан
    is_active   BOOLEAN NOT NULL DEFAULT FALSE                -- активен ли предпромпт для выбора по умолчанию
);

COMMENT ON TABLE system_prompt IS 'Системные предпромпты для инициализации контекста диалога.';
COMMENT ON COLUMN system_prompt.id IS 'UUID — уникальный идентификатор предпромпта.';
COMMENT ON COLUMN system_prompt.name IS 'Имя/ключ предпромпта (для выбора и ссылок).';
COMMENT ON COLUMN system_prompt.description IS 'Описание: для чего и где используется промпт.';
COMMENT ON COLUMN system_prompt.content IS 'Содержимое системного промпта (текст).';
COMMENT ON COLUMN system_prompt.created_at IS 'Метка времени создания записи.';
COMMENT ON COLUMN system_prompt.is_active IS 'Флаг активности предпромпта.';
