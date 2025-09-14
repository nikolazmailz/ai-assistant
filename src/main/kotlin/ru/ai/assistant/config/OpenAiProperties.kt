package ru.ai.assistant.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.openai")
data class OpenAiProperties(
    val token: String = "REPLACE_ME",
)