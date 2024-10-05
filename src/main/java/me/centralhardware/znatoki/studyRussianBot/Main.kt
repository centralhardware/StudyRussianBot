package me.centralhardware.znatoki.studyRussianBot

import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import me.centralhardware.znatoki.studyRussianBot.telegram.TelegramBot
import java.net.InetSocketAddress

/**
 * init telegram ApiContext and telegram bot, statistic
 */
fun main() {
    HttpServer.create().apply { bind(InetSocketAddress(80), 0); createContext("/health") { it.sendResponseHeaders(200, 0); it.responseBody.close() }; start() }
    WordManager.init()
    TelegramBot.init()
    LoggerFactory.getLogger("main").info("telegram bot run")
}