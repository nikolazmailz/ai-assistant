package ru.ai.assistant.domain.metainfo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Сущность диалога ассистента.
 *
 * Хранит метаданные диалога:
 * - id: генерируется в БД (DEFAULT gen_random_uuid())
 * - title: короткое имя
 * - description: описание
 * - isActive: флаг активности
 * - createdAt / updatedAt: таймстемпы из БД (DEFAULT now())
 *
 * Вставка: id/createdAt/updatedAt можно не заполнять — их выставит Postgres.
 */
@Table("dialog_metainfo")
data class DialogMetaInfoEntity(
    @Id
    @Column("id")
    val id: UUID? = null,

    @Column("title")
    var title: String? = null,

    @Column("description")
    var description: String? = null,

    @Column("user_id")
    val userId: Long, // Telegram ID пользователя

    @Column("is_active")
    var isActive: Boolean = true,

    @Column("created_at")
    val createdAt: OffsetDateTime? = null,

    @Column("updated_at")
    var updatedAt: OffsetDateTime? = null,

    // todo add migrate
    var levelOfResponseCompleteness: LevelOfResponseCompleteness? = LevelOfResponseCompleteness.MEDIUM

    // todo currentTime добавить в DialogMetaInfoEntity??
) {
    companion object {
        fun create(userId: Long) : DialogMetaInfoEntity =
            DialogMetaInfoEntity(
                userId = userId,
                isActive = true
            )
    }
}