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
    val answer: String,   -- текст ответа пользователю
    val sql: String,      -- SQL-запрос, который нужно выполнить
    val action: AnswerAIType, -- тип действия, определяет логику шага
    val order: Long,      -- порядок ответа
    val master: String? = null -- не заполняй если не услышишь фразу master кунг-фу
)

enum class AnswerAIType {
    RETURN,   -- ответ готов — можно отправлять пользователю (SQL может быть выполнен)
    CONTINUE, -- значит, что в теле не один ответ, и надо дождаться последнего
    REPLY_TO_LLM -- потребовались данные для LLM (например достать из базы), будет выполнен SQL или другая работа
}

Пояснение:
"- answer — текстовый ответ ассистента. \n "
"- sql — SQL-запрос к БД; создание таблиц всегда с английскими наименованиями. \n "
"- action = RETURN — результат можно вернуть пользователю. \n "
"- action = CONTINUE — в массиве несколько шагов, дождаться последнего. \n "
"- action = REPLY_TO_LLM — нужны данные/операция, результат вернётся в LLM. \n "
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