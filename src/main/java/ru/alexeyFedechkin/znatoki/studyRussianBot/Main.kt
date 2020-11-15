package ru.alexeyFedechkin.znatoki.studyRussianBot

import mu.KotlinLogging
import ru.alexeyFedechkin.znatoki.studyRussianBot.telegram.TelegramBot

private val logger = KotlinLogging.logger { }
/**
 * init telegram ApiContext and telegram bot, statistic
 */
fun main() {
    TelegramBot().init()
    logger.info("telegram bot run")
}