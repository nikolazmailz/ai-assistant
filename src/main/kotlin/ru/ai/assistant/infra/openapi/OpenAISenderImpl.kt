package ru.ai.assistant.infra.openapi

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.ai.assistant.application.openai.AISender
import ru.ai.assistant.infra.openapi.dto.ChatCompletionResponse

@Service
class OpenAISenderImpl(
    private val openaiWebClient: WebClient,
) : AISender {

    private val log = KotlinLogging.logger {}

    override fun defineTitleDialog(prompt: String): Mono<String> {
        val request = mapOf(
//            "model" to "gpt-4o-mini",
            "model" to "gpt-3.5-turbo",
            "messages" to listOf(
                mapOf("role" to "user", "content" to "Определи название для диалога : ${prompt.take(500)}")
            )
        )

        return openaiWebClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatCompletionResponse::class.java)
            .doOnNext { log.info { "Ответ OpenAI: $it" } }
            .doOnError { t -> log.error(t) { "Ошибка при запросе к OpenAI" } }
            .map {
                it.choices.first().message.content
            }
    }


    override fun chatWithGPT(prompt: List<Map<String, String>>, knowledge: String): Mono<String> {

        val request = mapOf(
            "model" to "gpt-4o-mini",
//            "model" to "gpt-3.5-turbo",
            "messages" to prompt
        )

        return openaiWebClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatCompletionResponse::class.java)
            .doOnNext {
//                log.debug { "Ответ OpenAI: $it" }
            }
            .doOnError { t -> log.error(t) { "Ошибка при запросе к OpenAI" } }
            .map {
                it.choices.first().message.content
            }
    }

}