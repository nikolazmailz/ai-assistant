package ru.ai.assistant.application.audit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.ai.assistant.domain.TelegramMessage
import ru.ai.assistant.domain.audit.AuditLogEntity
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.domain.audit.PayloadTypeLog
import ru.ai.assistant.utils.JacksonObjectMapper

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository
) {

    private val log = KotlinLogging.logger {}

    suspend fun logUserText(message: TelegramMessage, payload: String, typeLog: PayloadTypeLog? = null ) {
        auditLogRepository.save(
            AuditLogEntity(
                userId = message.from?.id!!,
                chatId = message.chat.id,
                source = "logUserText",
                payloadTypeLog = typeLog ?: PayloadTypeLog.TEXT,
                payload = payload
            )
        )

    }

    suspend fun logDialogQueueHistory(userId: Long, chatId: Long, dialogs: List<Map<String, String>>) {
//        log.debug { "DialogService answers ${jacksonObjectMapper().writeValueAsString(dialogs)}" }
        auditLogRepository.save(
            AuditLogEntity(
                userId = userId,
                chatId = chatId,
                source = "logDialogQueueHistory",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = JacksonObjectMapper.instance.writeValueAsString(dialogs)
            )
        )
    }

    suspend fun logRequestToAi(userId: Long, chatId: Long, requestAi: String) {
        auditLogRepository.save(
            AuditLogEntity(
                userId = userId,
                chatId = chatId,
                source = "logRequestToAi",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = requestAi
            )
        )
    }

    suspend fun logAnswersAi(userId: Long, chatId: Long, responseAi: String?) {
//        log.debug { "responseAi $responseAi" }
        auditLogRepository.save(
            AuditLogEntity(
                userId = userId,
                chatId = chatId,
                source = "logAnswersAi",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = responseAi
            )
        )
    }



}