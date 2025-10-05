package ru.ai.assistant.config.scheduler

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("app.scheduler")
data class SchedulerProperties(
    var enabled: Boolean = true,
    var poll: Poll = Poll()
) {

    data class Poll(
        var enabled: Boolean = true,
        /** ISO-8601: PT1S, PT500MS, PT2M, … */
        var initialDelay: Duration = Duration.ofSeconds(1),
        var fixedDelay: Duration = Duration.ofMillis(500),
        var batchSize: Int = 50
    )
}