//package ru.ai.assistant.config.security
//
//import org.slf4j.LoggerFactory
//import org.springframework.http.HttpMethod
//import org.springframework.http.HttpStatus
//import org.springframework.stereotype.Component
//import org.springframework.web.server.ServerWebExchange
//import org.springframework.web.server.WebFilter
//import org.springframework.web.server.WebFilterChain
//import reactor.core.publisher.Mono
//import ru.ai.assistant.config.TelegramProperties
//import java.security.MessageDigest
//
//@Component
//class WebhookSecretFilter(
//    private val props: TelegramProperties
//) : WebFilter {
//
//    private val log = LoggerFactory.getLogger(javaClass)
//    private val headerName = "X-Telegram-Bot-Api-Secret-Token"
//    private val path = "/tg/webhook"
//
//
//    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
//        val expected = props.webhookSecret?.takeIf { it.isNotBlank() } ?: return chain.filter(exchange)
//
//        val req = exchange.request
//        if (req.method != HttpMethod.POST || req.uri.path != path) {
//            return chain.filter(exchange)
//        }
//
//        val actual = req.headers.getFirst(headerName) ?: ""
//        if (!constantTimeEquals(actual, expected)) {
//            log.warn("Webhook secret mismatch for {}", path)
//            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
//            return exchange.response.setComplete()
//        }
//        return chain.filter(exchange)
//    }
//
//    private fun constantTimeEquals(a: String, b: String): Boolean =
//        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
//
//}