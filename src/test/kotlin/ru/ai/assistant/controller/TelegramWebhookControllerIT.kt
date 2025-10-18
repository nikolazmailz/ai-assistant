package ru.ai.assistant.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.runs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import ru.ai.assistant.BaseIT
import ru.ai.assistant.application.DialogService
import ru.ai.assistant.domain.TelegramChat
import ru.ai.assistant.domain.TelegramMessage
import ru.ai.assistant.domain.TelegramUpdate
import ru.ai.assistant.domain.TelegramUser

class TelegramWebhookControllerIT(
    private val dialogService: DialogService
) : BaseIT() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    init {
        {
            should("delegate update handling to dialog service and respond with OK") {
                val update = TelegramUpdate(
                    update_id = 42,
                    message = TelegramMessage(
                        message_id = 100,
                        from = TelegramUser(
                            id = 500,
                            first_name = "John",
                            username = "doe"
                        ),
                        chat = TelegramChat(
                            id = 700,
                            type = "private"
                        ),
                        text = "Hello"
                    )
                )

                coEvery { dialogService.handleMessage(update) } just runs

                webTestClient.post()
                    .uri("/tg/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(update)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<Void>()
                    .isEmpty

                coVerify(exactly = 1) { dialogService.handleMessage(update) }
            }
        }
    }
}
