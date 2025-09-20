package ru.ai.assistant

import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.WebClient

@TestConfiguration
class TestWebClientConfig {

    companion object {
        // Запускаем MockWebServer один раз
        @JvmStatic
        val mockServer = MockWebServer().apply { start() }
    }

    @Bean
    @Primary
    fun openaiWebClient(): WebClient =
        WebClient.builder()
            // базовый URL на корневой путь мок-сервера
            .baseUrl(mockServer.url("/").toString().removeSuffix("/"))
            .build()

    @Bean
    fun mockServer(): MockWebServer = mockServer
}