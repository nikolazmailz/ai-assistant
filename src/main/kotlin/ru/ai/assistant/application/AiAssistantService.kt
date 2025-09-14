package ru.ai.assistant.application

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.ai.assistant.domain.TelegramUpdate
import ru.ai.assistant.infra.TelegramClient

@Service
class AiAssistantService(
    private val openAIService: OpenAIService,
    private val telegramClient: TelegramClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun handleMessage(update: TelegramUpdate): Mono<Unit> {

        log.debug("update.message?.text!! ${update.message?.text!!}")

        return openAIService.chatWithGPT(update.message.text).flatMap {
            telegramClient.sendMessage(
                update.message.chat.id, it
            )
        }
    }
}