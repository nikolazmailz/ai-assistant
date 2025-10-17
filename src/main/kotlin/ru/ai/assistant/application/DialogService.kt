package ru.ai.assistant.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import ru.ai.assistant.domain.DialogQueue
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.map
import ru.ai.assistant.application.dto.AnswerAI
import ru.ai.assistant.application.dto.AnswerAIType
import ru.ai.assistant.application.metainfo.DialogMetaInfoEntityService
import ru.ai.assistant.application.openai.AISender
import ru.ai.assistant.db.RawSqlService
import ru.ai.assistant.domain.DialogQueueRepository
import ru.ai.assistant.domain.QueueStatus
import ru.ai.assistant.domain.RoleType
import ru.ai.assistant.domain.audit.AuditLogEntity
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.domain.audit.PayloadTypeLog
import ru.ai.assistant.infra.TelegramClient
import ru.ai.assistant.application.security.sql.AnswerAiGuard
import ru.ai.assistant.domain.SourceDialogType
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

        auditLogRepository.save(
            AuditLogEntity(
                userId = dialogQueue.userId,
                chatId = dialogQueue.chatId,
                source = "System",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = jacksonObjectMapper().writeValueAsString(dialogs)
            )
        )

        val responseAi = openAISender.chatWithGPT(dialogs, prompt).awaitSingleOrNull()

        val parseAiContent = parseAiContent(responseAi!!)

        auditLogRepository.save(
            AuditLogEntity(
                userId = dialogQueue.userId,
                chatId = dialogQueue.chatId,
//                sessionId = sessionId,
                source = "AI",
                payloadTypeLog = PayloadTypeLog.TEXT,
                payload = jacksonObjectMapper().writeValueAsString(parseAiContent)
                // id/createdAt/updatedAt — оставляем на DEFAULT в БД
            )
        )


//        val answers: List<AnswerAI> = jacksonObjectMapper().readValue(
//            responseAi,
//            object : TypeReference<List<AnswerAI>>() {}
//        )
//        log.info { "DialogService answers $answers" }

        var fullAnswer = ""

        for (answer in parseAiContent) {
            fullAnswer += answer.answer

            if (answer.sql != null && answer.sql != "") {

                if (answerAiGuard.sqlValidate(answer)) {
                    try {
                        val rawSqlServiceResult = rawSqlService.execute(answer.sql)
                        log.debug { "rawSqlServiceResult: $rawSqlServiceResult" }
                    } catch (e: Exception) {

                        dialogQueueRepository.save(
                            DialogQueue(
                                userId = dialogQueue.userId,
                                chatId = dialogQueue.chatId,
                                dialogId = dialogQueue.dialogId,
                                status = QueueStatus.ERROR,
                                payload = jacksonObjectMapper().writeValueAsString(answer),
//                                scheduledAt = Instant.now().plusSeconds(5),
                                source = SourceDialogType.AI,
                                role = RoleType.ASSISTANT,
                            )
                        )

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
                        dialogId = dialogQueue.dialogId,
                        status = QueueStatus.NEW,
                        payload = jacksonObjectMapper().writeValueAsString(answer),
                        scheduledAt = Instant.now().plusSeconds(5),
                        source = SourceDialogType.AI,
                        role = RoleType.ASSISTANT
                    )
                )
            }
        }

        if(fullAnswer.isNotBlank()) {
            telegramClient.sendMessage(dialogQueue.chatId, fullAnswer).awaitSingleOrNull()
        }

        log.debug { "fullAnswer $fullAnswer" }

    }

    fun parseAiContent(responseAi: String): List<AnswerAI> {
        // срезаем ```json ... ``` или ``` ... ```
        val cleaned = Regex("^```(?:json)?\\s*|\\s*```$", RegexOption.MULTILINE)
            .replace(responseAi.trim(), "")
            .trim()

        val mapper = jacksonObjectMapper().findAndRegisterModules()
        return mapper.readValue(cleaned)
    }
}