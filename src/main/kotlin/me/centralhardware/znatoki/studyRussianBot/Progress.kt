package me.centralhardware.znatoki.studyRussianBot

import kotliquery.queryOf
import me.centralhardware.znatoki.studyRussianBot.objects.Rule
import me.centralhardware.znatoki.studyRussianBot.objects.Word

object Progress {

    /** Marks [rule] complete once every one of its words has been answered right. */
    suspend fun markRuleAsComplete(chatId: Long, rule: Rule) = Db.query {
        it.run(
            queryOf(
                    """
                INSERT INTO checked_rule (chat_id, rule)
                SELECT :chatId, :rule
                WHERE NOT EXISTS (
                    SELECT 1 FROM words w
                    WHERE (:ruleId = $ALL_WORDS_RULE OR w.ruleid = :ruleId)
                      AND NOT EXISTS (
                          SELECT 1 FROM checked_word c
                          WHERE c.chat_id = :chatId AND c.word = w.word
                      )
                )
                ON CONFLICT DO NOTHING
            """,
                    mapOf("chatId" to chatId, "rule" to rule.name, "ruleId" to rule.id),
                )
                .asUpdate
        )
    }

    /** Names of the rules this user has completed, in one query. */
    suspend fun checkedRules(chatId: Long): Set<String> = Db.query {
        it.run(
            queryOf("SELECT rule FROM checked_rule WHERE chat_id = :chatId", mapOf("chatId" to chatId))
                .map { r -> r.string("rule") }
                .asList
        ).toSet()
    }

    suspend fun getRightCount(chatId: Long): Long = count("checked_word", chatId)

    suspend fun getWrongCount(chatId: Long): Long = count("checked_wrong_word", chatId)

    suspend fun markWordAsRight(chatId: Long, word: Word) = markWord("checked_word", chatId, word)

    suspend fun markWordAsWrong(chatId: Long, word: Word) = markWord("checked_wrong_word", chatId, word)

    private suspend fun count(table: String, chatId: Long): Long = Db.query {
        it.run(
            queryOf("SELECT count(*) AS cnt FROM $table WHERE chat_id = :chatId", mapOf("chatId" to chatId))
                .map { r -> r.long("cnt") }
                .asSingle
        ) ?: 0L
    }

    private suspend fun markWord(table: String, chatId: Long, word: Word) = Db.query {
        it.run(
            queryOf(
                    "INSERT INTO $table (chat_id, word) VALUES (:chatId, :word) ON CONFLICT DO NOTHING",
                    mapOf("chatId" to chatId, "word" to word.name),
                )
                .asUpdate
        )
    }
}
