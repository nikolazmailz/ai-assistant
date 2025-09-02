package ru.ai.assistant.api

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.ZoneId

@RestController
class HealthController {

    @GetMapping("/healthz", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun healthz(): Mono<Map<String, Any>> {
        val now = OffsetDateTime.now(ZoneId.systemDefault())
        val result = mapOf(
            "status" to "ok",
            "time" to now.toString(),
            "zone" to ZoneId.systemDefault().id
        )
        return Mono.just(result)
    }
}