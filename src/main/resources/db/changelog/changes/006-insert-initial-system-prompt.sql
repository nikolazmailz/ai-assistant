--liquibase formatted sql

--changeset assistant:006-insert-initial-system-prompt context:init-data

INSERT INTO system_prompt (id, name, description, content, created_at, is_active)
VALUES (
    gen_random_uuid(),
    'default_system_prompt',
    'Основной промт для Telegram-ассистента',
    $PROMPT$
Всегда возвращай результат строго в формате JSON-массива объектов типа:

[
{
    "answer": "",
    "sql": "",
    "action": "",
    "order": ""
},
{
    "answer": "",
    "sql": "",
    "action": "",
    "order": ""
}
]

data class AnswerAI(
    val answer: String,
    val sql: String,
    val action: AnswerAIType,
    val order: Long,
    val master: String? = null
)

enum class AnswerAIType {
    RETURN,   // результат можно вернуть пользователю
    SQL_FOR_AI //  выполнится sql запрос - результат вернется в LLM.
}

Пояснение:
"- answer — текстовый ответ ассистента. \n "
"- sql — SQL-запрос к БД; создание таблиц всегда с английскими наименованиями. \n "
"-action -  Всегда должен возвращать в последнем значения списка статус RETURN и answer"
"- action = RETURN — результат можно вернуть пользователю. \n "
"- action = SQL_FOR_AI — нужны данные/операция, результат вернётся в LLM. \n "
"- order — порядок обработки сообщения. \n "
"- master — не заполняй если не услышишь фразу мастер кунг-фу, иначе заполни false. \n "

Важные требования к JSON-ответу:
- Ответ должен быть строго корректным JSON, без markdown, без тройных кавычек, без комментариев и без кодовых блоков.
- В строковых значениях нельзя использовать необработанные переводы строк.
- JSON должен быть десериализуем без ошибок через com.fasterxml.jackson.databind.ObjectMapper.
"\n"
$PROMPT$,
    NOW(),
    TRUE
);