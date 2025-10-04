package ru.ai.assistant.config.scheduler

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(SchedulerProperties::class)
class SchedulingConfig {
}