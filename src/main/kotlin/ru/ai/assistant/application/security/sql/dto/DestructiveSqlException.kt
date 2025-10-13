package ru.ai.assistant.application.security.sql.dto

class DestructiveSqlException(
    message: String,
    val indices: List<Int> = emptyList()
) : RuntimeException(message)