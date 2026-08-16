package me.centralhardware.znatoki.studyRussianBot.model

import kotliquery.Row
import me.centralhardware.znatoki.studyRussianBot.DatabaseService

data class Rule(val id: Int, val name: String, var parent: Rule?, val words: List<Word>) {

    fun getWord(count: Int): Collection<Word> {
        return words.shuffled().take(count)
    }
}

val ruleMapper = { row: Row ->
    Rule(
        row.int("id"),
        row.string("name"),
        DatabaseService.getRuleById(row.int("parent")),
        DatabaseService.getWords(row.int("id")),
    )
}
