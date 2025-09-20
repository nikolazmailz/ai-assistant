package ru.ai.assistant

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkBeans
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.ShouldSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import io.kotest.extensions.spring.SpringExtension
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import ru.ai.assistant.api.TelegramWebhookController
import ru.ai.assistant.application.AiAssistantService
import ru.ai.assistant.infra.openapi.OpenAIServiceImpl

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [TestWebClientConfig::class],
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.liquibase=TRACE",
        "logging.level.org.springframework.boot.autoconfigure.liquibase=TRACE"
    ]
)
@ActiveProfiles("test")
@Testcontainers
@MockkBeans(
    MockkBean(TelegramWebhookController::class),
//    MockkBean(OpenAIWebClientConfig::class),
    MockkBean(AiAssistantService::class),
    MockkBean(OpenAIServiceImpl::class),
)
abstract class BaseIT(body: ShouldSpec.() -> Unit = {}) : ShouldSpec(body) {

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
            // R2DBC
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)

            // JDBC-datasource (для Liquibase)
            registry.add("spring.liquibase.enabled") {
                "true"
            }
            registry.add("spring.liquibase.url") {
                "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.liquibase.user", postgres::getUsername)
            registry.add("spring.liquibase.password", postgres::getPassword)

            // Мастер-чейндж-лог
            registry.add("spring.liquibase.change-log") {
                "classpath:db/changelog/db.changelog-master.yaml"
            }

        }

    }
}