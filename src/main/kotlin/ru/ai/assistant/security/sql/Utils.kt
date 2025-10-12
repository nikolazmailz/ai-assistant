package ru.ai.assistant.security.sql

object Utils {}

//fun isDestructiveSql(text: String): Boolean {
//    val s = sanitizeSql(text)
//    return dangerous.any { it.containsMatchIn(s) }
//}
//
//private fun sanitizeSql(input: String): String {
//    var s = input
//    s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
//    s = Regex("(?m)--.*?$").replace(s, " ")
//    s = Regex("'([^']|'')*'").replace(s, " ")
//    s = Regex("\"([^\"]|\"\")*\"").replace(s, " ")
//    return s
//}
//
//private val dangerous = listOf(
//    Regex("""\bDROP\s+(DATABASE|SCHEMA|TABLE|VIEW|MATERIALIZED\s+VIEW|INDEX|SEQUENCE|TYPE|TRIGGER|FUNCTION|PROCEDURE|EXTENSION)\b""", RegexOption.IGNORE_CASE),
//    Regex("""\bTRUNCATE\b(\s+TABLE\b)?""", RegexOption.IGNORE_CASE),
//    Regex("""\bDELETE\s+FROM\b""", RegexOption.IGNORE_CASE),
//    Regex("""\bALTER\s+TABLE\b[\s\S]*?\bDROP\s+(COLUMN|CONSTRAINT)\b""", RegexOption.IGNORE_CASE)
//)


//    Mode.STRICT
