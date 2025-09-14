package ru.ai.assistant.infra.openapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatCompletionResponse(
    val `object`: String,
    val id: String,
    val model: String,
    val created: Long,
    @JsonProperty("request_id") val requestId: String? = null,
    @JsonProperty("tool_choice") val toolChoice: String?,
    val usage: Usage,
    val seed: Long,
    @JsonProperty("top_p") val topP: Double,
    val temperature: Double,
    @JsonProperty("presence_penalty") val presencePenalty: Double,
    @JsonProperty("frequency_penalty") val frequencyPenalty: Double,
    @JsonProperty("system_fingerprint") val systemFingerprint: String? = null,
    @JsonProperty("input_user") val inputUser: String?,
    @JsonProperty("service_tier") val serviceTier: String,
    val tools: Any?,
    val metadata: Map<String, Any>?,
    val choices: List<Choice>,
    @JsonProperty("response_format") val responseFormat: String?
)
