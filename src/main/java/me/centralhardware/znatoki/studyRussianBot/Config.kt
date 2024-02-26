package me.centralhardware.znatoki.studyRussianBot

/**
 *provide access to application config file
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
    val redisUrl: String = System.getenv("REDIS_URL")
    /**
     * path to file rule.json
     */
    val rulePath: String = System.getenv("RULE_PATH")
    /**
     * path to file word.json
     */
    val wordPath: String = System.getenv("WORD_PATH")
    /**
     * List of admins id
     * separator - ","
     */
    val admins: List<Long> = System.getenv("ADMIN_ID")
        .split(",".toRegex())
        .filter { it.isNotEmpty() }
        .map { it.toLong() }
}