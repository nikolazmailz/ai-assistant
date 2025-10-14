package ru.ai.assistant.application.security.sql

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import ru.ai.assistant.application.dto.AnswerAI
import ru.ai.assistant.application.security.sql.dto.Mode

@Component
class AnswerAiGuard {

    private val log = KotlinLogging.logger {}

    /** Бросает DestructiveSqlException, если объект опасен в своём режиме master. */
    fun sqlValidate(ai: AnswerAI): Boolean {
        val mode = parseMode(ai.master)
        log.debug { "AnswerAiGuard $mode" }
        return if (sqlDanger(ai.sql!!, mode)) {
            false
//            throw DestructiveSqlException(
//                message = "Blocked by SQL guard: master='${ai.master}', order=${ai.order}"
//            )
        } else {
            true
        }
    }

    /** Валидирует список; собирает индексы опасных элементов. */
    suspend fun validateAll(list: List<AnswerAI>): Boolean {
        val bad = list.mapIndexedNotNull { idx, ai ->
            val mode = parseMode(ai.master)
            if (sqlDanger(ai.sql!!, mode)) idx else null
        }
        return if (bad.isNotEmpty()) {
//            throw DestructiveSqlException(
//                message = "Blocked by SQL guard in list: indices=${bad}",
//                indices = bad
//            )
            false
        } else {
            true
        }
    }

    private fun parseMode(master: String?): Mode =
        when (master?.lowercase()) {
            "off", "none", "disable", "disabled", "false" -> Mode.OFF
            "relaxed", "soft" -> Mode.RELAXED
            else -> Mode.STRICT
        }

    private fun sqlDanger(sql: String, mode: Mode): Boolean {
        val s = sanitizeSql(sql)
        return when (mode) {
            Mode.OFF -> false
            Mode.RELAXED -> listOf(DROP_ANY, TRUNCATE, ALTER_DROP).any { it.containsMatchIn(s) }
            Mode.STRICT -> listOf(DROP_ANY, TRUNCATE, ALTER_DROP, DELETE_FROM).any { it.containsMatchIn(s) }
        }
    }

    private fun sanitizeSql(input: String): String {
        var s = input
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = Regex("(?m)--.*?$").replace(s, " ")
        s = Regex("'([^']|'')*'").replace(s, " ")
        s = Regex("\"([^\"]|\"\")*\"").replace(s, " ")
        return s
    }


    private val DROP_ANY = Regex("""\bDROP\s+(DATABASE|SCHEMA|TABLE|VIEW|MATERIALIZED\s+VIEW|INDEX|SEQUENCE|TYPE|TRIGGER|FUNCTION|PROCEDURE|EXTENSION)\b""", RegexOption.IGNORE_CASE)
    private val TRUNCATE = Regex("""\bTRUNCATE\b(\s+TABLE\b)?""", RegexOption.IGNORE_CASE)
    private val ALTER_DROP = Regex("""\bALTER\s+TABLE\b[\s\S]*?\bDROP\s+(COLUMN|CONSTRAINT)\b""", RegexOption.IGNORE_CASE)
    private val DELETE_FROM = Regex("""\bDELETE\s+FROM\b""", RegexOption.IGNORE_CASE)
    private val UPDATE_NO_WHERE = Regex("""\bUPDATE\b(?![\s\S]*\bWHERE\b)""", RegexOption.IGNORE_CASE)

}
