package ru.ai.assistant.jackson

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.StringSpec
import com.fasterxml.jackson.core.type.TypeReference
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import ru.ai.assistant.application.dto.AnswerAI

class JacksonDeserializationTest : StringSpec({

    val objectMapper = jacksonObjectMapper()

//    "должен корректно десериализовать список AnswerAI" {
//        val json = this::class.java.getResource("/data/jackson/request.json")!!.readText()
//
//
//        val result = objectMapper.readValue(
//            json,
//            object : TypeReference<List<AnswerAI>>() {}
//        )
//
//        result.size shouldBe 2
//        result[0] shouldNotBe null
////        result[0] shouldBe AnswerAI(1, "Hello")
//    }

//    "должен бросать исключение при некорректном JSON" {
//        val invalidJson = """
//            [
//              {"id": "abc", "text": "Invalid id"}
//            ]
//        """.trimIndent()
//
//        shouldThrow<JsonMappingException> {
//            objectMapper.readValue(
//                invalidJson,
//                object : TypeReference<List<AnswerAI>>() {}
//            )
//        }
//    }

    "должен вернуть пустой список при пустом JSON массиве" {
        val emptyJson = "[]"

        val result = objectMapper.readValue(
            emptyJson,
            object : TypeReference<List<AnswerAI>>() {}
        )

        result shouldBe emptyList()
    }
})
