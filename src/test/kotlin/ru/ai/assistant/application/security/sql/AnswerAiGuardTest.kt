package ru.ai.assistant.application.security.sql

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import ru.ai.assistant.application.dto.AnswerAI
import ru.ai.assistant.application.dto.AnswerAIType

class AnswerAiGuardTest : StringSpec({

    val guard = AnswerAiGuard()

    fun ai(sql: String, master: String? = null) = AnswerAI(
        answer = "",
        sql = sql,
        action = AnswerAIType.RETURN,
        order = 1,
        master = master
    )

    "STRICT: allows single UPDATE with literals" {
        val sql = """
            UPDATE public.dialog_metainfo
            SET title = 'Dialog Service Handle Message'
            WHERE id = '349ed322-4d4f-4fb3-9c1b-be814316a0df'
        """.trimIndent()
        guard.sqlValidate(ai(sql, master = null)).shouldBeTrue()
    }

    "Error UseCase" {
        val sql = """
            UPDATE dialog_metainfo SET title = 'Неопознанный диалог' WHERE id = '650c4e0f-4875-45d1-ae94-1cdc315e8ca0';
        """.trimIndent()


        guard.sqlValidate(ai(sql, master = null)).shouldBeTrue()

    }



    "Error UseCase 2" {
        val answer = "[\n" +
                "    {\n" +
                "        \"answer\": \"Диалог не имеет заголовка. Предлагаю установить название: 'Общий разговор'.\",\n" +
                "        \"sql\": \"UPDATE dialog_metainfo SET title = 'Общий разговор' WHERE user_id = 167548610 AND dialog_id = '650c4e0f-4875-45d1-ae94-1cdc315e8ca0';\",\n" +
                "        \"action\": \"RETURN\"\n" +
                "    }\n" +
                "]"


       val aiAnswer = guard.parseAiContent(answer)

        print(aiAnswer)

        guard.sqlValidate(ai(aiAnswer.first().sql!!, master = null)).shouldBeTrue()
    }

})