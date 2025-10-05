package ru.ai.assistant.domain.systemprompt

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.UUID

@Table("system_promnt")
data class SystemPromnt(
    @Id
    val id: UUID? = null,

    val name: String,

    val description: String? = null,

    val content: String,

    @Column("created_at")
    val createdAt: OffsetDateTime? = null,

    @Column("is_active")
    val isActive: Boolean = false
)