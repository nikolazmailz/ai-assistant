package ru.ai.assistant.domain.audit

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Модель для таблицы audit_log.
 * Значения created_at/updated_at оставляем на стороне БД (DEFAULT now()).
 */
@Table("audit_log")
data class AuditLogEntity(
    @Id
    @Column("id")
    val id: UUID? = null,

    @Column("user_id")
    val userId: Long,

    @Column("chat_id")
    val chatId: Long,

    @Column("session_id")
    val sessionId: UUID? = null,

    @Column("source")
    val source: String = "telegram",

    @Column("payload_type_log")
    val payloadTypeLog: PayloadTypeLog = PayloadTypeLog.TEXT,

    @Column("payload")
    val payload: String? = null,

    @Column("created_at")
    val createdAt: Instant? = null,

    @Column("updated_at")
    val updatedAt: Instant? = null
)