package ru.ai.assistant.application.audit

import org.springframework.stereotype.Service
import ru.ai.assistant.domain.TelegramMessage
import ru.ai.assistant.domain.audit.AuditLogEntity
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.domain.audit.PayloadTypeLog

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository
) {


    suspend fun log(message: TelegramMessage, payload: String, typeLog: PayloadTypeLog? = null ) {
        auditLogRepository.save(
            AuditLogEntity(
                userId = message.from?.id!!,
                chatId = message.chat.id,
                source = "user",
                payloadTypeLog = typeLog ?: PayloadTypeLog.TEXT,
                payload = payload
            )
        )

    }

}