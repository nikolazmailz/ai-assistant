package ru.ai.assistant.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TelegramProperties::class)
class PropertiesConfig