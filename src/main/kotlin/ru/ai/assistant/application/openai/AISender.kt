package ru.ai.assistant.application.openai

import reactor.core.publisher.Mono

interface AISender {

    fun defineTitleDialog(prompt: String): Mono<String>
    fun chatWithGPT(prompt: List<Map<String, String>>, knowledge: String, userId: Long, chatId: Long): Mono<String>
//    fun chatWithGPT(request: Map<String, Any>): Mono<String>
}