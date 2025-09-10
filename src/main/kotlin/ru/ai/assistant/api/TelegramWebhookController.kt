package ru.ai.assistant.api

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ai.assistant.domain.TelegramUpdate
import ru.ai.assistant.infra.TelegramClient

/**
 * Заглушка: принимает апдейты Telegram и пишет их в лог.
 */
@RestController
@RequestMapping("/tg")
class TelegramWebhookController(
    private val telegramClient: TelegramClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/webhook")
    fun onUpdate(@RequestBody update: TelegramUpdate): ResponseEntity<Unit> {
        log.info("Got update: {}", update)
        telegramClient.sendMessage(update.message?.chat?.id!!, "Hi").subscribe()
        return ResponseEntity.ok().build()
    }
}
