package ru.alexeyFedechkin.znatoki.studyRussianBot.objects

import ru.alexeyFedechkin.znatoki.studyRussianBot.Config
import ru.alexeyFedechkin.znatoki.studyRussianBot.WordManager
import ru.alexeyFedechkin.znatoki.studyRussianBot.objects.enums.UserStatus
import ru.alexeyFedechkin.znatoki.studyRussianBot.utils.Redis
import ru.alexeyFedechkin.znatoki.studyRussianBot.utils.Resource
import java.util.*

/**
 * data class contain structure  of rule used for testing
 *
 */
data class User(
        /**
         * id of telegram user
         */
        val chatId: Long) {
    /**
     * current rule that user passes in this time
     */
    var currRule: Rule? = null
    /**
     *status of user
     * @see UserStatus
     */
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
        val builder = StringBuilder()
        builder.append(Resource.getStringByKey("STR_10")).append(count).append("\n")
        builder.append(Resource.getStringByKey("STR_11")).append("\n")
        val result = HashMap<String, Int>()
        for ((wrightName) in wrongWords) {
            if (!result.containsKey(wrightName)) {
                result[wrightName] = 1
            } else {
                result[wrightName] = result[wrightName]!! + 1
            }
        }
        for (key in result.keys) {
            builder.append(key).append(" - ").append(result[key]).append("\n")
        }
        builder.append("всего ").append(result.size).append(" слов").append("\n")
        return builder.toString()
    }

    /**
     * get String with data about profile of user
     *
     * @return string with profile data
     */
    fun getProfile(): String {
        val rightPercent: Int
        val wright = Redis.getCountOfCheckedWord(chatId).toDouble()
        val all = Redis.getCountOfWrongCheckedWord(chatId).toDouble() + Redis.getCountOfCheckedWord(chatId).toDouble()
        rightPercent = if (all != 0.0 && wright < all) {
            (wright / all * 100).toInt()
        } else {
            100
        }
        val builder = StringBuilder()
        builder.append(Resource.getStringByKey("STR_12")).append("\n").append("\n").append("\n").append(Resource.getStringByKey("STR_15")).append(Redis.getCountOfCheckedWord(chatId)).append("\n").append(Resource.getStringByKey("STR_45")).append(Redis.getCountOfWrongCheckedWord(chatId)).append("\n").append(Resource.getStringByKey("STR_46")).append(rightPercent).append("%").append("\n").append(Resource.getStringByKey("STR_16")).append("\n")
        for ((name) in WordManager.rules) {
            if (Redis.isCheckRule(chatId, name)) {
                builder.append(" - ").append("\"").append(name).append("\"").append("\n")
            }
        }
        when {
            Config.admins.contains(chatId) -> builder.append(Resource.getStringByKey("STR_35")).append(Resource.getStringByKey("STR_37"))
            Redis.checkRight(chatId) -> builder.append(Resource.getStringByKey("STR_35")).append(Resource.getStringByKey("STR_38"))
            else -> builder.append(Resource.getStringByKey("STR_35")).append(Resource.getStringByKey("STR_39"))
        }
        return builder.toString()
    }
}