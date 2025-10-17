package ru.ai.assistant.domain.systemprompt

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ru.ai.assistant.db.RawSqlService
import ru.ai.assistant.db.SqlScript
import ru.ai.assistant.domain.metainfo.DialogMetaInfoEntity
import java.time.OffsetDateTime

/**
 * Сервис который должен собрать системный промпт
 *
 * Системный промпт должен включать в себя:
 * 1. DialogInfo - данные о диалоге
 * 2. Ты — полезный AI-ассистент.
 * 3. Уровень объема ответа - LevelOfResponseCompleteness (dialogInfo.levelOfResponseCompleteness)
 * 4. System Prompt
 * 5. Твои знания: $knowledge
 *
 * */
@Component
class PromptComponent(
    private val systemPromptRepository: SystemPromptRepository,
    private val rawSqlService: RawSqlService,
) {

    private val log = KotlinLogging.logger {}

    suspend fun collectSystemPrompt(dialogInfo: DialogMetaInfoEntity): String {

        val dialogInfoPrompt = createDialogInfoPrompt(dialogInfo)
        val globalPrompt = systemPromptRepository.findFirstByIsActiveTrue()!!
        val globalPromptContent = globalPrompt.content
        val knowledge = rawSqlService.execute(SqlScript.QUERY_ALL_DATA)

        return "$dialogInfoPrompt \n " +
                "$WHO_AMI \n " +
                "${dialogInfo.levelOfResponseCompleteness?.levelPrompt} \n " +
                "$globalPromptContent \n Твои знания: $knowledge \n" +
                "".let {
                    log.debug { "\n\n collectSystemPrompt \n $it \n\n" }
                    it
                }
    }

    private fun createDialogInfoPrompt(dialogInfo: DialogMetaInfoEntity): String = """ 
            {
              "userId": "${dialogInfo.userId}",
              "dialogId": "${dialogInfo.id}",
              "dialogName": "${dialogInfo.title}",
              "currentTime": "${OffsetDateTime.now()}"
            }""".trim()


    companion object {
        private const val WHO_AMI = "Ты — полезный AI-ассистент."
    }

}