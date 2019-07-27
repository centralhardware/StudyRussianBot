package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils

import mu.KotlinLogging
import redis.clients.jedis.Jedis
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.User
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.WordManager
import java.util.HashSet

object Redis {
        private val logger = KotlinLogging.logger {  }
        private val jedis: Jedis = Jedis(Config.redisHost, Config.redisPort)
        const val COUNT_OF_SENT_MESSAGE_KEY = "count_of_sent_message"
        const val COUNT_OF_RECEIVED_MESSAGE_KEY = "count_of_received_message"
        const val COUNT_OF_RECEIVED_MESSAGE_POSTFIX = "_count_of_received_message"
        const val COUNT_OF_SENT_MESSAGE_POSTFIX = "_count_of_sent_message"
        private const val KEY_POSTFIX = "_key"
        private const val CHECKED_WRONG_WORD_POSTFIX = "_checked_wrong_word"
        private const val CHECKED_WORD_POSTFIX = "_checked_word"
        private const val CHECKED_RULE_POSTFIX = "_checked_rule"

        /**
         * store data in redis about count of received messages
         * @param chatId id of user
         */
        fun received(chatId: Long) {
            val count_of_received_message_key = chatId.toString() + COUNT_OF_RECEIVED_MESSAGE_POSTFIX
            if (jedis.get(count_of_received_message_key) == null) {
                jedis.set(count_of_received_message_key, "1")
            } else {
                jedis.set(count_of_received_message_key, (Integer.parseInt(jedis.get(count_of_received_message_key)) + 1).toString())
            }
            logger.info("set key \"" + count_of_received_message_key + "\" value \"" + jedis.get(count_of_received_message_key) + "\"")

            if (jedis.get(COUNT_OF_RECEIVED_MESSAGE_KEY) == null) {
                jedis.set(COUNT_OF_RECEIVED_MESSAGE_KEY, "1")
            } else {
                jedis.set(COUNT_OF_RECEIVED_MESSAGE_KEY, (Integer.parseInt(jedis.get(COUNT_OF_RECEIVED_MESSAGE_KEY)) + 1).toString())
            }
            logger.info("set key \"" + COUNT_OF_RECEIVED_MESSAGE_KEY + "\" value \"" + jedis.get(COUNT_OF_RECEIVED_MESSAGE_KEY) + "\"")
        }

        /**
         * store data in redis about count of sent messages
         * @param chatId id of user
         */
        fun sent(chatId: Long) {
            val count_of_sent_message_key = chatId.toString() + COUNT_OF_SENT_MESSAGE_POSTFIX
            if (jedis.get(count_of_sent_message_key) == null) {
                jedis.set(count_of_sent_message_key, "1")
            } else {
                jedis.set(count_of_sent_message_key, (Integer.parseInt(jedis.get(count_of_sent_message_key)) + 1).toString())
            }
            logger.info("set key \"" + count_of_sent_message_key + "\" value \"" + jedis.get(count_of_sent_message_key) + "\"")

            if (jedis.get(COUNT_OF_SENT_MESSAGE_KEY) == null) {
                jedis.set(COUNT_OF_SENT_MESSAGE_KEY, "1")
            } else {
                jedis.set(COUNT_OF_SENT_MESSAGE_KEY, (Integer.parseInt(jedis.get(COUNT_OF_SENT_MESSAGE_KEY)) + 1).toString())
            }
            logger.info("set key \"" + COUNT_OF_SENT_MESSAGE_KEY + "\" value \"" + jedis.get(COUNT_OF_SENT_MESSAGE_KEY) + "\"")
        }

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
            if (Config.isTesting) {
                if (checkedCount > 2) {
                    jedis.sadd(checkRuleKey, user.currRule!!.name)
                    logger.info("add value \"" + user.currRule!!.name + "\" to set by key \"" + checkRuleKey + "\"")
                }
            } else {
                if (checkedCount >= user.currRule!!.words.size) {
                    jedis.sadd(checkRuleKey, user.currRule!!.name)
                }
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
         * get count of send message for user
         * @param chatId id of user
         * @return count of sen message
         */
        fun getCountOfSentMessage(chatId: Long): String {
            logger.info("get count of message")
            val count_of_received_message_key = chatId.toString() + COUNT_OF_RECEIVED_MESSAGE_POSTFIX
            return jedis.get(count_of_received_message_key)
        }

        /**
         * get count of received message for user
         * @param chatId id of user
         * @return count of received message
         */
        fun getCountOfReceivedMessage(chatId: Long): String {
            logger.info("get count of received message")
            val count_of_sent_message_key = chatId.toString() + COUNT_OF_SENT_MESSAGE_POSTFIX
            return jedis.get(count_of_sent_message_key)
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
            jedis.sadd(checkWordKey, user.words.get(0).name)
            logger.info("add value \"" + user.words.get(0).name + "\" to set by key \"" + checkWordKey + "\"")
        }

        /**
         * note that the word was not answered correctly for the given user
         *
         * @param user user that check word
         */
        fun checkWrongWord(user: User) {
            val checkWordKey = user.chatId.toString() + CHECKED_WRONG_WORD_POSTFIX
            jedis.sadd(checkWordKey, user.words.get(0).name)
            logger.info("add value \"" + user.words.get(0).name + "\" to set by key \"" + checkWordKey + "\"")
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
            if (key != null) {
                logger.info("right for user = \"$user_id\" valid")
                return true
            } else {
                logger.info("right for user = \"$user_id\" don't valid")
                return false
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

        /**
         * delete value by key
         * @param key key of value to delete
         */
        fun deleteKey(key: String) {
            jedis.del(key)
        }

        /**
         * get value by giving key
         * @param key key for getting
         * @return String with value by giving key
         */
        fun getValue(key: String): String {
            return jedis.get(key)
        }

        fun setKey(key: String, value: String) {
            jedis.set(key, value)
        }

        /**
         * get set of redis keys without keys with activated code
         *
         * @return all redis keys set
         */
        fun getAllKeys(pattern: String): Set<String> {
            val keys = HashSet<String>()
            for (str in jedis.keys(pattern)) {
                if (!str.endsWith(KEY_POSTFIX)) {
                    keys.add(str)
                }
            }
            return keys
        }

        /**
         * add data to list. using for store data about statistic
         *
         * @param key   key of list
         * @param value value too add in list
         */
        @Synchronized
        fun addToList(key: String, value: String) {
            jedis.lpush(key, value)
        }

        /**
         * get list by key
         *
         * @param key key of list
         * @return list data
         */
        fun getListByKey(key: String): List<String> {
            return jedis.lrange(key, 0, jedis.llen(key)!!)
        }
}