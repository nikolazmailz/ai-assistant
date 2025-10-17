--liquibase formatted sql

-- changeset ai-assistant:003-create-conversation-queue
-- Создание таблицы dialog_queue — очередь шагов обработки диалога

-- 1. Enum для направления шага (входящее/исходящее)
--CREATE TYPE direction AS ENUM ('INBOUND', 'OUTBOUND');

-- 2. Enum для роли автора шага
CREATE TYPE role_type AS ENUM ('USER', 'ASSISTANT', 'SYSTEM');

-- 3. Enum для типа полезной нагрузки
--CREATE TYPE payload_type AS ENUM ('TEXT', 'VOICE', 'PHOTO', 'DOCUMENT', 'UNKNOWN');

-- Добавление статуса шага в очередь
CREATE TYPE queue_status AS ENUM ('NEW','PROCESSING','WAITING','READY','SUCCESS','ERROR');

--CREATE TYPE step_kind AS ENUM ('REQUEST','RESPONSE','REPLY_TO_AI');

-- 4. Таблица dialog_queue
CREATE TABLE dialog_queue (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- уникальный идентификатор шага в очереди
    user_id         BIGINT NOT NULL,                             -- Telegram ID пользователя
    chat_id         BIGINT NOT NULL,                             -- Telegram Chat ID (куда возвращать ответ)
    payload         TEXT,                                        -- содержимое шага (текст или JSON)
    status          TEXT NOT NULL DEFAULT 'NEW',         -- Статус шага: new (создан), processing (в работе), waiting (ожидание внешнего события), ready (готов к следующему этапу), success (завершён), error (ошибка).
    scheduled_at    TIMESTAMPTZ NOT NULL DEFAULT now(),          -- Плановое время выполнения шага (не брать задачу раньше этого момента).
    dialog_id       UUID,                                        -- логическая сессия, объединяющая серию шагов
    dialog_title    TEXT,                                        -- имя диалога
    source          TEXT NOT NULL DEFAULT 'telegram',            -- источник: telegram / web / api
--    direction       TEXT NOT NULL DEFAULT 'INBOUND',        -- направление шага: входящее/исходящее
    role            TEXT NOT NULL DEFAULT 'USER',           -- роль автора шага: user / assistant / system
--    payload_type    TEXT NOT NULL DEFAULT 'TEXT',        -- тип полезной нагрузки: text / voice / photo / ...
--    step_kind       TEXT NOT NULL DEFAULT 'REQUEST',        -- тип шага: request / response / replyToLLM
--    next_step_hint  TEXT,                                        -- подсказка о предполагаемом следующем шаге
--    action_type     TEXT,                                        -- тип действия: send_reply / fetch_calendar / ...
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),          -- когда шаг создан
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()           -- когда шаг обновлён
);

-- 5. Комментарии к таблице и колонкам
COMMENT ON TABLE dialog_queue IS 'Очередь шагов обработки диалога. Содержит состояния сообщений и действий, выполняемых по цепочке LLM.';

COMMENT ON COLUMN dialog_queue.id IS 'UUID — уникальный идентификатор шага в очереди.';
COMMENT ON COLUMN dialog_queue.user_id IS 'Telegram ID пользователя (владелец действия).';
COMMENT ON COLUMN dialog_queue.chat_id IS 'Telegram Chat ID (куда возвращать ответ/идентификатор канала).';
COMMENT ON COLUMN dialog_queue.payload IS 'Содержимое шага (текст, JSON для структурки, ссылочная мета для файлов).';
COMMENT ON COLUMN dialog_queue.status IS 'Статус шага: new (создан), processing (в работе), waiting (ожидание внешнего события), ready (готов к следующему этапу), success (завершён), error (ошибка).';
COMMENT ON COLUMN dialog_queue.scheduled_at IS 'Плановое время выполнения шага (не брать задачу раньше этого момента).';
COMMENT ON COLUMN dialog_queue.dialog_id IS 'UUID логической сессии, объединяющей серию шагов.';
COMMENT ON COLUMN dialog_queue.dialog_title  IS 'Короткое имя или заголовок диалога (для отображения в интерфейсе).';
COMMENT ON COLUMN dialog_queue.source IS 'Источник: telegram / web / api и т.п.';
--COMMENT ON COLUMN dialog_queue.direction IS 'Направление шага: inbound (входящее) или outbound (исходящее).';
COMMENT ON COLUMN dialog_queue.role IS 'Роль автора шага: user / assistant / system.';
--COMMENT ON COLUMN dialog_queue.payload_type IS 'Тип полезной нагрузки: text / voice / photo / document / unknown.';
--COMMENT ON COLUMN dialog_queue.step_kind IS 'Тип шага для роутера: request, response, replyToLLM.';
--COMMENT ON COLUMN dialog_queue.next_step_hint IS 'Подсказка о предполагаемом следующем шаге (для простого роутинга).';
--COMMENT ON COLUMN dialog_queue.action_type IS 'Тип действия: send_reply, fetch_calendar, run_sql, call_service и т.п.';
COMMENT ON COLUMN dialog_queue.created_at IS 'Метка времени создания шага.';
COMMENT ON COLUMN dialog_queue.updated_at IS 'Метка времени последнего обновления шага.';
