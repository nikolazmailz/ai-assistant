package ru.ai.assistant.db

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import io.r2dbc.postgresql.api.PostgresqlException
import org.springframework.r2dbc.BadSqlGrammarException
import org.springframework.r2dbc.connection.ConnectionFactoryUtils
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import reactor.core.Exceptions

@Service
class RawSqlService(
    private val template: R2dbcEntityTemplate,
    private val txManager: org.springframework.r2dbc.connection.R2dbcTransactionManager
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


    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
//            mapOf("type" to "error", "message" to (e.cause?.message ?: e.message))
            log.error(e) { "Ошибка при executeSmart SQL" }
            toErrorPayload(e)
        }
    }


    private fun toErrorPayload(t: Throwable): Map<String, Any?> {
        val e = Exceptions.unwrap(t)
        return when (e) {
            is PostgresqlException -> mapOf(
                "type" to "db_error",
                "sqlState" to sqlStateOf(e),          // например "23505"
                "message" to e.message,            // общая
                "detail" to e.errorDetails.detail, // DETAIL
                "hint" to e.errorDetails.hint,     // HINT
                "where" to e.errorDetails.where,   // WHERE
                "schema" to e.errorDetails.schemaName,
                "table" to e.errorDetails.tableName,
                "column" to e.errorDetails.columnName,
                "constraint" to e.errorDetails.constraintName,
                "position" to e.errorDetails.position
            )
            is R2dbcDataIntegrityViolationException -> mapOf(
                "type" to "integrity_violation",
                "sqlState" to e.sqlState,
                "message" to e.message
            )
            is BadSqlGrammarException -> mapOf(
                "type" to "bad_sql",
                "sqlState" to sqlStateOf(e),
                "message" to e.message
            )
            else -> mapOf("type" to "error", "message" to (e.message ?: t.message))
        }
    }

    private fun sqlStateOf(any: Any): String? {
        // 1) Попробуем метод getSqlState()
        runCatching { any.javaClass.getMethod("getSqlState").invoke(any) as? String }.getOrNull()?.let { return it }
        // 2) Попробуем публичное/приватное поле sqlState
        return runCatching {
            any.javaClass.getDeclaredField("sqlState").apply { isAccessible = true }.get(any) as? String
        }.getOrNull()
    }

    private val log = KotlinLogging.logger {}
}
