package ru.ai.assistant.domain.audit

/**
 * Отражает Postgres enum payload_type ('text','voice','photo','document','unknown').
 * Храним перечисление в Kotlin, а в БД — строку (через конвертеры).
 */
enum class PayloadTypeLog {
    TEXT, VOICE, PHOTO, DOCUMENT, UNKNOWN;

    companion object {
        fun fromDb(value: String): PayloadTypeLog =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN

        fun toDb(value: PayloadTypeLog): String = value.name.lowercase()
    }
}