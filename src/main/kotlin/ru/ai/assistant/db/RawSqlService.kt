package ru.ai.assistant.db

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service

@Service
class RawSqlService(
    private val template: R2dbcEntityTemplate
) {

    suspend fun execute(sql: String): List<Map<String, Any?>> {
        return template.databaseClient
            .sql(sql)
            .map { row, meta ->
                val result = linkedMapOf<String, Any?>()
                // RowMetadata.getColumnMetadatas() -> Iterable<ColumnMetadata>
                var i = 0
                for (cmd in meta.getColumnMetadatas()) {
                    // Берём по индексу, чтобы НЕ ловить неоднозначность get(String)/get(Int)
                    result[cmd.name] = row.get(i)
                    i++
                }
                result
            }
            .all()
            .collectList()
            .awaitSingle()
    }
}
