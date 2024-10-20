package me.centralhardware.znatoki.studyRussianBot.objects

import me.centralhardware.znatoki.studyRussianBot.WordMapper
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus
import me.centralhardware.znatoki.studyRussianBot.Redis

data class TelegramUser(
    val chatId: Long
) {
    var currRule: Rule? = null
    var status: UserStatus = UserStatus.NONE
    var words: MutableList<Word> = mutableListOf()
    var wrongWords: MutableList<Word> = mutableListOf()
    var count: Int = 0

    fun reset() {
        status = UserStatus.NONE
        words.clear()
        wrongWords.clear()
        count = 0
    }

    fun getTestingResult(): String {
        val result = wrongWords
            .map { it.name }
            .groupingBy { it }
            .eachCount()
        return buildString {
            append("всего слов в тестировании$count\n")
            append("слова, в которых допущены ошибки\n")
            result.forEach { (k, v) -> append("$k - $v\n") }
            append("всего ${result.size}  слов\n")
        }
    }

    suspend fun getProfile(): String {
        val rightPercent: Int
        val wright = Redis.getRightCount(chatId).toDouble()
        val all = Redis.getWrongCount(chatId).toDouble() + Redis.getRightCount(chatId).toDouble()
        rightPercent = if (all != 0.0 && wright < all) {
            (wright / all * 100).toInt()
        } else {
            100
        }

        return """
            Профиль:


            слов отвечено правильно: ${Redis.getRightCount(chatId)}
            слов отвечено неправильно ${Redis.getWrongCount(chatId)}
            процент правильных ответов ${rightPercent}%
            пройденные правила:
            ${
            WordMapper.getRules().map { it.name }
            .filter { Redis.isCheckRule(chatId, it) }
            .joinToString("\n")}
        """.trimIndent()
    }
}