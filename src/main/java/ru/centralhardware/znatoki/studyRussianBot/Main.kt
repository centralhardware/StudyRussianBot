package ru.centralhardware.znatoki.studyRussianBot

import mu.KotlinLogging
import ru.centralhardware.znatoki.studyRussianBot.telegram.TelegramBot

private val logger = KotlinLogging.logger { }
/**
 * init telegram ApiContext and telegram bot, statistic
 * Copyright © 2019-2021 Fedechkin Alexey Borisovich. Contacts: alex@centralhardware.ru
 */
fun main() {
    WordManager.init()
    TelegramBot.init()
    logger.info("telegram bot run")
}