package ru.centralhardware.znatoki.studyRussianBot.utils

import mu.KotlinLogging
import redis.clients.jedis.Jedis
import ru.centralhardware.znatoki.studyRussianBot.Config
import ru.centralhardware.znatoki.studyRussianBot.WordManager
import ru.centralhardware.znatoki.studyRussianBot.objects.User

/**
 *provide access to redis server
 */
object Redis {
    private val logger = KotlinLogging.logger { }
    private val jedis: Jedis = Jedis(Config.redisHost, Config.redisPort)
    private const val KEY_POSTFIX = "_key"
    private const val CHECKED_WRONG_WORD_POSTFIX = "_checked_wrong_word"
    private const val CHECKED_WORD_POSTFIX = "_checked_word"
    private const val CHECKED_RULE_POSTFIX = "_checked_rule"

    /**
     * store data about passing rule task
     * @param user user that pass rule
     */
    fun checkRule(user: User) {
        val checkWordKey = user.chatId.toString() + CHECKED_WORD_POSTFIX
        val checkRuleKey = user.chatId.toString() + CHECKED_RULE_POSTFIX
        var checkedCount = 0
        for (word in WordManager.getRuleByName(user.currRule!!.name)!!.words) {
            if (jedis.sismember(checkWordKey, word.name)) {
                checkedCount++
            }
        }
        if (checkedCount >= user.currRule!!.words.size) {
            jedis.sadd(checkRuleKey, user.currRule!!.name)
        }
    }

    /**
     * check on passing rule
     * @param chatId id of user
     * @param rule name of rule
     * @return true if rule was passing
     */
    fun isCheckRule(chatId: Long, rule: String): Boolean {
        val checkRuleKey = chatId.toString() + CHECKED_RULE_POSTFIX
        return jedis.sismember(checkRuleKey, rule)!!
    }

    /**
     * get count of checked word
     * @param chatId id of user
     * @return count of checked word
     */
    fun getCountOfCheckedWord(chatId: Long): Long {
        logger.info("get count of checked word")
        val checkWordKey = chatId.toString() + CHECKED_WORD_POSTFIX
        return jedis.scard(checkWordKey)!!
    }

    /**
     * get count of wrong checked word
     *
     * @param chatId id of user
     * @return count of checked word
     */
    fun getCountOfWrongCheckedWord(chatId: Long): Long {
        logger.info("get count of wrong checked word")
        val checkWordKey = chatId.toString() + CHECKED_WRONG_WORD_POSTFIX
        return jedis.scard(checkWordKey)!!
    }

    /**
     * note that the word was answered correctly for the given user
     * @param user user that check word
     */
    fun checkWord(user: User) {
        val checkWordKey = user.chatId.toString() + CHECKED_WORD_POSTFIX
        jedis.sadd(checkWordKey, user.words[0].name)
        logger.info("add value \"" + user.words[0].name + "\" to set by key \"" + checkWordKey + "\"")
    }

    /**
     * note that the word was not answered correctly for the given user
     *
     * @param user user that check word
     */
    fun checkWrongWord(user: User) {
        val checkWordKey = user.chatId.toString() + CHECKED_WRONG_WORD_POSTFIX
        jedis.sadd(checkWordKey, user.words[0].name)
        logger.info("add value \"" + user.words[0].name + "\" to set by key \"" + checkWordKey + "\"")
    }

    /**
     * check license status
     * @param user_id id of user
     * @return true if user don't have demo access
     */
    fun checkRight(user_id: Long): Boolean {
        if (Config.admins.contains(user_id)) {
            logger.info("user \"$user_id\" have admin permission")
            return true
        }
        val checkRightKey = user_id.toString() + KEY_POSTFIX
        val key = jedis.get(checkRightKey)
        return if (key != null) {
            logger.info("right for user = \"$user_id\" valid")
            true
        } else {
            logger.info("right for user = \"$user_id\" don't valid")
            false
        }
    }

    /**
     * set activated code
     * @param user_id id of user
     * @param key activated code
     */
    fun setRight(user_id: Long, key: String) {
        val checkRightKey = user_id.toString() + KEY_POSTFIX
        jedis.set(checkRightKey, key)
        logger.info("set right for key = \"$key\" and user = \"$user_id\"")
    }
}