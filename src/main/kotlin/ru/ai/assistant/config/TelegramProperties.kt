package ru.ai.assistant.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.telegram")
data class TelegramProperties(
    var botToken: String = "REPLACE_ME",
    var webhookSecret: String? = null
)