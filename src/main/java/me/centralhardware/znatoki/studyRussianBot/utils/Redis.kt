package me.centralhardware.znatoki.studyRussianBot.utils

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import org.slf4j.LoggerFactory
import me.centralhardware.znatoki.studyRussianBot.Config
import me.centralhardware.znatoki.studyRussianBot.WordManager
import me.centralhardware.znatoki.studyRussianBot.objects.User

/**
 *provide access to redis server
 */
object Redis {
    private val redis = newClient(Endpoint.from(Config.redisUrl))
    private const val KEY_POSTFIX = "_key"
    private const val CHECKED_WRONG_WORD_POSTFIX = "_checked_wrong_word"
    private const val CHECKED_WORD_POSTFIX = "_checked_word"
    private const val CHECKED_RULE_POSTFIX = "_checked_rule"

    /**
     * store data about passing rule task
     * @param user user that pass rule
     */
    suspend fun checkRule(user: User) {
        val checkWordKey = "${user.chatId} $CHECKED_WORD_POSTFIX"
        val checkRuleKey = "${user.chatId} $CHECKED_RULE_POSTFIX"
        var checkedCount = WordManager.getRuleByName(user.currRule!!.name)!!.words.count { redis.sismember(checkWordKey, it.name) == 1L }

        if (checkedCount >= user.currRule!!.words.size) {
            redis.sadd(checkRuleKey, user.currRule!!.name)
        }
    }

    /**
     * check on passing rule
     * @param chatId id of user
     * @param rule name of rule
     * @return true if rule was passing
     */
    suspend fun isCheckRule(chatId: Long, rule: String): Boolean = redis.sismember("$chatId$CHECKED_RULE_POSTFIX", rule) == 1L

    /**
     * get count of checked word
     * @param chatId id of user
     * @return count of checked word
     */
    suspend fun getCountOfCheckedWord(chatId: Long): Long = redis.scard("$chatId$CHECKED_WORD_POSTFIX")

    /**
     * get count of wrong checked word
     *
     * @param chatId id of user
     * @return count of checked word
     */
    suspend fun getCountOfWrongCheckedWord(chatId: Long): Long = redis.scard("$chatId$CHECKED_WRONG_WORD_POSTFIX")

    /**
     * note that the word was answered correctly for the given user
     * @param user user that check word
     */
    suspend fun checkWord(user: User) = redis.sadd("${user.chatId}$CHECKED_WORD_POSTFIX", user.words[0].name)

    /**
     * note that the word was not answered correctly for the given user
     *
     * @param user user that check word
     */
    suspend fun checkWrongWord(user: User) = redis.sadd("${user.chatId}$CHECKED_WRONG_WORD_POSTFIX", user.words[0].name)

    /**
     * check license status
     * @param userId id of user
     * @return true if user don't have demo access
     */
    suspend fun checkRight(userId: Long): Boolean {
        if (Config.admins.contains(userId)) {
            return true
        }
        return redis.get("$userId$KEY_POSTFIX") == null
    }

    /**
     * set activated code
     * @param userId id of user
     * @param key activated code
     */
    suspend fun setRight(userId: Long, key: String) = redis.set("$userId$KEY_POSTFIX", key)
}