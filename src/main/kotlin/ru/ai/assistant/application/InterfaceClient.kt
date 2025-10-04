package ru.ai.assistant.application

import reactor.core.publisher.Mono
import ru.ai.assistant.infra.TelegramClient.ParseMode

interface InterfaceClient {

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        parseMode: ParseMode? = null
    ): Mono<Unit>
}