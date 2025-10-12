package ru.ai.assistant.application

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
    CONTINUE,
    REPLY_TO_LLM
}