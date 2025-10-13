//package ru.ai.assistant.application
//
//import io.github.oshai.kotlinlogging.KotlinLogging
//import kotlinx.coroutines.reactor.awaitSingleOrNull
//import org.springframework.stereotype.Service
//import ru.ai.assistant.application.openai.OpenAISender
//import ru.ai.assistant.db.RawSqlService
//import ru.ai.assistant.db.SqlScript
//
//@Service
//class OpenAIService(
//    private val rawSqlService: RawSqlService,
//    private val openAISender: OpenAISender,
//) {
//
//    private val log = KotlinLogging.logger {}
//
//    suspend fun chatWithGPT(prompt: String, knowledge: String): String? {
//        val knowledge = rawSqlService.execute(SqlScript.QUERY_ALL_DATA)
//
//        val jsonText = knowledge.first()["tables"] as String
//
//        log.debug { "Get knowledge $jsonText" }
//
//        val request = mapOf(
//            "model" to "gpt-3.5-turbo",
//            "messages" to listOf(
//                mapOf("role" to "system", "content" to " $knowledge"),
//                mapOf("role" to "user", "content" to prompt)
//            )
//        )
//
//        return openAISender.chatWithGPT(request).awaitSingleOrNull()
//    }
//}