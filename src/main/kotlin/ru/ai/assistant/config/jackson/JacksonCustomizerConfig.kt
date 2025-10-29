package ru.ai.assistant.config.jackson


import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonCustomizerConfig {
    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.modulesToInstall(JavaTimeModule())
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
}