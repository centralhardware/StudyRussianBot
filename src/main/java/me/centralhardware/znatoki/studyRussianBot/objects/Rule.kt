package me.centralhardware.znatoki.studyRussianBot.objects

import kotliquery.Row
import me.centralhardware.znatoki.studyRussianBot.WordMapper

data class Rule(val id: Int, val name: String, var parent: Rule?, val words: List<Word>) {

    fun getWord(count: Int): Collection<Word> {
        return words.shuffled().take(count)
    }
}

val ruleMapper = { row: Row ->
    Rule(
        row.int("id"),
        row.string("name"),
        WordMapper.getRuleById(row.int("parent")),
        WordMapper.getWords(row.int("id"))
    )
}
