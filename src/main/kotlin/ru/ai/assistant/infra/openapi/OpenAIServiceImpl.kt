package ru.ai.assistant.infra.openapi

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.ai.assistant.application.OpenAIService
import ru.ai.assistant.domain.systemprompt.SystemPromntRepository
import ru.ai.assistant.infra.openapi.dto.ChatCompletionResponse

@Service
class OpenAIServiceImpl(
    private val openaiWebClient: WebClient,
    private val systemPromntRepository: SystemPromntRepository,
) : OpenAIService {

    private val log = KotlinLogging.logger {}

//    private val systemPrompt = """
//        Ты полезный Telegram-ассистент. Отвечай кратко и по делу.
//        ответ всегда присылай в формате JSON на основе объекта
//
//        data class AnswerAI(
//            val answer: String,
//            val sql: String,
//            val action: AnswerAIType,
//        )
//
//        enum class AnswerAIType {
//            RETURN,
//            CONTINUE
//        }
//
//        Где answer - текст ответа
//        sql - SQL запрос который будет выполнен к БД
//        тип ответа пока всегда AnswerAIType.RETURN
//
//    """.trimIndent()

    private val systemPrompt = """
        Ты — полезный Telegram-ассистент.
Отвечай кратко и по делу.

Всегда возвращай результат **в формате JSON-массива** объектов типа:

[
{
    "answer": "",
    "sql": "",
    "action": "",
    "order": "",
},
{ // если два шага
    "answer": "",
    "sql": "",
    "action": "",
    "order": "",
}
]

data class AnswerAI(
    val answer: String,   -- текст ответа пользователю
    val sql: String,      -- SQL-запрос, который нужно выполнить
    val action: AnswerAIType -- тип действия, определяет логику шага,
    val order: Long -- порядок ответа
)

enum class AnswerAIType {
    RETURN,   -- ответ готов — можно отправлять пользователю (SQL может быть выполнен)
    CONTINUE  -- нужно получить дополнительные данные перед формированием финального ответа
}

Пояснение:
- `answer` — текстовый ответ ассистента.
- `sql` — SQL-запрос к БД, который требуется выполнить.
- `action = RETURN` — значит, что результат можно вернуть пользователю.
- `action = CONTINUE` — значит, что потребуется ещё один запрос с результатом выполнения SQL или другой операции.
- `order` — порядок обработки сообщения   

Твои знания: 

    """.trimIndent()

    override fun chatWithGPT(prompt: String, knowledge: String): Mono<String> {

//        return mono {
//            systemPromntRepository.findFirstByIsActiveTrue()!!
//        }.flatMap { sPromnt ->

        val request = mapOf(
//            "model" to "gpt-4o-mini",
            "model" to "gpt-3.5-turbo",
            "messages" to listOf(
//                mapOf("role" to "system", "content" to LocalDateTime.now()),
                mapOf("role" to "system", "content" to "$systemPrompt $knowledge"),
                mapOf("role" to "user", "content" to prompt)
            )
//            "input" to prompt
        )

        log.debug { "chatWithGPT request $request" }

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
//    }
}