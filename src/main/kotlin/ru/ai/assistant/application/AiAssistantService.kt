package ru.ai.assistant.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.ai.assistant.db.RawSqlService
import ru.ai.assistant.db.TestMsg
import ru.ai.assistant.db.TestMsgRepository
import ru.ai.assistant.domain.TelegramUpdate
import ru.ai.assistant.infra.TelegramClient
import java.time.OffsetDateTime

@Service
class AiAssistantService(
    private val LLMService: LLMService,
    private val telegramClient: TelegramClient,
    private val objectMapper: ObjectMapper,
    private val testMsgRepository: TestMsgRepository,
    private val rawSqlService: RawSqlService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun handleMessage(update: TelegramUpdate) {

        if(update.message == null || update.message.text == null) {
            return
        }

        log.debug("update.message?.text!! ${update.message?.text!!}")

        mono {
            testMsgRepository.save(
                TestMsg(
                    chatId = update.message.message_id,
                    userId = update.message.from?.id!!,
                    text = update.message.text,
                    createdAt = OffsetDateTime.now(),
                )
            )
        }.subscribe()

//        LLMService.chatWithGPT(update.message.text).map {
//
//            mono {
//                testMsgRepository.save(
//                    TestMsg(
//                        chatId = update.message.message_id,
//                        userId = update.message.from?.id!!,
//                        text = it,
//                        createdAt = OffsetDateTime.now(),
//                    )
//                )
//            }.subscribe()
//
//            val answerAI: AnswerAI = objectMapper.readValue<AnswerAI>(it)
//
//            mono {
//                val rawSqlServiceResult = rawSqlService.execute(answerAI.sql)
//                log.debug("rawSqlServiceResult $rawSqlServiceResult")
//            }.subscribe()
//
//            telegramClient.sendMessage(
//                update.message.chat.id, it
//            )
//
//        }
    }
}