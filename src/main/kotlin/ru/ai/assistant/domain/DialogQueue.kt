package ru.ai.assistant.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Очередь шагов обработки диалога. Содержит состояния сообщений и действий, выполняемых по цепочке LLM.
 */
@Table("dialog_queue")
data class DialogQueue(

    @Id
    @Column("id")
    val id: UUID? = null, // UUID — уникальный идентификатор шага в очереди.



    @Column("user_id")
    val userId: Long, // Telegram ID пользователя

    @Column("chat_id")
    val chatId: Long, // Telegram Chat ID (куда возвращать ответ)

    @Column("dialog_id")
    val dialogId: UUID? = null, // логическая сессия

    @Column("dialog_title")
    val dialogTitle: String? = null, // логическая сессия

    @Column("status")
    val status: QueueStatus = QueueStatus.NEW, // new/processing/waiting/ready/success/error

    @Column("payload")
    val payload: String? = null, // содержимое шага (текст или JSON)

    @Column("scheduled_at")
    val scheduledAt: Instant = Instant.now(), // плановое время исполнения (NOT NULL)

    @Column("source")
    val source: SourceDialogType = SourceDialogType.TELEGRAM, // источник: telegram/web/api

    @Column("role")
    val role: RoleType = RoleType.USER, // user/assistant/system



    @Column("created_at")
    val createdAt: Instant = Instant.now(), // NOT NULL

    @Column("updated_at")
    val updatedAt: Instant = Instant.now() // NOT NULL
) {

    fun dialogId() = this.dialogId!!
}

enum class QueueStatus { NEW, PROCESSING, WAITING, READY, SUCCESS, ERROR }
enum class RoleType { USER, ASSISTANT, SYSTEM }
enum class SourceDialogType { TELEGRAM, AI }