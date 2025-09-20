package ru.ai.assistant.db

import org.springframework.beans.factory.annotation.Autowired
import ru.ai.assistant.BaseIT

class RawSqlServiceIT: BaseIT() {

    @Autowired
    private lateinit var rawSqlService: RawSqlService


    init {

        should("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'") {

            val query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"

            val response = rawSqlService.execute(query)

            println(response)

        }

    }

}