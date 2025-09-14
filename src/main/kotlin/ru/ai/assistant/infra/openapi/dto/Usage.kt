package ru.ai.assistant.infra.openapi.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Usage(
    @JsonProperty("total_tokens") val totalTokens: Int,
    @JsonProperty("completion_tokens") val completionTokens: Int,
    @JsonProperty("prompt_tokens") val promptTokens: Int
)