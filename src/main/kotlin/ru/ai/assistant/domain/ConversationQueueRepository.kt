package ru.ai.assistant.domain

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ConversationQueueRepository : CoroutineCrudRepository<ConversationQueueEntity, UUID> {

    fun findAllByDialogIdOrderByCreatedAtAsc(dialogId: UUID): Flow<ConversationQueueEntity>
    fun findAllByChatIdOrderByCreatedAtAsc(chatId: Long): Flow<ConversationQueueEntity>

    @Query(
        """
        SELECT * FROM conversation_queue
        WHERE status IN ('new','ready')
          AND scheduled_at <= now()
        ORDER BY scheduled_at ASC
        LIMIT :limit
    """
    )
    fun findReady(limit: Int): Flow<ConversationQueueEntity>

    @Modifying
    @Query(
        """
        UPDATE conversation_queue
        SET status = CAST(:status AS queue_status),
            updated_at = now()
        WHERE id = :id
    """
    )
    suspend fun updateStatus(id: UUID, status: QueueStatus): Int

//    @Query(
//        """
//        WITH cte AS (
//            SELECT id
//            FROM conversation_queue
//            WHERE status IN ('new','ready')
//              AND scheduled_at <= now()
//            ORDER BY scheduled_at ASC
//            LIMIT :batch
//            FOR UPDATE SKIP LOCKED
//        ),
//        upd AS (
//            UPDATE conversation_queue q
//            SET status = 'processing',
//                updated_at = now()
//            WHERE q.id IN (SELECT id FROM cte)
//            RETURNING q.*
//        )
//        SELECT *
//        FROM upd
//        ORDER BY scheduled_at ASC
//        """
//    )
//    fun pickBatchForProcessing(batch: Int): Flow<ConversationQueueEntity>

    @Query(
        """
        WITH cte AS (
            SELECT id
            FROM conversation_queue
            WHERE status IN ('new','ready')
              AND scheduled_at <= now()
            ORDER BY scheduled_at ASC
            LIMIT :batch
            FOR UPDATE SKIP LOCKED
        )
            UPDATE conversation_queue q
            SET status = 'processing', updated_at = now()
            WHERE q.id IN (SELECT id FROM cte)
            RETURNING q.*

        """
    )
    fun pickBatchForProcessing(batch: Int): Flow<ConversationQueueEntity>

    @Modifying
    @Query("UPDATE conversation_queue SET status = 'success', updated_at = now() WHERE id = :id")
    suspend fun markSuccess(id: UUID): Int

    @Modifying
    @Query("UPDATE conversation_queue SET status = 'error',   updated_at = now() WHERE id = :id")
    suspend fun markError(id: UUID): Int
}