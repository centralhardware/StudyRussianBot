package ru.alexeyFedechkin.znatoki.studyRussianBot

import mu.KotlinLogging
import ru.alexeyFedechkin.znatoki.studyRussianBot.Objects.UserStatistic
import ru.alexeyFedechkin.znatoki.studyRussianBot.Utils.Redis
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

object Statistic {
    private val logger =KotlinLogging.logger {  }
    private const val TOTAL_SENT_KEY = "total_sent"
    private const val TOTAL_RECEIVED_KEY = "total_received"
    private const val USER_SEND_KEY = "_send"
    private const val USER_RECEIVED_KEY = "_received"
    @Volatile
    private var totalSent = 0
    @Volatile
    private var totalReceived = 0
    private val countReceivedForUser = Collections.synchronizedMap(LinkedHashMap<Long, Int>())
    private val countSentForUser = Collections.synchronizedMap(LinkedHashMap<Long, Int>())

    private const val  MILLISECONDS_IN_SECOND  = 1000
    private const val  SECOND_IN_MINUTE        = 60

    /**
     * save statistic to redis
     * start timer schedule.
     */
    fun init(){
        val period: Int = if (Config.isTesting) {
            1
        } else {
            60
        }
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                logger.info("save statistic")
                Redis.addToList(TOTAL_SENT_KEY, totalSent.toString())
                Redis.addToList(TOTAL_RECEIVED_KEY, totalReceived.toString())
                for (key in countSentForUser.keys) {
                    Redis.addToList(key.toString() + USER_SEND_KEY, countSentForUser[key].toString())
                }
                for (key in countReceivedForUser.keys) {
                    Redis.addToList(key.toString() + USER_RECEIVED_KEY, countReceivedForUser[key].toString())
                }
                clearVariable()
            }
        }, 0, (MILLISECONDS_IN_SECOND * SECOND_IN_MINUTE * period).toLong())
    }

    /**
     * clear all variable for start new period of statistics collection
     */
    private fun clearVariable() {
        totalReceived = 0
        totalSent = 0
        countReceivedForUser.clear()
        countSentForUser.clear()
    }

    /**
     * store metric about received message
     * @param chatId id of user
     */
    fun checkReceived(chatId: Long) {
        totalReceived++
        if (countReceivedForUser.containsKey(chatId)) {
            countReceivedForUser[chatId] = countReceivedForUser[chatId]!! + 1
        } else {
            countReceivedForUser[chatId] = 1
        }
    }

    /**
     * store metric about sent message
     * @param chatId id of user
     */
    fun checkSent(chatId: Long) {
        totalSent++
        if (countSentForUser.containsKey(chatId)) {
            countSentForUser[chatId] = countSentForUser[chatId]!! + 1
        } else {
            countSentForUser[chatId] = 1
        }
    }

    /**
     * get statistic
     * @return object with all statistic
     */
    fun getStatistic(): UserStatistic {
        val res = UserStatistic()
        //get row data from redis
        val totalSent = Redis.getListByKey(TOTAL_SENT_KEY)
        val totalReceived = Redis.getListByKey(TOTAL_RECEIVED_KEY)
        val userSent = Redis.getAllKeys("*$USER_SEND_KEY")
        val userReceived = Redis.getAllKeys("*$USER_RECEIVED_KEY")
        val usersSent = Redis.getAllKeys("*" + Redis.COUNT_OF_SENT_MESSAGE_POSTFIX)
        val usersReceived = Redis.getAllKeys("*" + Redis.COUNT_OF_RECEIVED_MESSAGE_POSTFIX)

        val userSentRes = HashMap<Long, ArrayList<Int>>()
        val userReceivedRes = HashMap<Long, ArrayList<Int>>()

        for (str in userSent) {
            val listString = Redis.getListByKey(str)
            val list = ArrayList<Int>()
            for (s in listString) {
                list.add(Integer.valueOf(s))
            }
            if (str != "total_send") {
                userSentRes[java.lang.Long.parseLong(str.replace(USER_SEND_KEY, ""))] = list
            }
        }
        for (str in userReceived) {
            val listString = Redis.getListByKey(str)
            val list = ArrayList<Int>()
            for (s in listString) {
                list.add(Integer.valueOf(s))
            }
            if (str != "total_received") {
                userReceivedRes[java.lang.Long.parseLong(str.replace(USER_RECEIVED_KEY, ""))] = list
            }
        }
        //set data to userStatistic object
        for (str in totalSent) {
            res.totalSend.add(Integer.valueOf(str))
        }
        for (str in totalReceived) {
            res.totalReceived.add(Integer.valueOf(str))
        }

        for (str in usersSent) {
            res.userCountSent[java.lang.Long.valueOf(str.replace(Redis.COUNT_OF_SENT_MESSAGE_POSTFIX, ""))] =
                    Integer.parseInt(Redis.getValue(str))
        }

        for (str in usersReceived) {
            res.userCountSent[java.lang.Long.valueOf(str.replace(Redis.COUNT_OF_RECEIVED_MESSAGE_POSTFIX, ""))] =
                    Integer.parseInt(Redis.getValue(str))
        }

        res.userReceived = userReceivedRes
        res.userSend = userSentRes

        res.totalCountOfSend = Integer.parseInt(Redis.getValue(Redis.COUNT_OF_SENT_MESSAGE_KEY))
        res.totalCountReceived = Integer.parseInt(Redis.getValue(Redis.COUNT_OF_RECEIVED_MESSAGE_KEY))
        return res
    }
}