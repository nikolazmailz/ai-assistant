package ru.ai.assistant.domain

/**
 * Упрощённая модель апдейта Telegram для MVP.
 * Покрывает только текстовые сообщения.
 */
data class TelegramUpdate(
    val update_id: Long,
    val message: TelegramMessage?
)

data class TelegramMessage(
    val message_id: Long,
    val from: TelegramUser?,
    val chat: TelegramChat,
    val text: String?
)

data class TelegramUser(
    val id: Long,
    val first_name: String? = null,
    val username: String? = null
)

data class TelegramChat(
    val id: Long,
    val type: String
)
