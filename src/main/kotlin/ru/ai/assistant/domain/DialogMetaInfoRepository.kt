package ru.ai.assistant.domain

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface DialogMetaInfoRepository : CoroutineCrudRepository<DialogMetaInfoEntity, UUID> {

    /** Все активные диалоги конкретного пользователя. */
    fun findAllByUserIdAndIsActiveTrue(userId: Long): Flow<DialogMetaInfoEntity>

    /** Поиск по точному названию у конкретного пользователя. */
    suspend fun findByUserIdAndTitle(userId: Long, title: String): DialogMetaInfoEntity?
}