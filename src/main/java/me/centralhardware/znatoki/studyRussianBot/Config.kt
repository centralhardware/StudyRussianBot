package me.centralhardware.znatoki.studyRussianBot

/**
 *provide access to application config file
 */
object Config {
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

}