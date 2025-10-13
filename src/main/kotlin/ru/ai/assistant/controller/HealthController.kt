package ru.ai.assistant.controller

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.ZoneId

@RestController
class HealthController {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/healthz", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun healthz(): Mono<Map<String, Any>> {
        log.info("healthz")
        val now = OffsetDateTime.now(ZoneId.systemDefault())
        val result = mapOf(
            "status" to "ok",
            "time" to now.toString(),
            "zone" to ZoneId.systemDefault().id
        )
        return Mono.just(result)
    }
}