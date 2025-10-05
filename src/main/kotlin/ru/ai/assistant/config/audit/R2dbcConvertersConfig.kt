package ru.ai.assistant.config.audit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import ru.ai.assistant.domain.audit.PayloadTypeLog

/**
 * Простые конвертеры String<->PayloadType для Spring Data R2DBC.
 * Boot автоматически подхватит их как beans.
 */
@Configuration
class R2dbcConvertersConfig {

    @Bean
    fun payloadTypeReadingConverter(): Converter<String, PayloadTypeLog> = StringToPayloadTypeLog

    @Bean
    fun payloadTypeWritingConverter(): Converter<PayloadTypeLog, String> = PayloadTypeLogToString
}

@ReadingConverter
object StringToPayloadTypeLog : Converter<String, PayloadTypeLog> {
    override fun convert(source: String): PayloadTypeLog = PayloadTypeLog.fromDb(source)
}

@WritingConverter
object PayloadTypeLogToString : Converter<PayloadTypeLog, String> {
    override fun convert(source: PayloadTypeLog): String = PayloadTypeLog.toDb(source)
}
