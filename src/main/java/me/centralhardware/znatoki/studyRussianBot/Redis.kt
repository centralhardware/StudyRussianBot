package me.centralhardware.znatoki.studyRussianBot

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import me.centralhardware.znatoki.studyRussianBot.objects.TelegramUser

object Redis {
    private val redis = newClient(Endpoint.Companion.from(System.getenv("REDIS_URL")))
    private const val CHECKED_WRONG_WORD_POSTFIX = "_checked_wrong_word"
    private const val CHECKED_WORD_POSTFIX = "_checked_word"
    private const val CHECKED_RULE_POSTFIX = "_checked_rule"

    suspend fun markRuleAsComplete(user: TelegramUser) {
        val checkWordKey = "${user.chatId} $CHECKED_WORD_POSTFIX"
        val checkRuleKey = "${user.chatId} $CHECKED_RULE_POSTFIX"
        val checkedCount =
            WordMapper.getWords(user.currRule!!.id).count {
                redis.sismember(checkWordKey, it.name) == 1L
            }

        if (checkedCount >= user.currRule!!.words.size) {
            redis.sadd(checkRuleKey, user.currRule!!.name)
        }
    }

    suspend fun isCheckRule(chatId: Long, rule: String): Boolean =
        redis.sismember("$chatId$CHECKED_RULE_POSTFIX", rule) == 1L

    suspend fun getRightCount(chatId: Long): Long = redis.scard("$chatId$CHECKED_WORD_POSTFIX")

    suspend fun getWrongCount(chatId: Long): Long =
        redis.scard("$chatId$CHECKED_WRONG_WORD_POSTFIX")

    suspend fun markWordAsRight(user: TelegramUser) =
        redis.sadd("${user.chatId}$CHECKED_WORD_POSTFIX", user.words[0].name)

    suspend fun markWordAsWrong(user: TelegramUser) =
        redis.sadd("${user.chatId}$CHECKED_WRONG_WORD_POSTFIX", user.words[0].name)
}
