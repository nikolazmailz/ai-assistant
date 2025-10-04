package ru.ai.assistant.application

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ai.assistant.application.scheduler.PollResult
import ru.ai.assistant.db.RawSqlService
import ru.ai.assistant.domain.ConversationQueueEntity
import ru.ai.assistant.domain.ConversationQueueRepository
import ru.ai.assistant.domain.Direction
import ru.ai.assistant.domain.QueueStatus
import ru.ai.assistant.domain.RoleType
import ru.ai.assistant.domain.TelegramUpdate
import ru.ai.assistant.domain.audit.AuditLogEntity
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.domain.audit.PayloadTypeLog
import ru.ai.assistant.infra.TelegramClient
import java.time.Instant

@Service
class AiAssistantHandler (
    private val auditLogRepository: AuditLogRepository,
    private val conversationQueueRepository: ConversationQueueRepository,
    private val conversionService: ConversionService,
    private val openAIService: OpenAIService,
    private val telegramClient: TelegramClient,
    private val objectMapper: ObjectMapper,
    private val rawSqlService: RawSqlService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun handleMessage(update: TelegramUpdate) {
        val message = update.message ?: return
        val text = message.text ?: return

        log.debug("update.message.text: $text")

        // сохраняем входящее сообщение
        auditLogRepository.save(
            AuditLogEntity(
                userId = message.from?.id!!,
                chatId = message.chat.id,
//                sessionId = sessionId,
                source = "telegram",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = text
                // id/createdAt/updatedAt — оставляем на DEFAULT в БД
            )
        )

        conversationQueueRepository.save(
            ConversationQueueEntity(
                userId       = message.from.id,
                chatId       = message.chat.id,
                payload      = text,
                status       = QueueStatus.NEW,
                scheduledAt  = Instant.now().plusSeconds(5),
//                dialogId     = UUID.randomUUID(),
                source       = "telegram",
                direction    = Direction.INBOUND,
                role         = RoleType.USER,
                payloadType  = ru.ai.assistant.domain.PayloadType.TEXT,
//                stepKind     = "request",
//                nextStepHint = "llm_call,
//                actionType   = null,
//                createdAt    = Instant.now(),
//                updatedAt    = Instant.now()
            )
        )

        // запрос в OpenAI
//        val response = openAIService.chatWithGPT(text)

        // сохраняем ответ
//        auditLogRepository.save(
//            TestMsg(
//                chatId = message.message_id,
//                userId = message.from.id,
//                text = response,
//                createdAt = OffsetDateTime.now(),
//            )
//        )

        // парсим ответ
//        val answerAI = runCatching {
//            objectMapper.readValue<AnswerAI>(response)
//        }.getOrElse {
//            log.error(it) { "Ошибка при разборе ответа OpenAI" }
//            telegramClient.sendMessage(message.chat.id, response)
//            return
//        }

        // выполняем SQL, если есть
//        if (answerAI.sql != null) {
//            try {
//                val rawSqlServiceResult = rawSqlService.execute(answerAI.sql)
//                log.debug("rawSqlServiceResult: $rawSqlServiceResult")
//            } catch (e: Exception) {
//                log.error(e) { "Ошибка при выполнении SQL" }
//            }
//        }

        // отправляем ответ пользователю
//        telegramClient.sendMessage(message.chat.id, response)
    }



//    fun handleMessage(update: TelegramUpdate): Mono<Unit> {
//
//        if(update.message == null || update.message.text == null) {
//            return Mono.empty()
//        }
//
//        log.debug("update.message?.text!! ${update.message?.text!!}")
//
//        mono {
//            auditLogRepository.save(
//                TestMsg(
//                    chatId = update.message.message_id,
//                    userId = update.message.from?.id!!,
//                    text = update.message.text,
//                    createdAt = OffsetDateTime.now(),
//                )
//            )
//        }.subscribe()
//
//        return openAIService.chatWithGPT(update.message.text).flatMap {
//
//            mono {
//                auditLogRepository.save(
//                    TestMsg(
//                        chatId = update.message.message_id,
//                        userId = update.message.from?.id!!,
//                        text = it,
//                        createdAt = OffsetDateTime.now(),
//                    )
//                )
//            }.subscribe()
//
//            val answerAI: AnswerAI = objectMapper.readValue<AnswerAI>(it)
//
//            if(answerAI.sql != null){
//                mono {
//                    val rawSqlServiceResult = rawSqlService.execute(answerAI.sql)
//                    log.debug("rawSqlServiceResult $rawSqlServiceResult")
//                }.subscribe()
//            }
//
//            telegramClient.sendMessage(
//                update.message.chat.id, it
//            )
//
//        }
//    }


    @Transactional
    suspend fun pollOnce(batchSize: Int): PollResult {
        val lockedItems = conversationQueueRepository.pickBatchForProcessing(batchSize).toList()
        if (lockedItems.isEmpty()) return PollResult(locked = 0, sent = 0, failed = 0)

        var sent = 0
        var failed = 0
        for (item in lockedItems) {
            val id = requireNotNull(item.id) { "conversation_queue.id must not be null" }
            try {
                // todo business logic
                conversionService.handleMsg(item)
                conversationQueueRepository.markSuccess(id)
                sent++
            } catch (t: Throwable) {
                log.warn("Processing failed for task id={}", id, t)
                conversationQueueRepository.markError(id)
                failed++
            }
        }
        return PollResult(locked = lockedItems.size, sent = sent, failed = failed)
    }

}
