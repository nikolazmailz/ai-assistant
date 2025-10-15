--liquibase formatted sql

--changeset system:remove-old-dialog-queue-columns
-- Удаляем устаревшие колонки из таблицы dialog_queue
ALTER TABLE dialog_queue
    DROP COLUMN IF EXISTS direction,
    DROP COLUMN IF EXISTS payload_type,
    DROP COLUMN IF EXISTS step_kind,
    DROP COLUMN IF EXISTS next_step_hint,
    DROP COLUMN IF EXISTS action_type;

--rollback ALTER TABLE dialog_queue
--    ADD COLUMN direction VARCHAR(32),
--    ADD COLUMN payload_type VARCHAR(64),
--    ADD COLUMN step_kind VARCHAR(64),
--    ADD COLUMN next_step_hint TEXT,
--    ADD COLUMN action_type VARCHAR(64);


--COMMENT ON COLUMN dialog_queue.direction IS 'Направление шага: inbound (входящее) или outbound (исходящее).';
--COMMENT ON COLUMN dialog_queue.payload_type IS 'Тип полезной нагрузки: text / voice / photo / document / unknown.';
--COMMENT ON COLUMN dialog_queue.step_kind IS 'Тип шага для роутера: request, response, replyToLLM.';
--COMMENT ON COLUMN dialog_queue.next_step_hint IS 'Подсказка о предполагаемом следующем шаге (для простого роутинга).';
--COMMENT ON COLUMN dialog_queue.action_type IS 'Тип действия: send_reply, fetch_calendar, run_sql, call_service и т.п.';
