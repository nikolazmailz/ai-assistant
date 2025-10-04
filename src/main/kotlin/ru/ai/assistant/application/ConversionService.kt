package ru.ai.assistant.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import ru.ai.assistant.domain.ConversationQueueEntity
import com.fasterxml.jackson.core.type.TypeReference
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.ai.assistant.infra.TelegramClient

@Service
class ConversionService(
    private val openAIService: OpenAIService,
    private val telegramClient: TelegramClient,
) {

    private val log = KotlinLogging.logger {}

    suspend fun handleMsg(item: ConversationQueueEntity) {

        // todo UseCase

        /**
         * Получить текущее время
         * Получить предпомнт
         * Сформировать истории диалога todo
         * Дополнить к сообщение запрос
         * Отправить
         * Получить ответ, сохранить его в лог
         * 1к - Что-то сделать и ответить пользователю
         * 2к - Что-то сделать и сделать новую запись в ConversationQueueRepository
         *
         * */
        val response = openAIService.chatWithGPT(item.payload!!).awaitSingleOrNull()

        val answers: List<AnswerAI> = jacksonObjectMapper().readValue(
            response,
            object : TypeReference<List<AnswerAI>>() {}
        )
        log.info { "ConversionService answers $answers" }


        telegramClient.sendMessage(item.chatId, response!!)

    }


}