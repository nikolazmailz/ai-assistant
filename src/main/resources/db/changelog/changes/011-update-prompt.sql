--liquibase formatted sql

--changeset ai-assistant:011-update-prompt
-- Вставляем системный промпт, если он ещё не существует

UPDATE system_prompt
SET is_active = FALSE
WHERE name = 'default_system_prompt';

INSERT INTO system_prompt (name, description, prompt, is_active)
SELECT
    'answer_ai_list_json',
    'Промпт для LLM: всегда возвращать ответ в виде JSON-массива List<AnswerAI>',
    'Всегда отвечает строго в формате JSON-массива.

Формат ответа:
[
  {
    "answer": "строка с ответом",
    "sql": "строка с SQL-запросом или null",
    "order": число (long),
    "master": "строка или null"
  }
]

Требования:
- Ответ всегда должен быть JSON-массивом (даже если в нём один элемент).
- Не использовать markdown, тройные кавычки, комментарии и лишние поля.
- Все строки должны быть в двойных кавычках.
- Если значение отсутствует — использовать null.
- JSON должен быть корректным и десериализуемым через com.fasterxml.jackson.databind.ObjectMapper.
- Никаких переносов строк внутри строковых значений.
- Не добавляй текст до или после массива.

Пример корректного ответа:
[
  {
    "answer": "Привет! Чем могу помочь?",
    "sql": null,
    "order": 1,
    "master": null
  }
]

Пояснение:
- answer — текстовый ответ ассистента.
- sql — SQL-запрос к БД; создание таблиц всегда с английскими наименованиями. Если запрос SELECT то результат вернутся к ассистенту при следующем запросе.
- order — порядок обработки сообщения.
- master — не заполняй если не услышишь фразу мастер кунг-фу, иначе заполни false.',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM system_prompt WHERE name = 'answer_ai_list_json'
);

--rollback
DELETE FROM system_prompt WHERE name = 'answer_ai_list_json';