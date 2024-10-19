package me.centralhardware.znatoki.studyRussianBot.objects

import me.centralhardware.znatoki.studyRussianBot.WordManager
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus
import me.centralhardware.znatoki.studyRussianBot.utils.Redis

/**
 * data class contain structure  of rule used for testing
 *
 */
data class TelegramUser(
    /**
     * id of telegram user
     */
    val chatId: Long
) {
    /**
     * current rule that user passes in this time
     */
    var currRule: Rule? = null

    var status: UserStatus? = null

    /**
     *a collection of words to be given to the user
     */
    var words: ArrayList<Word>

    /**
     *a collection of words that made a mistake
     */
    var wrongWords: ArrayList<Word>

    /**
     *entered count of word in testing
     */
    var count: Int = 0

    init {
        this.status = UserStatus.NONE
        words = ArrayList()
        wrongWords = ArrayList()
    }

    /**
     *reset user state to default
     */
    fun reset() {
        status = UserStatus.NONE
        words.clear()
        wrongWords.clear()
        count = 0
    }

    /**
     * get string with result of testing for last rule task
     * @return result string
     */
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

    /**
     * get String with data about profile of user
     *
     * @return string with profile data
     */
    suspend fun getProfile(): String {
        val rightPercent: Int
        val wright = Redis.getCountOfCheckedWord(chatId).toDouble()
        val all = Redis.getCountOfWrongCheckedWord(chatId).toDouble() + Redis.getCountOfCheckedWord(chatId).toDouble()
        rightPercent = if (all != 0.0 && wright < all) {
            (wright / all * 100).toInt()
        } else {
            100
        }

        return """
            Профиль:


            слов отвечено правильно: ${Redis.getCountOfCheckedWord(chatId)}
            слов отвечено неправильно ${Redis.getCountOfWrongCheckedWord(chatId)}
            процент правильных ответов ${rightPercent}%
            пройденные правила:
            ${WordManager.rules.map { it.name }
            .filter { Redis.isCheckRule(chatId, it) }
            .joinToString("\n")}
        """.trimIndent()
    }
}