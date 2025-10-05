package ru.ai.assistant.domain.systemprompt


import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface SystemPromntRepository : CoroutineCrudRepository<SystemPromnt, UUID> {

    suspend fun findByName(name: String): SystemPromnt?

    suspend fun findFirstByIsActiveTrue(): SystemPromnt?
}