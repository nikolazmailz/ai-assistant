--liquibase formatted sql

-- changeset ai-assistant:005-create-dialog

CREATE TABLE dialog_metainfo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Уникальный идентификатор диалога
    title VARCHAR(100) NOT NULL,                   -- Короткое название (например, "Голосовое управление")
    description TEXT,                              -- Подробное описание диалога или контекста
    user_id         BIGINT NOT NULL,               -- Telegram ID пользователя
    is_active BOOLEAN NOT NULL DEFAULT TRUE,       -- Флаг активности диалога
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), -- Дата создания
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()  -- Дата последнего обновления
);

COMMENT ON TABLE dialog_metainfo IS 'Хранит метаданные диалогов Telegram-ассистента (идентификатор, активность, описание)';
COMMENT ON COLUMN dialog_metainfo.id IS 'Уникальный идентификатор диалога (UUID)';
COMMENT ON COLUMN dialog_metainfo.title IS 'Короткое название диалога';
COMMENT ON COLUMN dialog_metainfo.description IS 'Описание или контекст диалога';
COMMENT ON COLUMN dialog_metainfo.user_id IS 'Telegram ID пользователя (владелец действия).';
COMMENT ON COLUMN dialog_metainfo.is_active IS 'Флаг активности диалога';
COMMENT ON COLUMN dialog_metainfo.created_at IS 'Дата и время создания диалога';
COMMENT ON COLUMN dialog_metainfo.updated_at IS 'Дата и время последнего обновления';

