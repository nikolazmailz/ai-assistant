package ru.ai.assistant.db

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import kotlinx.coroutines.flow.Flow

@Repository
interface TestMsgRepository : CoroutineCrudRepository<TestMsg, Long> {
    fun findByChatIdOrderByCreatedAtDesc(chatId: Long): Flow<TestMsg>
}