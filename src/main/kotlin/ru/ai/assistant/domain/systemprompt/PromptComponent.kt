package ru.ai.assistant.domain.systemprompt

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ru.ai.assistant.domain.metainfo.DialogMetaInfoEntity
import java.time.OffsetDateTime

/**
 * Сервис который должен собрать системный промпт
 *
 * Системный промпт должен включать в себя:
 * 1. DialogInfo - данные о диалоге
 * 2. Ты — полезный AI-ассистент.
 * 3. Уровень объема ответа - LevelOfResponseCompleteness
 * 4. Твои знания: $knowledge
 *
 * */
@Component
class PromptComponent(
    private val systemPromptRepository: SystemPromptRepository,
) {


    suspend fun collectSystemPrompt(dialogInfo: DialogMetaInfoEntity): Mono<String> {

        val dialogInfoPrompt =  createDialogInfoPrompt(dialogInfo)

//        val levelOfResponseCompleteness = dialogInfo.levelOfResponseCompleteness

        val globalPrompt = systemPromptRepository

        return Mono.just("todo")
    }

    private fun createDialogInfoPrompt(dialogInfo: DialogMetaInfoEntity): String = """ 
            {
              "userId": "${dialogInfo.userId}",
              "dialogId": "${dialogInfo.id}",
              "dialogName": "${dialogInfo.title}",
              "currentTime": "${OffsetDateTime.now()}"
            }""".trim()


    companion object {




    }

}