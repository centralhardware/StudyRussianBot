package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects

import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.UserStatus
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.WordManager
import java.util.ArrayList
import java.util.HashMap

class User {
    val chatId: Long
    var currRule: Rule? = null
    var status: UserStatus? = null
    var words: ArrayList<Word>
    var wrongWords: ArrayList<Word>
    var count = 0

    constructor(chatId: Long){
        this.chatId = chatId
        this.status = UserStatus.NONE
        words = ArrayList()
        wrongWords = ArrayList()
    }

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
        for (word in wrongWords) {
            if (!result.containsKey(word.wrightName)) {
                result[word.wrightName] = 1
            } else {
                result[word.wrightName] = result[word.wrightName]!! + 1
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
        val wrong = Redis.getCountOfWrongCheckedWord(chatId).toDouble()
        if (wrong != 0.0 && wright < wrong) {
            rightPercent = (wright / wrong * 100).toInt()
        } else {
            rightPercent = 100
        }
        val builder = StringBuilder()
        builder.append(Resource.getStringByKey("STR_12")).append("\n").append(Resource.getStringByKey("STR_13")).append(Redis.getCountOfSentMessage(chatId)).append("\n")
                .append(Resource.getStringByKey("STR_14")).append(Redis.getCountOfReceivedMessage(chatId)).append("\n").append(Resource.getStringByKey("STR_15")).append(Redis.getCountOfCheckedWord(chatId)).append("\n").append(Resource.getStringByKey("STR_45")).append(Redis.getCountOfWrongCheckedWord(chatId)).append("\n").append(Resource.getStringByKey("STR_46")).append(rightPercent).append("%").append("\n").append(Resource.getStringByKey("STR_16")).append("\n")
        for (rule in WordManager.rules) {
            if (Redis.isCheckRule(chatId, rule.name)) {
                builder.append(" - ").append("\"").append(rule.name).append("\"").append("\n")
            }
        }
        if (Config.admins.contains(chatId)) {
            builder.append(Resource.getStringByKey("STR_35")).append(Resource.getStringByKey("STR_37"))
        } else if (Redis.checkRight(chatId)) {
            builder.append(Resource.getStringByKey("STR_35")).append(Resource.getStringByKey("STR_38"))
        } else {
            builder.append(Resource.getStringByKey("STR_35")).append(Resource.getStringByKey("STR_39"))
        }
        return builder.toString()
    }
}