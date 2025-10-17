package ru.ai.assistant.application.handler

import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ai.assistant.application.DialogService
import ru.ai.assistant.application.metainfo.DialogMetaInfoEntityService
import ru.ai.assistant.domain.DialogQueue
import ru.ai.assistant.domain.DialogQueueRepository
import ru.ai.assistant.domain.QueueStatus
import ru.ai.assistant.domain.RoleType
import ru.ai.assistant.domain.SourceDialogType
import ru.ai.assistant.domain.TelegramUpdate
import ru.ai.assistant.domain.audit.AuditLogEntity
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.domain.audit.PayloadTypeLog
import java.time.Instant

@Service
class AiAssistantHandler (
    private val auditLogRepository: AuditLogRepository,
    private val dialogQueueRepository: DialogQueueRepository,
    private val dialogService: DialogService,
    private val dialogMetaInfoEntityService: DialogMetaInfoEntityService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

//    suspend fun handleMessage(update: TelegramUpdate) {
//        val message = update.message ?: return
//        val text = message.text ?: return
//
//        log.debug("update.message.text: $text")
//
//        // сохраняем входящее сообщение
//        auditLogRepository.save(
//            AuditLogEntity(
//                userId = message.from?.id!!,
//                chatId = message.chat.id,
////                sessionId = sessionId,
//                source = "user",
//                payloadTypeLog = PayloadTypeLog.TEXT,
//                payload = text
//                // id/createdAt/updatedAt — оставляем на DEFAULT в БД
//            )
//        )
//
//        val dialogMetaInfo = dialogMetaInfoEntityService.getOrCreateDialogMetaInfo(userId = message.from.id)
//
////        if(dialogMetaInfo.title == null) {
////            // todo call llm for define title
////        }
//
//        dialogQueueRepository.save(
//            DialogQueue(
//                userId = message.from.id,
//                chatId = message.chat.id,
//                dialogId = dialogMetaInfo.id,
//                dialogTitle = dialogMetaInfo.title,
//                status = QueueStatus.NEW,
//                payload = text,
//                scheduledAt = Instant.now().plusSeconds(5),
//                source = SourceDialogType.TELEGRAM,
//                role = RoleType.USER,
//            )
//        )
//    }

    @Transactional
    suspend fun pollOnce(batchSize: Int): PollResult {
        val lockedItems = dialogQueueRepository.pickBatchForProcessing(batchSize).toList()
        if (lockedItems.isEmpty()) return PollResult(locked = 0, sent = 0, failed = 0)

        var sent = 0
        var failed = 0
        for (item in lockedItems) {
            val id = requireNotNull(item.id) { "dialog_queue.id must not be null" }
            try {
                // todo business logic
                dialogService.handlePollMsg(item)
                dialogQueueRepository.markSuccess(id)
                sent++
            } catch (t: Throwable) {
                log.warn("Processing failed for task id={}", id, t)
                dialogQueueRepository.markError(id)
                failed++
            }
        }
        return PollResult(locked = lockedItems.size, sent = sent, failed = failed)
    }

}