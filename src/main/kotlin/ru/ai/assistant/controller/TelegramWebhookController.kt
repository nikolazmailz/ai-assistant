package ru.ai.assistant.controller

import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import ru.ai.assistant.application.handler.AiAssistantHandler
import ru.ai.assistant.domain.TelegramUpdate

/**
 * Заглушка: принимает апдейты Telegram и пишет их в лог.
 */
@RestController
@RequestMapping("/tg")
class TelegramWebhookController(
    private val aiAssistantHandler: AiAssistantHandler
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/webhook")
    fun onUpdate(@RequestBody update: TelegramUpdate): Mono<ResponseEntity<Void>> {
        log.info("Got update: {}", update)
        return mono {
            aiAssistantHandler.handleMessage(update)
            ResponseEntity.ok().build<Void>()
        }
    }
}


//️ Если handleMessage не должен блокировать ответ
//
//Если ты хочешь асинхронно обработать сообщение и сразу ответить Telegram 200 OK, то лучше не ждать завершения handleMessage:


//Ждать выполнения обработки	mono { aiAssistantHandler.handleMessage(update) }
//Ответить сразу (fire-and-forget)	Mono.fromRunnable { runBlocking { ... } }.subscribe()

