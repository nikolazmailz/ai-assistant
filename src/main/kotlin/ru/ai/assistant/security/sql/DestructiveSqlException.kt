package ru.ai.assistant.security.sql

class DestructiveSqlException(
    message: String,
    val indices: List<Int> = emptyList()
) : RuntimeException(message)
