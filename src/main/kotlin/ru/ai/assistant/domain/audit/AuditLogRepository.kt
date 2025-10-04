package ru.ai.assistant.domain.audit

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository : CoroutineCrudRepository<AuditLogEntity, UUID> {
    fun findAllByChatId(chatId: Long): Flow<AuditLogEntity>
}