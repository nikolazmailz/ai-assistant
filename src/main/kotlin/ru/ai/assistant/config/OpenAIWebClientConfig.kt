package ru.ai.assistant.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider

@Configuration
@EnableConfigurationProperties(OpenAiProperties::class, ProxyProperties::class)
class OpenAIWebClientConfig {

    @Bean
    fun openaiWebClient(
        openAiProperties: OpenAiProperties,
        proxyProperties: ProxyProperties
    ): WebClient {

        val httpClient = HttpClient.create()
            .proxy { proxy ->
                proxy
                    .type(ProxyProvider.Proxy.HTTP)
                    .host(proxyProperties.host)
                    .port(proxyProperties.port)
                    .username(proxyProperties.user)
                    .password { proxyProperties.password}
            }

        return WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${openAiProperties.token}")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

}