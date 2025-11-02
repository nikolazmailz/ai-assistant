package ru.ai.assistant.infra

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class TelegramClient(
    @Value("\${app.telegram.bot-token}") private val botToken: String,
    builder: WebClient.Builder
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client = builder
        .baseUrl("https://api.telegram.org/bot$botToken")
        .build()

    enum class ParseMode(val wire: String) {
        MarkdownV2("MarkdownV2"),
        HTML("HTML");
        override fun toString() = wire
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class SendMessageRequest(
        val chat_id: Long,
        val text: String,
        val parse_mode: String? = null,
        val disable_web_page_preview: Boolean? = null,
        val disable_notification: Boolean? = null
    )

    data class TgResponse<T>(
        val ok: Boolean,
        val result: T? = null,
        val description: String? = null
    )

    fun sendMessage(
        chatId: Long,
        text: String,
        parseMode: ParseMode? = null
    ): Mono<Unit> {

        require(chatId > 0) { "chatId must be > 0" }
        require(text.isNotBlank()) { "text must be not blank" }

        val payload = SendMessageRequest(
            chat_id = chatId,
            text = text,
            parse_mode = parseMode?.wire // только допустимые строки
        )

        return client.post()
            .uri("/sendMessage")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchangeToMono { resp ->
                if (resp.statusCode().is2xxSuccessful) {
                    resp.bodyToMono(TgResponse::class.java)
                        .flatMap { raw ->
                            @Suppress("UNCHECKED_CAST")
                            val tg = raw as TgResponse<*>
                            if (tg.ok) Mono.just(Unit)
                            else Mono.error(IllegalStateException("Telegram error: ${tg.description ?: "unknown"}"))
                        }
                } else {
                    resp.bodyToMono(String::class.java)
                        .defaultIfEmpty("<empty body>")
                        .flatMap { body ->
                            val code = resp.statusCode().value()
                            log.warn("Telegram HTTP {}: {}", code, body)
                            Mono.error(IllegalStateException("HTTP $code from Telegram: $body"))
                        }
                }
            }
    }
}
