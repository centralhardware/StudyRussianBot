package me.centralhardware.znatoki.studyRussianBot

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotliquery.Session
import kotliquery.sessionOf

/**
 * A small pool instead of one shared JDBC connection: handlers run
 * concurrently, a pgjdbc connection is not safe to share between them, and a
 * pooled connection that drops is replaced instead of failing every later query.
 */
object Db {
    val dataSource: HikariDataSource by lazy {
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = System.getenv("POSTGRES_URL")
                username = System.getenv("POSTGRES_USERNAME")
                password = System.getenv("POSTGRES_PASSWORD")
                maximumPoolSize = 4
            }
        )
    }

    /** Runs [block] on its own pooled session, off the bot's dispatcher. */
    suspend fun <T> query(block: (Session) -> T): T =
        withContext(Dispatchers.IO) { sessionOf(dataSource).use(block) }
}
