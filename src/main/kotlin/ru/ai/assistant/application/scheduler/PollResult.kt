package ru.ai.assistant.application.scheduler

data class PollResult(
    val locked: Int,
    val sent: Int,
    val failed: Int
) {
}