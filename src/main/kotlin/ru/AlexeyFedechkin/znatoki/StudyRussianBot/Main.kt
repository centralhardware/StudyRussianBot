package ru.AlexeyFedechkin.znatoki.StudyRussianBot

import mu.KotlinLogging
import org.telegram.telegrambots.ApiContextInitializer
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.TelegramBot

private val logger = KotlinLogging.logger {  }
/**
 * init telegram ApiContext and telegram bot, statistic
 */
fun main(){
    logger.info("program start")
    ApiContextInitializer.init()
    logger.info("api context init")
    TelegramBot().init()
    logger.info("telegram bot run")
    Statistic.init()
    logger.info("init statistic")
}