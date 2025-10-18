package ru.ai.assistant.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.runs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.ai.assistant.application.DialogService
import ru.ai.assistant.application.handler.AiAssistantHandler
import ru.ai.assistant.domain.TelegramChat
import ru.ai.assistant.domain.TelegramMessage
import ru.ai.assistant.domain.TelegramUpdate
import ru.ai.assistant.domain.TelegramUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Testcontainers
class TelegramWebhookControllerIT : ShouldSpec({
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
}) {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var dialogService: DialogService

    @MockkBean
    private lateinit var aiAssistantHandler: AiAssistantHandler

    override fun extensions(): List<Extension> = listOf(SpringExtension)

    companion object {
        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16").apply {
            withDatabaseName("testdb")
            withUsername("postgresql")
            withPassword("postgresql")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)

            registry.add("spring.liquibase.enabled") { "true" }
            registry.add("spring.liquibase.url") {
                "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.liquibase.user", postgres::getUsername)
            registry.add("spring.liquibase.password", postgres::getPassword)
            registry.add("spring.liquibase.change-log") { "classpath:db/changelog/db.changelog-master.yaml" }
        }
    }
}
