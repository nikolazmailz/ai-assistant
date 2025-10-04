package ru.ai.assistant.application

data class AnswerAI(
    val answer: String,
    val sql: String? = null,
    val action: AnswerAIType,
) {
}

enum class AnswerAIType {
    RETURN,
    CONTINUE
}