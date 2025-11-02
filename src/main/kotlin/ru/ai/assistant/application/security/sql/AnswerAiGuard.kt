package ru.ai.assistant.application.security.sql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import ru.ai.assistant.application.dto.AnswerAI
import ru.ai.assistant.application.security.sql.dto.Mode
import ru.ai.assistant.utils.JacksonObjectMapper

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
//        val s = sanitizeSql(sql)
//        return when (mode) {
//            Mode.OFF -> false
//            Mode.RELAXED -> listOf(DROP_ANY, TRUNCATE, ALTER_DROP).any { it.containsMatchIn(s) }
//            Mode.STRICT -> listOf(DROP_ANY, TRUNCATE, ALTER_DROP, DELETE_FROM).any { it.containsMatchIn(s) }
//        }

//        val s = sanitizeSql(sql)
        val rules = when (mode) {
            Mode.OFF -> emptyList()
            Mode.RELAXED -> listOf("DROP_ANY" to DROP_ANY, "TRUNCATE" to TRUNCATE, "ALTER_DROP" to ALTER_DROP)
            Mode.STRICT -> listOf("DROP_ANY" to DROP_ANY, "TRUNCATE" to TRUNCATE, "ALTER_DROP" to ALTER_DROP, "DELETE_FROM" to DELETE_FROM)
        }

        for ((name, rx) in rules) {
            if (rx.containsMatchIn(sql)) {
                log.warn { "SQL guard hit: rule=$name, mode=$mode, sanitized='${sql.trim()}'" }
                return true
            }
        }
        log.debug { "SQL guard passed: mode=$mode, sanitized='${sql.trim()}'" }
        return false
    }

    private fun sanitizeSql(input: String): String {
        var s = input
        // 1) Удаляем многострочные комментарии
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        log.debug { "sanitizeSql result s1 $s" }

        // 2) Удаляем однострочные комментарии
        s = Regex("(?m)--.*?$").replace(s, " ")
        log.debug { "sanitizeSql result s2 $s" }

        // 3) Заменяем строковые литералы на <str>
        s = Regex("'([^']|'')*'").replace(s, " <str> ")
        log.debug { "sanitizeSql result s3 $s" }

        // 4) Заменяем двойные кавычки (quoted identifiers) на <ident>
        s = Regex("\"([^\"]|\"\")*\"").replace(s, " <ident> ")
        log.debug { "sanitizeSql result s4 $s" }

        // (опц.) “умные” кавычки и бэктики:
        s = Regex("‘([^‘]|‘‘)*’").replace(s, " <str> ")
        log.debug { "sanitizeSql result s5 $s" }
        s = Regex("`([^`]|``)*`").replace(s, " <ident> ")
        log.debug { "sanitizeSql result s6 $s" }

        // 5) Схлопываем пробелы
        s = Regex("\\s+").replace(s, " ").trim()
        log.debug { "sanitizeSql result s7 $s" }
        return s
    }


    private val DROP_ANY = Regex("""\bDROP\s+(DATABASE|SCHEMA|TABLE|VIEW|MATERIALIZED\s+VIEW|INDEX|SEQUENCE|TYPE|TRIGGER|FUNCTION|PROCEDURE|EXTENSION)\b""", RegexOption.IGNORE_CASE)
    private val TRUNCATE = Regex("""\bTRUNCATE\b(\s+TABLE\b)?""", RegexOption.IGNORE_CASE)
    private val ALTER_DROP = Regex("""\bALTER\s+TABLE\b[\s\S]*?\bDROP\s+(COLUMN|CONSTRAINT)\b""", RegexOption.IGNORE_CASE)
    private val DELETE_FROM = Regex("""\bDELETE\s+FROM\b""", RegexOption.IGNORE_CASE)
//    private val UPDATE_NO_WHERE = Regex("""\bUPDATE\b(?![\s\S]*\bWHERE\b)""", RegexOption.IGNORE_CASE)


    fun parseAiContent(responseAi: String): List<AnswerAI> {
        // срезаем ```json ... ``` или ``` ... ```
        val cleaned = Regex("^```(?:json)?\\s*|\\s*```$", RegexOption.MULTILINE)
            .replace(responseAi.trim(), "")
            .trim()

        val mapper = JacksonObjectMapper.instance.findAndRegisterModules()
        return mapper.readValue(cleaned)
    }
}
