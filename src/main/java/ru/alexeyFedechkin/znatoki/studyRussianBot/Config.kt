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
     * bot testing username
     */
    val testUserName: String = config.getString("BOT_TESTING_USER_NAME")
    /**
     * bot testing token
     */
    val testToken: String = config.getString("BOT_TESTING_TOKEN")
    /**
     * host of proxy server
     */
    val proxyHost: String = config.getString("PROXY_HOST")
    /**
     * port of proxy server
     */
    val proxyPort: Int = config.getString("PROXY_PORT")
        .toInt()
    /**
     * if true will be used test bot and debug message will by displayed
     */
    val isTesting: Boolean = config.getString("IS_TESTING")
        .toBoolean()
    /**
     * is setting proxy option
     */
    val isUseProxy: Boolean = config.getString("IS_USE_PROXY")
        .toBoolean()
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
    val admins: ArrayList<Long> = {
        val adminsArr = config.getString("ADMIN_ID")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val admins = java.util.ArrayList<Long>()
        for (id in adminsArr) {
            admins.add(java.lang.Long.valueOf(id))
        }
        admins
    }.invoke()
}