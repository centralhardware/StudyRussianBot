package ru.alexeyFedechkin.znatoki.studyRussianBot

import java.util.*
import kotlin.collections.ArrayList

object Config {
    private val config = ResourceBundle.getBundle("config")
    val userName        : String    = config.getString("BOT_USER_NAME")
    val token           : String    = config.getString("BOT_TOKEN")
    val testUserName    : String    = config.getString("BOT_TESTING_USER_NAME")
    val testToken       : String    = config.getString("BOT_TESTING_TOKEN")
    val proxyHost       : String    = config.getString("PROXY_HOST")
    val proxyPort       : Int       = config.getString("PROXY_PORT").toInt()
    val isTesting       : Boolean   = config.getString("IS_TESTING").toBoolean()
    val isUseProxy      : Boolean   = config.getString("IS_USE_PROXY").toBoolean()
    val redisHost       : String    = config.getString("REDIS_HOST")
    val redisPort       : Int       = config.getString("REDIS_PORT").toInt()
    val privateKey      : String    = config.getString("RSA_PRIVATE_KEY")
    val publicKey       : String    = config.getString("RSA_PUBLIC_KEY")
    val admins          : ArrayList<Long> = {
        val adminsArr = config.getString("ADMIN_ID").
                split(",".toRegex())
                .dropLastWhile { it.isEmpty() }.
                        toTypedArray()
        val admins = java.util.ArrayList<Long>()
        for (id in adminsArr) {
            admins.add(java.lang.Long.valueOf(id))
        }
        admins
    }.invoke()
}