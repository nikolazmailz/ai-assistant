package ru.ai.assistant.infra.openapi

import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.ai.assistant.application.OpenAIService
import ru.ai.assistant.infra.openapi.dto.ChatCompletionResponse
import java.time.LocalDateTime

@Service
class OpenAIServiceImpl(
    private val openaiWebClient: WebClient
): OpenAIService {

    private val log = KotlinLogging.logger {}

    private val systemPrompt = """
        Ты полезный Telegram-ассистент. Отвечай кратко и по делу.
        ответ всегда присылай в формате JSON на основе объекта 
        
        data class AnswerAI(
            val answer: String,
            val sql: String,
            val action: AnswerAIType,
        )

        enum class AnswerAIType {
            RETURN,
            CONTINUE
        }
        
        Где answer - текст ответа
        sql - SQL запрос который будет выполнен к БД
        тип ответа пока всегда AnswerAIType.RETURN
        
    """.trimIndent()

    override fun chatWithGPT(prompt: String): Mono<String> {
        val request = mapOf(
//            "model" to "gpt-4o-mini",
            "model" to "gpt-3.5-turbo",
            "messages" to listOf(
                mapOf("role" to "system", "content" to LocalDateTime.now()),
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to prompt)
            )
//            "input" to prompt
        )

        return openaiWebClient.post()
            .uri("/chat/completions")
//            .uri("/response")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
//            .onStatus({ it.is2xxSuccessful }) { response ->
//                response.bodyToMono(String::class.java).flatMap { body ->
//                    logger.error { "Ошибка OpenAI: ${response.statusCode()} — $body" }
//                    Mono.error(RuntimeException("Ошибка OpenAI: ${response.statusCode()} — $body"))
//                }
//            }
            .bodyToMono(ChatCompletionResponse::class.java)
            .doOnNext { log.info { "Ответ OpenAI: $it" } }
            .doOnError { t -> log.error(t) { "Ошибка при запросе к OpenAI" } }
            .map {
                it.choices.first().message.content }
    }

}