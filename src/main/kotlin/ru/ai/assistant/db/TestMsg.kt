package ru.ai.assistant.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("test_msg")
data class TestMsg(
    @Id val id: Long? = null,
    @Column("chat_id") val chatId: Long,
    @Column("user_id") val userId: Long,
    @Column("text") val text: String,
    @Column("created_at") val createdAt: OffsetDateTime? = null
)