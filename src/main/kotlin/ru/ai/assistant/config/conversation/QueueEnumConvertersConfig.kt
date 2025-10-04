package ru.ai.assistant.config.conversation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import ru.ai.assistant.domain.Direction
import ru.ai.assistant.domain.PayloadType
import ru.ai.assistant.domain.QueueStatus
import ru.ai.assistant.domain.RoleType

@Configuration
class QueueEnumConvertersConfig {

    @Bean fun queueStatusRead(): Converter<String, QueueStatus> = StringToQueueStatus
    @Bean fun queueStatusWrite(): Converter<QueueStatus, String> = QueueStatusToString

    @Bean fun directionRead(): Converter<String, Direction> = StringToDirection
    @Bean fun directionWrite(): Converter<Direction, String> = DirectionToString

    @Bean fun roleTypeRead(): Converter<String, RoleType> = StringToRoleType
    @Bean fun roleTypeWrite(): Converter<RoleType, String> = RoleTypeToString

    @Bean fun payloadTypeRead(): Converter<String, PayloadType> = StringToPayloadType
    @Bean fun payloadTypeWrite(): Converter<PayloadType, String> = PayloadTypeToString
}

@ReadingConverter object StringToQueueStatus : Converter<String, QueueStatus> {
    override fun convert(source: String) = QueueStatus.valueOf(source.uppercase())
}
@WritingConverter object QueueStatusToString : Converter<QueueStatus, String> {
    override fun convert(source: QueueStatus) = source.name.lowercase()
}

@ReadingConverter object StringToDirection : Converter<String, Direction> {
    override fun convert(source: String) = Direction.valueOf(source.uppercase())
}
@WritingConverter object DirectionToString : Converter<Direction, String> {
    override fun convert(source: Direction) = source.name.lowercase()
}

@ReadingConverter object StringToRoleType : Converter<String, RoleType> {
    override fun convert(source: String) = RoleType.valueOf(source.uppercase())
}
@WritingConverter object RoleTypeToString : Converter<RoleType, String> {
    override fun convert(source: RoleType) = source.name.lowercase()
}

@ReadingConverter object StringToPayloadType : Converter<String, PayloadType> {
    override fun convert(source: String) = PayloadType.valueOf(source.uppercase())
}
@WritingConverter object PayloadTypeToString : Converter<PayloadType, String> {
    override fun convert(source: PayloadType) = source.name.lowercase()
}