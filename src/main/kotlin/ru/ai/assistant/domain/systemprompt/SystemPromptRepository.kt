package ru.ai.assistant.domain.systemprompt


import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface SystemPromptRepository : CoroutineCrudRepository<SystemPrompt, UUID> {

    suspend fun findByName(name: String): SystemPrompt?

    suspend fun findFirstByIsActiveTrue(): SystemPrompt?
}