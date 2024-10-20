package me.centralhardware.znatoki.studyRussianBot

import kotliquery.queryOf
import kotliquery.sessionOf
import me.centralhardware.znatoki.studyRussianBot.objects.Rule
import me.centralhardware.znatoki.studyRussianBot.objects.Word
import me.centralhardware.znatoki.studyRussianBot.objects.ruleMapper
import me.centralhardware.znatoki.studyRussianBot.objects.wordMapper

object WordMapper {
    val session = sessionOf(System.getenv("POSTGRES_URL"),
        System.getenv("POSTGRES_USERNAME"),
        System.getenv("POSTGRES_PASSWORD"))

    fun getRules() = session.run(queryOf(
        """
            SELECT * 
            FROM rules
        """
    ).map(ruleMapper).asList)

    fun getRuleById(id: Int): Rule? = session.run(queryOf(
        """
            SELECT * 
            FROM rules
            WHERE id = :id
            LIMIT 1
        """, mapOf("id" to id)
    ).map(ruleMapper).asSingle)

    fun getRulePage(pageNumber: Int) = session.run(queryOf("""
        SELECT  *
        FROM rules
        ORDER BY id
        LIMIT 7
        OFFSET :pageNumber * 7
    """, mapOf("pageNumber" to pageNumber)).map(ruleMapper).asList)

    fun getWords(ruleId: Int): List<Word> = session.run(queryOf(
        """
           SELECT * 
           FROM words w
           WHERE (
                CASE 
                    WHEN :ruleId = 1 THEN true
                    WHEN :ruleId != 1 THEN ruleid = :ruleId
                END
           )
        """, mapOf("ruleId" to ruleId)
    ).map(wordMapper).asList)
}