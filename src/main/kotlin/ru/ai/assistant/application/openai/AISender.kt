package ru.ai.assistant.application.openai

import reactor.core.publisher.Mono

interface AISender {

    fun defineTitleDialog(prompt: String): Mono<String>
    fun chatWithGPT(prompt: String, knowledge: String): Mono<String>
//    fun chatWithGPT(request: Map<String, Any>): Mono<String>
}