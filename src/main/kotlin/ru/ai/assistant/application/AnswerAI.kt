package ru.ai.assistant.application

data class AnswerAI(
    val answer: String,
    val sql: String,
    val action: AnswerAIType,
) {
}

enum class AnswerAIType {
    RETURN,
    CONTINUE
}