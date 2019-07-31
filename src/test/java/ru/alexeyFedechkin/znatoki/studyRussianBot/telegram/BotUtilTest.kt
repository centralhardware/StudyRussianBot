package ru.alexeyFedechkin.znatoki.studyRussianBot.telegram

import org.junit.Ignore
import org.junit.Test

class BotUtilTest {

    @Test
    @Ignore
    fun easterAgg() {
        val botUtil = BotUtil(Sender(TelegramBot()))
        botUtil.birthday(1)
    }
}