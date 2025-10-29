--liquibase formatted sql

--changeset ai-assistant:010-rename-content-to-prompt
-- Проверяем, есть ли колонка content, и переименовываем в prompt

ALTER TABLE system_prompt RENAME COLUMN content TO prompt;
