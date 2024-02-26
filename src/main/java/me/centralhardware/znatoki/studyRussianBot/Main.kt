package me.centralhardware.znatoki.studyRussianBot

import org.slf4j.LoggerFactory
import me.centralhardware.znatoki.studyRussianBot.telegram.TelegramBot

/**
 * init telegram ApiContext and telegram bot, statistic
 */
fun main() {
    WordManager.init()
    TelegramBot.init()
    LoggerFactory.getLogger("main").info("telegram bot run")
}