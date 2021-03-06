package ru.centralhardware.znatoki.studyRussianBot

import java.util.*

/**
 * provide access to application config file
 * Copyright © 2019-2021 Fedechkin Alexey Borisovich. Contacts: alex@centralhardware.ru
 */
object Config {
    val TELEGRAM_API_BOT_URL: String = System.getenv("TELEGRAM_API_BOT_URL")
    /**
     * bot username
     */
    val userName: String = System.getenv("BOT_USER_NAME")
    /**
     * bot token
     */
    val token: String = System.getenv("BOT_TOKEN")
    /**
     * host of redis server
     */
    val redisHost: String = System.getenv("REDIS_HOST")
    /**
     * port of redis server
     */
    val redisPort: Int = System.getenv("REDIS_PORT")
        .toInt()
    /**
     * List of admins id
     * separator - ","
     */
    val admins: ArrayList<Long> = run {
        val adminsArr = System.getenv("ADMIN_ID")
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