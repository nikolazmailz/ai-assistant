package ru.ai.assistant.infra.openapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import ru.ai.assistant.BaseIT
import java.util.concurrent.TimeUnit

class OpenAISenderImplIT @Autowired constructor(
    private val mockServer: MockWebServer,
    private val webClient: WebClient
) : BaseIT({

    val objectMapper = jacksonObjectMapper()
    val openAISender = OpenAISenderImpl(webClient)

    should("define dialog title using OpenAI response") {
        val expectedContent = "Диалог о погоде"
        mockServer.enqueue(successfulResponse(expectedContent))

        val longPrompt = "p".repeat(550)

        val result = openAISender.defineTitleDialog(longPrompt).block()

        result shouldBe expectedContent

        val recordedRequest = mockServer.takeRequest(1, TimeUnit.SECONDS)
        requireNotNull(recordedRequest)
        recordedRequest.path shouldBe "/chat/completions"

        val requestJson = objectMapper.readTree(recordedRequest.body.readUtf8())
        requestJson.get("model").asText() shouldBe "gpt-3.5-turbo"
        val content = requestJson.get("messages")[0].get("content").asText()
        content shouldContain longPrompt.take(500)
        content.length shouldBe ("Определи название для диалога : ".length + 500)
    }

    should("chat with GPT and return assistant message") {
        val expectedContent = "Ответ ассистента"
        mockServer.enqueue(successfulResponse(expectedContent))

        val prompt = listOf(
            mapOf("role" to "user", "content" to "Привет")
        )

        val result = openAISender.chatWithGPT(prompt, knowledge = "").block()

        result shouldBe expectedContent

        val recordedRequest = mockServer.takeRequest(1, TimeUnit.SECONDS)
        requireNotNull(recordedRequest)
        recordedRequest.path shouldBe "/chat/completions"

        val requestJson = objectMapper.readTree(recordedRequest.body.readUtf8())
        requestJson.get("model").asText() shouldBe "gpt-4o-mini"
        val messagesNode = requestJson.get("messages")
        messagesNode.size() shouldBe prompt.size
        messagesNode[0].get("role").asText() shouldBe "user"
        messagesNode[0].get("content").asText() shouldBe "Привет"
    }

    should("propagate non-successful responses as WebClientResponseException") {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"internal"}""")
        )

        shouldThrow<WebClientResponseException> {
            openAISender.chatWithGPT(emptyList(), knowledge = "").block()
        }
    }
}) {
    private fun successfulResponse(content: String) =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(
                """
                {
                  "object": "chat.completion",
                  "id": "chatcmpl-123",
                  "model": "gpt-3.5-turbo",
                  "created": 1710000000,
                  "tool_choice": null,
                  "usage": {
                    "total_tokens": 20,
                    "completion_tokens": 10,
                    "prompt_tokens": 10
                  },
                  "seed": 1,
                  "top_p": 1.0,
                  "temperature": 0.7,
                  "presence_penalty": 0.0,
                  "frequency_penalty": 0.0,
                  "input_user": null,
                  "service_tier": "default",
                  "tools": null,
                  "metadata": null,
                  "choices": [
                    {
                      "index": 0,
                      "message": {
                        "content": "$content",
                        "role": "assistant",
                        "tool_calls": null,
                        "function_call": null
                      },
                      "finish_reason": "stop",
                      "logprobs": null,
                      "tool_calls": null,
                      "function_call": null
                    }
                  ],
                  "response_format": null
                }
                """.trimIndent()
            )
}
