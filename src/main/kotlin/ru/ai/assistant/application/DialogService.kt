package ru.ai.assistant.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import ru.ai.assistant.domain.DialogQueue
import com.fasterxml.jackson.core.type.TypeReference
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.ai.assistant.application.openai.OpenAISender
import ru.ai.assistant.db.RawSqlService
import ru.ai.assistant.db.SqlScript
import ru.ai.assistant.domain.DialogQueueRepository
import ru.ai.assistant.domain.Direction
import ru.ai.assistant.domain.PayloadType
import ru.ai.assistant.domain.QueueStatus
import ru.ai.assistant.domain.RoleType
import ru.ai.assistant.domain.audit.AuditLogEntity
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.domain.audit.PayloadTypeLog
import ru.ai.assistant.infra.TelegramClient
import java.time.Instant
import java.time.LocalDateTime

@Service
class DialogService(
    private val openAISender: OpenAISender,
    private val telegramClient: TelegramClient,
    private val auditLogRepository: AuditLogRepository,
    private val rawSqlService: RawSqlService,
    private val dialogQueueRepository: DialogQueueRepository,
) {

    private val log = KotlinLogging.logger {}

    suspend fun handleMsg(dialog: DialogQueue) {

        // todo UseCase

        /**
         * Получить текущее время
         * Получить предпомнт
         * Сформировать истории диалога todo
         * Дополнить к сообщение запрос
         * Отправить
         * Получить ответ, сохранить его в лог
         * 1к - Что-то сделать и ответить пользователю
         * 2к - Что-то сделать и сделать новую запись в ConversationQueueRepository
         *
         * */

        val knowledge = rawSqlService.execute(SqlScript.QUERY_ALL_DATA)

        val jsonText = knowledge.first()["tables"] as String


        val additionalData = "\n userId = ${dialog.userId} \n systemTime = ${LocalDateTime.now()}"

        log.debug { "Get knowledge $jsonText" }

        val response = openAISender.chatWithGPT(dialog.payload!!, jsonText + additionalData).awaitSingleOrNull()

        auditLogRepository.save(
            AuditLogEntity(
                userId = dialog.userId,
                chatId = dialog.chatId,
//                sessionId = sessionId,
                source = "AI",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = response
                // id/createdAt/updatedAt — оставляем на DEFAULT в БД
            )
        )

        val answers: List<AnswerAI> = jacksonObjectMapper().readValue(
            response,
            object : TypeReference<List<AnswerAI>>() {}
        )
        log.info { "ConversionService answers $answers" }

        var fullAnswer = ""

        for (answer in answers) {
            fullAnswer += answer.answer

            if (answer.sql != null && answer.sql != "") {
                try {
                    val rawSqlServiceResult = rawSqlService.execute(answer.sql)
                    log.debug { "rawSqlServiceResult: $rawSqlServiceResult" }
                } catch (e: Exception) {
                    log.error(e) { "Ошибка при выполнении SQL" }
                }
            }

            if (answer.action == AnswerAIType.REPLY_TO_LLM) {
                dialogQueueRepository.save(
                    DialogQueue(
                        userId       = dialog.userId,
                        chatId       = dialog.chatId,
                        payload      = jacksonObjectMapper().writeValueAsString(answer),
                        status       = QueueStatus.NEW,
                        scheduledAt  = Instant.now().plusSeconds(5),
//                dialogId     = UUID.randomUUID(),
                        source       = "telegram",
                        direction    = Direction.INBOUND,
                        role         = RoleType.ASSISTANT,
                        payloadType  = PayloadType.TEXT,
//                stepKind     = "request",
//                nextStepHint = "llm_call,
//                actionType   = null,
//                createdAt    = Instant.now(),
//                updatedAt    = Instant.now()
                    )
                )
            }
        }


        telegramClient.sendMessage(dialog.chatId, fullAnswer).awaitSingleOrNull()

    }


}