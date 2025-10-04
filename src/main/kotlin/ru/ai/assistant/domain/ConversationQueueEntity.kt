package ru.ai.assistant.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Очередь шагов обработки диалога. Содержит состояния сообщений и действий, выполняемых по цепочке LLM.
 */
@Table("conversation_queue")
data class ConversationQueueEntity(

    @Id
    @Column("id")
    val id: UUID? = null, // UUID — уникальный идентификатор шага в очереди.

    @Column("user_id")
    val userId: Long, // Telegram ID пользователя

    @Column("chat_id")
    val chatId: Long, // Telegram Chat ID (куда возвращать ответ)

    @Column("payload")
    val payload: String? = null, // содержимое шага (текст или JSON)

    @Column("status")
    val status: QueueStatus = QueueStatus.NEW, // new/processing/waiting/ready/success/error

    @Column("scheduled_at")
    val scheduledAt: Instant = Instant.now(), // плановое время исполнения (NOT NULL)

    @Column("dialog_id")
    val dialogId: UUID? = null, // логическая сессия

    @Column("source")
    val source: String = "telegram", // источник: telegram/web/api

    @Column("direction")
    val direction: Direction = Direction.INBOUND, // inbound/outbound

    @Column("role")
    val role: RoleType = RoleType.USER, // user/assistant/system

    @Column("payload_type")
    val payloadType: PayloadType = PayloadType.TEXT, // text/voice/photo/document/unknown

    @Column("step_kind")
    val stepKind: StepKind = StepKind.REQUEST, // request/response/REPLYTOAI

    @Column("next_step_hint")
    val nextStepHint: String? = null, // подсказка следующего шага

    @Column("action_type")
    val actionType: String? = null, // send_reply/fetch_calendar/run_sql/...

    @Column("created_at")
    val createdAt: Instant = Instant.now(), // NOT NULL

    @Column("updated_at")
    val updatedAt: Instant = Instant.now() // NOT NULL
)

enum class QueueStatus { NEW, PROCESSING, WAITING, READY, SUCCESS, ERROR }
enum class Direction { INBOUND, OUTBOUND }
enum class RoleType { USER, ASSISTANT, SYSTEM }
enum class PayloadType { TEXT, VOICE, PHOTO, DOCUMENT, UNKNOWN }
enum class StepKind { REQUEST, REPLY_TO_AI}