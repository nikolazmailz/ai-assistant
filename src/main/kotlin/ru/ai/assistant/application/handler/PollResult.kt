package ru.ai.assistant.application.handler

data class PollResult(
    val locked: Int,
    val sent: Int,
    val failed: Int
) {
}