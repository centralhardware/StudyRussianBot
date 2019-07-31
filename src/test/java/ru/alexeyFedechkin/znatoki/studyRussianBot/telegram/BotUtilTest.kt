package ru.alexeyFedechkin.znatoki.studyRussianBot.telegram

import org.junit.Test

class BotUtilTest {

    @Test
    fun easterAgg() {
        val botUtil = BotUtil(Sender(TelegramBot()))
        botUtil.easterAgg(1)
    }
}