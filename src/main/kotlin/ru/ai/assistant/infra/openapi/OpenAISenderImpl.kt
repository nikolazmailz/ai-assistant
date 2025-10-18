package ru.ai.assistant.infra.openapi

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import ru.ai.assistant.application.openai.AISender
import ru.ai.assistant.infra.openapi.dto.ChatCompletionResponse
import kotlin.collections.take
import kotlin.text.take

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

        val x =  mapOf("role" to "user", "content" to "Определи название для диалога : ${prompt.take(500)}")

        x.let { it ->
            log.debug { it }
        }

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

        val schema = mapOf(
            "type" to "json_schema",
            "json_schema" to mapOf(
                "name" to "AnswerAIArray",
                "strict" to true,
                "schema" to mapOf(
                    "\$schema" to "https://json-schema.org/draft/2020-12/schema",
                    "title" to "AnswerAIArray",
                    "type" to "array",
                    "minItems" to 1,
                    "items" to mapOf(
                        "type" to "object",
                        "additionalProperties" to false,
                        "required" to listOf("answer", "action", "order"),
                        "properties" to mapOf(
                            "answer" to mapOf("type" to "string"),
                            "sql" to mapOf("type" to listOf("string", "null"), "default" to null),
                            "action" to mapOf("type" to "string", "enum" to listOf("RETURN", "SQL_FOR_AI")),
                            "order" to mapOf("type" to "integer", "minimum" to 0),
                            "master" to mapOf("type" to listOf("string", "null"), "default" to null)
                        )
                    )
                )
            )
        )

        val request = mapOf(
            "model" to "gpt-4o-mini",
//            "model" to "gpt-3.5-turbo",
            "messages" to prompt
//            "temperature" to 0,
//            "response_format" to schema
        )

//        "messages" to listOf(
//
//        )

        log.debug { "chatWithGPT request $request" }

        return openaiWebClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
//            .retrieve()
//            .bodyToMono(ChatCompletionResponse::class.java)
//            .doOnNext {
//                log.debug { "Ответ OpenAI: $it" }
//            }
//            .doOnError { t -> log.error(t) { "Ошибка при запросе к OpenAI" } }
//            .map {
//                it.choices.first().message.content
//            }
            .exchangeToMono { resp ->
                if (resp.statusCode().is2xxSuccessful) {
                    resp.bodyToMono(ChatCompletionResponse::class.java).doOnNext {
                        log.debug { "Ответ OpenAI: $it" }
                    }
                } else {
                    resp.bodyToMono(String::class.java)
                        .flatMap { body ->
                            log.error { "OpenAI 4xx/5xx: ${resp.statusCode().value()} body=$body" }
                            Mono.error(
                                WebClientResponseException.create(
                                resp.statusCode().value(), "OpenAI error",
                                resp.headers().asHttpHeaders(), body.toByteArray(), null
                            ))
                        }
                }
            }
            .map { it.choices.first().message.content }
    }

}