package ru.ai.assistant.application

interface LLMService {

    suspend fun chatWithGPT(prompt: String): String
}