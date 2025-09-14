package ru.ai.assistant.infra.openapi.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(
    val content: String,
    val role: String,
    @JsonProperty("tool_calls") val toolCalls: Any?,
    @JsonProperty("function_call") val functionCall: Any?
)