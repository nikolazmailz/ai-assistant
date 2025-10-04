package ru.ai.assistant.infra.sheduler

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.ai.assistant.application.AiAssistantHandler
import ru.ai.assistant.config.scheduler.SchedulerProperties

@Component
class PollingSchedulerBlocking(
    private val aiAssistantHandler: AiAssistantHandler,
    private val props: SchedulerProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(
        initialDelayString = "\${outbox.poll.initialDelay:PT1S}",
        fixedDelayString = "\${outbox.poll.fixedDelay:PT0.5S}"
    )
    fun tick() = runBlocking {
        if (!props.enabled || !props.poll.enabled) return@runBlocking
        try {
            val result = aiAssistantHandler.pollOnce(props.poll.batchSize)
            if (log.isDebugEnabled && result.locked > 0) {
                log.debug(
                    "Outbox tick (blocking): locked={}, sent={}, failed={}",
                    result.locked, result.sent, result.failed
                )
            }
        } catch (t: Throwable) {
            log.error("Outbox tick (blocking) failed", t)
        }
    }
}