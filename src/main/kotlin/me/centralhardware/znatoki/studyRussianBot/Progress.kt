package me.centralhardware.znatoki.studyRussianBot

import kotliquery.queryOf
import me.centralhardware.znatoki.studyRussianBot.objects.TelegramUser

object Progress {
    private val session = WordMapper.session

    fun markRuleAsComplete(user: TelegramUser) {
        val checkedCount =
            WordMapper.getWords(user.currRule!!.id).count { isWordChecked(user.chatId, it.name) }

        if (checkedCount >= user.currRule!!.words.size) {
            session.run(
                queryOf(
                        """
                INSERT INTO checked_rule (chat_id, rule)
                VALUES (:chatId, :rule)
                ON CONFLICT DO NOTHING
            """,
                        mapOf("chatId" to user.chatId, "rule" to user.currRule!!.name),
                    )
                    .asUpdate
            )
        }
    }

    fun isCheckRule(chatId: Long, rule: String): Boolean =
        session.run(
            queryOf(
                    """
            SELECT 1
            FROM checked_rule
            WHERE chat_id = :chatId AND rule = :rule
            LIMIT 1
        """,
                    mapOf("chatId" to chatId, "rule" to rule),
                )
                .map { true }
                .asSingle
        ) ?: false

    fun getRightCount(chatId: Long): Long = count("checked_word", chatId)

    fun getWrongCount(chatId: Long): Long = count("checked_wrong_word", chatId)

    fun markWordAsRight(user: TelegramUser) = markWord("checked_word", user)

    fun markWordAsWrong(user: TelegramUser) = markWord("checked_wrong_word", user)

    private fun isWordChecked(chatId: Long, word: String): Boolean =
        session.run(
            queryOf(
                    """
            SELECT 1
            FROM checked_word
            WHERE chat_id = :chatId AND word = :word
            LIMIT 1
        """,
                    mapOf("chatId" to chatId, "word" to word),
                )
                .map { true }
                .asSingle
        ) ?: false

    private fun count(table: String, chatId: Long): Long =
        session.run(
            queryOf(
                    """
            SELECT count(*) AS cnt
            FROM $table
            WHERE chat_id = :chatId
        """,
                    mapOf("chatId" to chatId),
                )
                .map { it.long("cnt") }
                .asSingle
        ) ?: 0L

    private fun markWord(table: String, user: TelegramUser) {
        session.run(
            queryOf(
                    """
            INSERT INTO $table (chat_id, word)
            VALUES (:chatId, :word)
            ON CONFLICT DO NOTHING
        """,
                    mapOf("chatId" to user.chatId, "word" to user.words[0].name),
                )
                .asUpdate
        )
    }
}
