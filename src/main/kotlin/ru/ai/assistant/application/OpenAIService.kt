package ru.ai.assistant.application

import reactor.core.publisher.Mono

interface OpenAIService {

    fun chatWithGPT(prompt: String): Mono<String>
}