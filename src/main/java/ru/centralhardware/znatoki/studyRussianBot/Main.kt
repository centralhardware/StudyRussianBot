package ru.centralhardware.znatoki.studyRussianBot

import org.slf4j.LoggerFactory
import ru.centralhardware.znatoki.studyRussianBot.telegram.TelegramBot

/**
 * init telegram ApiContext and telegram bot, statistic
 */
fun main() {
    WordManager.init()
    TelegramBot.init()
    LoggerFactory.getLogger("main").info("telegram bot run")
}