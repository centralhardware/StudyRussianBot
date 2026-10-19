package me.centralhardware.znatoki.studyRussianBot

import kotliquery.queryOf
import me.centralhardware.znatoki.studyRussianBot.objects.Rule
import me.centralhardware.znatoki.studyRussianBot.objects.Word
import me.centralhardware.znatoki.studyRussianBot.objects.ruleMapper
import me.centralhardware.znatoki.studyRussianBot.objects.wordMapper

/** Rule id whose test draws from every word. */
const val ALL_WORDS_RULE = 1

/** Rules per page of the rules keyboard. */
const val RULES_PAGE_SIZE = 7

object WordMapper {

    suspend fun getRules(): List<Rule> = Db.query {
        it.run(queryOf("SELECT id, name, parent FROM rules ORDER BY id").map(ruleMapper).asList)
    }

    suspend fun getRuleById(id: Int): Rule? = Db.query {
        it.run(
            queryOf("SELECT id, name, parent FROM rules WHERE id = :id", mapOf("id" to id))
                .map(ruleMapper)
                .asSingle
        )
    }

    suspend fun countRules(): Int = Db.query {
        it.run(queryOf("SELECT count(*) AS cnt FROM rules").map { r -> r.int("cnt") }.asSingle) ?: 0
    }

    suspend fun getRulePage(pageNumber: Int): List<Rule> = Db.query {
        it.run(
            queryOf(
                    "SELECT id, name, parent FROM rules ORDER BY id LIMIT :size OFFSET :offset",
                    mapOf("size" to RULES_PAGE_SIZE, "offset" to pageNumber * RULES_PAGE_SIZE),
                )
                .map(ruleMapper)
                .asList
        )
    }

    /** The words of a rule, loaded only when a test of it starts. */
    suspend fun getWords(ruleId: Int): List<Word> = Db.query {
        it.run(
            queryOf(
                    "SELECT * FROM words WHERE :ruleId = $ALL_WORDS_RULE OR ruleid = :ruleId",
                    mapOf("ruleId" to ruleId),
                )
                .map(wordMapper)
                .asList
        )
    }
}
