package ru.ai.assistant.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.proxy")
data class ProxyProperties(
    val host: String = "proxyHost",
    val port: String = "0",
    val user: String = "proxyUsername",
    val password: String = "proxyPassword",
)