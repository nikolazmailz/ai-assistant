package ru.ai.assistant.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import ru.ai.assistant.domain.DialogQueue
import com.fasterxml.jackson.core.type.TypeReference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.map
import ru.ai.assistant.application.dto.AnswerAI
import ru.ai.assistant.application.dto.AnswerAIType
import ru.ai.assistant.application.metainfo.DialogMetaInfoEntityService
import ru.ai.assistant.application.openai.AISender
import ru.ai.assistant.db.RawSqlService
import ru.ai.assistant.domain.DialogQueueRepository
import ru.ai.assistant.domain.Direction
import ru.ai.assistant.domain.PayloadType
import ru.ai.assistant.domain.QueueStatus
import ru.ai.assistant.domain.RoleType
import ru.ai.assistant.domain.audit.AuditLogEntity
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.domain.audit.PayloadTypeLog
import ru.ai.assistant.infra.TelegramClient
import ru.ai.assistant.application.security.sql.AnswerAiGuard
import ru.ai.assistant.domain.systemprompt.PromptComponent
import java.time.Instant

@Service
class DialogService(
    private val openAISender: AISender,
    private val telegramClient: TelegramClient,
    private val auditLogRepository: AuditLogRepository,
    private val rawSqlService: RawSqlService,
    private val dialogQueueRepository: DialogQueueRepository,
    private val answerAiGuard: AnswerAiGuard,
    private val promptComponent: PromptComponent,
    private val dialogMetaInfoEntityService: DialogMetaInfoEntityService,
) {

    private val log = KotlinLogging.logger {}

    suspend fun handleMsg(dialogQueue: DialogQueue) {

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

//        val knowledge = rawSqlService.execute(SqlScript.QUERY_ALL_DATA)

//        val jsonText = knowledge.first()["tables"] as String
//
//        val additionalData = "\n userId = ${dialog.userId} \n systemTime = ${LocalDateTime.now()}"
//
//        log.debug { "Get knowledge $jsonText" }


        val prompt = promptComponent.collectSystemPrompt(
            dialogMetaInfoEntityService.getDialogMetaInfoById(dialogQueue.dialogId!!)
        )

        val dialogs = dialogQueueRepository.findAllByDialogIdOrderByCreatedAtDesc(dialogQueue.dialogId).map {
            it.payload + "\n"
        }.toString()

        val responseAi = openAISender.chatWithGPT(dialogs, prompt).awaitSingleOrNull()

        auditLogRepository.save(
            AuditLogEntity(
                userId = dialogQueue.userId,
                chatId = dialogQueue.chatId,
//                sessionId = sessionId,
                source = "AI",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = responseAi
                // id/createdAt/updatedAt — оставляем на DEFAULT в БД
            )
        )

        dialogQueueRepository.save(
            DialogQueue(
                userId = dialogQueue.userId,
                chatId = dialogQueue.chatId,
                payload = responseAi,
                status = QueueStatus.ERROR,
                scheduledAt = Instant.now().plusSeconds(5),
//                dialogId     = UUID.randomUUID(),
                source = "ai",
                direction = Direction.INBOUND,
                role = RoleType.ASSISTANT,
                payloadType = PayloadType.TEXT,
            )
        )

        val answers: List<AnswerAI> = jacksonObjectMapper().readValue(
            responseAi,
            object : TypeReference<List<AnswerAI>>() {}
        )
        log.info { "DialogService answers $answers" }

        var fullAnswer = ""

        for (answer in answers) {
            fullAnswer += answer.answer

            if (answer.sql != null && answer.sql != "") {

                if (!answerAiGuard.sqlValidate(answer)) {
                    try {
                        val rawSqlServiceResult = rawSqlService.execute(answer.sql)
                        log.debug { "rawSqlServiceResult: $rawSqlServiceResult" }
                    } catch (e: Exception) {
                        log.error(e) { "Ошибка при выполнении SQL" }
                    }
                } else {
                    log.debug { "answerAiGuard.sqlValidat false by ${answer.sql}" }
                }
            }

            if (answer.action == AnswerAIType.SQL_FOR_AI) {
                dialogQueueRepository.save(
                    DialogQueue(
                        userId = dialogQueue.userId,
                        chatId = dialogQueue.chatId,
                        payload = jacksonObjectMapper().writeValueAsString(answer),
                        status = QueueStatus.NEW,
                        scheduledAt = Instant.now().plusSeconds(5),
                        dialogId     = dialogQueue.dialogId,
                        source = "AI",
                        direction = Direction.INBOUND,
                        role = RoleType.ASSISTANT,
                        payloadType = PayloadType.TEXT,
//                stepKind     = "request",
//                nextStepHint = "llm_call,
//                actionType   = null,
//                createdAt    = Instant.now(),
//                updatedAt    = Instant.now()
                    )
                )
            }
        }

        if(fullAnswer.isNotBlank()) {
            telegramClient.sendMessage(dialogQueue.chatId, fullAnswer).awaitSingleOrNull()
        }

        log.debug { "fullAnswer $fullAnswer" }


    }


}