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

//    suspend fun executeSmart(sql: String): Any {
//        return try {
//            val rows = template.databaseClient
//                .sql(sql)
//                .map { row, meta ->
//                    val result = linkedMapOf<String, Any?>()
//                    meta.columnMetadatas.forEachIndexed { i, cmd ->
//                        result[cmd.name] = row.get(i)
//                    }
//                    result
//                }
//                .all()
//                .collectList()
//                .awaitSingle()
//
//            if (rows.isNotEmpty()) {
//                mapOf("type" to "rows", "rows" to rows)
//            } else {
//                val updated = template.databaseClient.sql(sql).fetch().rowsUpdated().awaitSingle()
//                mapOf("type" to "rowsAffected", "rowsAffected" to updated)
//            }
//        } catch (e: Exception) {
//            mapOf("type" to "error", "message" to e.message)
//        }
//    }


    suspend fun executeSmart(sqlRaw: String): Any {
        // 1) срежем ; и пробелы
        val sql = sqlRaw.trim().trimEnd(';').trim()
        val first = sql.takeWhile { !it.isWhitespace() }.uppercase()

        return try {
            when (first) {
                "SELECT", "WITH", "VALUES", "SHOW", "EXPLAIN" -> {
                    val rows = template.databaseClient
                        .sql(sql)
                        .map { row, meta ->
                            buildMap<String, Any?> {
                                meta.columnMetadatas.forEachIndexed { i, cmd ->
                                    this[cmd.name] = row.get(i)
                                }
                            }
                        }
                        .all()
                        .collectList()
                        .awaitSingle()

                    if (rows.isNotEmpty())
                        mapOf("type" to "rows", "rows" to rows)
                    else
                        mapOf("type" to "rows", "rows" to emptyList<Map<String, Any?>>())
                }
                else -> {
                    // DDL/DML: CREATE / ALTER / DROP / INSERT / UPDATE / DELETE / MERGE …
                    val updated = template.databaseClient
                        .sql(sql)
                        .fetch()
                        .rowsUpdated()
                        .awaitSingle()

                    mapOf("type" to "rowsAffected", "rowsAffected" to updated)
                }
            }
        } catch (e: Exception) {
            // Полезно логировать первопричину
            mapOf("type" to "error", "message" to (e.cause?.message ?: e.message))
        }
    }
}
