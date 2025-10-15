--liquibase formatted sql

--changeset system:add-dialog-title-to-dialog-queue
-- Добавляем колонку dialog_title в таблицу dialog_queue
ALTER TABLE dialog_queue
    ADD COLUMN IF NOT EXISTS dialog_title TEXT;

COMMENT ON COLUMN dialog_queue.dialog_title IS 'Короткое имя или заголовок диалога (для отображения в интерфейсе).';

--rollback
ALTER TABLE dialog_queue
    DROP COLUMN IF EXISTS dialog_title;
