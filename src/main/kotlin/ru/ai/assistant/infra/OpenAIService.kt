package ru.ai.assistant.infra

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono
import ru.ai.assistant.application.LLMService
import ru.ai.assistant.infra.openapi.dto.ChatCompletionResponse

@Service
class OpenAIService(
    private val openaiWebClient: WebClient
) : LLMService {

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

    override suspend fun chatWithGPT(prompt: String): String {
        val request = mapOf(
//            "model" to "gpt-4o-mini",
            "model" to "gpt-3.5-turbo",
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to prompt)
            )
//            "input" to prompt
        )

        val response = openaiWebClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .awaitBody<ChatCompletionResponse>()

        return response.choices.first().message.content
    }

}