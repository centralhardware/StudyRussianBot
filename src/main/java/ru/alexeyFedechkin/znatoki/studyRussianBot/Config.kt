package ru.alexeyFedechkin.znatoki.studyRussianBot

import java.util.*

/**
 *provide access to application config file
 */
object Config {
    private val config = ResourceBundle.getBundle("config")
    /**
     * bot username
     */
    val userName: String = config.getString("BOT_USER_NAME")
    /**
     * bot token
     */
    val token: String = config.getString("BOT_TOKEN")
    /**
     * host of redis server
     */
    val redisHost: String = config.getString("REDIS_HOST")
    /**
     * port of redis server
     */
    val redisPort: Int = config.getString("REDIS_PORT")
        .toInt()
    /**
     * List of admins id
     * separator - ","
     */
    val admins: ArrayList<Long> = run {
        val adminsArr = config.getString("ADMIN_ID")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val admins = ArrayList<Long>()
        for (id in adminsArr) {
            admins.add(java.lang.Long.valueOf(id))
        }
        admins
    }
}