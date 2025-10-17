package ru.ai.assistant.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import ru.ai.assistant.domain.DialogQueue
import com.fasterxml.jackson.core.type.TypeReference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.toList
import ru.ai.assistant.application.audit.AuditService
import ru.ai.assistant.application.dto.AnswerAI
import ru.ai.assistant.application.metainfo.DialogMetaInfoEntityService
import ru.ai.assistant.application.openai.AISender
import ru.ai.assistant.db.RawSqlService
import ru.ai.assistant.domain.DialogQueueRepository
import ru.ai.assistant.domain.QueueStatus
import ru.ai.assistant.domain.RoleType
import ru.ai.assistant.domain.audit.AuditLogRepository
import ru.ai.assistant.infra.TelegramClient
import ru.ai.assistant.application.security.sql.AnswerAiGuard
import ru.ai.assistant.domain.SourceDialogType
import ru.ai.assistant.domain.TelegramUpdate
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
    private val auditService: AuditService,
) {

    private val log = KotlinLogging.logger {}

    suspend fun handleMessage(update: TelegramUpdate) {
        val message = update.message ?: return
        val payload = message.text ?: return

        log.debug { "update.message.text: $payload" }

        auditService.logUserText(message, payload = payload)

        val dialogMetaInfo = dialogMetaInfoEntityService.getOrCreateDialogMetaInfo(userId = message.from!!.id).let {
            if (it.title == null) {
                val title = openAISender.defineTitleDialog(payload).awaitSingleOrNull()
                log.debug { "defineTitleDialog $title" }
                dialogMetaInfoEntityService.setTitle(it.id!!, title!!)
                it.title = title
            }
            it
        }

        dialogQueueRepository.save(
            DialogQueue(
                userId = message.from.id,
                chatId = message.chat.id,
                dialogId = dialogMetaInfo.id,
                dialogTitle = dialogMetaInfo.title,
                status = QueueStatus.NEW,
                payload = payload,
                scheduledAt = Instant.now().plusSeconds(5),
                source = SourceDialogType.TELEGRAM,
                role = RoleType.USER,
            )
        )
    }

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
    suspend fun handlePollMsg(dialogQueue: DialogQueue) {

        val dialogs = mutableListOf<Map<String, String>>()

        dialogQueueRepository.findAllByDialogIdOrderByCreatedAtAsc(dialogQueue.dialogId())
            .toList().forEach {
                dialogs.add(mapOf("role" to it.role.name.lowercase(), "content" to it.payload!!))
            }

        auditService.logDialogQueueHistory(dialogQueue.userId, dialogQueue.chatId, dialogs)

        val systemPrompt = promptComponent.collectSystemPrompt(
            dialogMetaInfoEntityService.getDialogMetaInfoById(dialogQueue.dialogId())
        )
        dialogs.addFirst(mapOf("role" to "system", "content" to systemPrompt))

        val responseAi = openAISender.chatWithGPT(dialogs, systemPrompt).awaitSingleOrNull()

        auditService.logAnswersAi(dialogQueue.userId, dialogQueue.chatId, responseAi)

        val answers: List<AnswerAI> = jacksonObjectMapper().readValue(
            responseAi,
            object : TypeReference<List<AnswerAI>>() {}
        )



        var collectAnswer = ""
        var collectResultSql = ""

        for (answer in answers) {
            collectAnswer += answer.answer

            if (answer.sql != null && answer.sql != "") {
                if (answerAiGuard.sqlValidate(answer)) {
                    try {
                        val rawSqlServiceResult = rawSqlService.executeSmart(answer.sql)
                        log.debug { "rawSqlServiceResult: $rawSqlServiceResult" }
                        collectResultSql += "${jacksonObjectMapper().writeValueAsString(rawSqlServiceResult)} \n"
                    } catch (e: Exception) {
                        dialogQueueRepository.save(
                            DialogQueue(
                                userId = dialogQueue.userId,
                                chatId = dialogQueue.chatId,
                                dialogId = dialogQueue.dialogId,
                                status = QueueStatus.ERROR,
                                payload = jacksonObjectMapper().writeValueAsString(answer),
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
        }

        if (collectAnswer.isNotBlank()) {
            dialogQueueRepository.save(
                DialogQueue(
                    userId = dialogQueue.userId,
                    chatId = dialogQueue.chatId,
                    dialogId = dialogQueue.dialogId,
                    dialogTitle = dialogQueue.dialogTitle,
                    status = QueueStatus.SUCCESS,
                    payload = "answer $collectAnswer",
                    scheduledAt = Instant.now().plusSeconds(5),
                    source = SourceDialogType.AI,
                    role = RoleType.ASSISTANT
                )
            )
            telegramClient.sendMessage(dialogQueue.chatId, collectAnswer).awaitSingleOrNull()
        }

        if (collectResultSql.isNotBlank()) {
            dialogQueueRepository.save(
                DialogQueue(
                    userId = dialogQueue.userId,
                    chatId = dialogQueue.chatId,
                    dialogId = dialogQueue.dialogId,
                    dialogTitle = dialogQueue.dialogTitle,
                    status = QueueStatus.NEW,
                    payload = "SQL_FOR_AI result $collectResultSql",
                    scheduledAt = Instant.now().plusSeconds(5),
                    source = SourceDialogType.AI,
                    role = RoleType.ASSISTANT
                )
            )
        }

//        log.debug { "fullAnswer $fullAnswer" }

    }

}