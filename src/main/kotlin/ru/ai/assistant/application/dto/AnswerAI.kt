package ru.ai.assistant.application.dto

data class AnswerAI(
    val answer: String,
    val sql: String? = null,
    val action: AnswerAIType,
    val order: Long,
    val master: String? = null
) {
}

enum class AnswerAIType {
    RETURN,
    SQL_FOR_AI
}