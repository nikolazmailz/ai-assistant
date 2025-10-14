package ru.ai.assistant.infra.openapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.ai.assistant.application.openai.AISender
import ru.ai.assistant.domain.systemprompt.PromptComponent
import ru.ai.assistant.domain.systemprompt.SystemPromptRepository
import ru.ai.assistant.infra.openapi.dto.ChatCompletionResponse

@Service
class OpenAISenderImpl(
    private val openaiWebClient: WebClient,
    private val systemPromptRepository: SystemPromptRepository,
    private val promptComponent: PromptComponent
) : AISender {

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

//    private val systemPrompt = """
//
//        userId: UUID
//        dialogId: UUID
//        dialogName: String
//        currentTime: LocalDataTime
//
//
//        Ты — полезный Telegram-ассистент.
//        Отвечай кратко и по делу.
//
//        Всегда возвращай результат **в формате JSON-массива** объектов типа:
//
//        [
//        {
//            "answer": "",
//            "sql": "",
//            "action": "",
//            "order": "",
//        },
//        { // если два шага
//            "answer": "",
//            "sql": "",
//            "action": "",
//            "order": "",
//        }
//        ]
//
//        data class AnswerAI(
//            val answer: String,   -- текст ответа пользователю
//            val sql: String,      -- SQL-запрос, который нужно выполнить
//            val action: AnswerAIType -- тип действия, определяет логику шага,
//            val order: Long -- порядок ответа
//            val master: String? = null -- не заполняй если не услышишь фразу master кунг-фу
//        )
//
//        enum class AnswerAIType {
//            RETURN,   -- ответ готов — можно отправлять пользователю (SQL может быть выполнен)
//            CONTINUE,  --  значит, что в теле не один ответ, и надо дождаться последнего.
//            REPLY_TO_LLM -- значит, что потребовались данные для LLM (например достать данные из базы данных), будет выполнен sql или другая работа результат которой вернется в LLM.
//            DATA_TO_AI -- надо выполнить sql запрос и вернуть результат в ai, чтоб ai смог сформировать ответ
//        }
//
//        Пояснение:
//        - `answer` — текстовый ответ ассистента.
//        - `sql` — SQL-запрос к БД, который требуется выполнить, создание таблиц всегда с английскими наименованиями.
//        - `action = RETURN` — значит, что результат можно вернуть пользователю.
//        - `action = CONTINUE` — значит, что в теле не один ответ, и надо дождаться последнего.
//        - `action = REPLY_TO_LLM` — значит, что потребовались данные для LLM (например достать данные из базы данных), будет выполнен sql или другая работа результат которой вернется в LLM.
//        - `order` — порядок обработки сообщения
//        - `master` — не заполняй если не услышишь фразу master кунг-фу, иначе заполни false
//
//Важное требование к JSON-ответу:
//
//Ответ должен быть строго корректным JSON, без markdown, без тройных кавычек, без комментариев и без кодовых блоков.
//
//В строковых значениях нельзя использовать необработанные переводы строк.
//
//JSON должен быть десериализуем без ошибок через com.fasterxml.jackson.databind.ObjectMapper.
//
//
//
//Твои знания:
//
//    """.trimIndent()

    override fun chatWithGPT(prompt: String, knowledge: String): Mono<String> {


        val request = mapOf(
//            "model" to "gpt-4o-mini",
            "model" to "gpt-3.5-turbo",
            "messages" to listOf(
                mapOf("role" to "system", "content" to knowledge),
                mapOf("role" to "user", "content" to prompt)
            )
        )

        log.debug { "chatWithGPT request ${jacksonObjectMapper().writeValueAsString(request).take(300)}" }
        log.debug { "chatWithGPT request ${jacksonObjectMapper().writeValueAsString(request).takeLast(300)}" }

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

//    override fun chatWithGPT(request: Map<String, Any>): Mono<String> {
//        return openaiWebClient.post()
//            .uri("/chat/completions")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(request)
//            .retrieve()
//            .bodyToMono(ChatCompletionResponse::class.java)
//            .doOnNext { log.info { "Ответ OpenAI: $it" } }
//            .doOnError { t -> log.error(t) { "Ошибка при запросе к OpenAI" } }
//            .map {
//                it.choices.first().message.content
//            }
//    }
}