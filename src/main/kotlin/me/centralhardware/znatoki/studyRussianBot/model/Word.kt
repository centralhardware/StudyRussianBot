package me.centralhardware.znatoki.studyRussianBot.model

import kotliquery.Row

data class Word(val wrightName: String, val name: String, val answer: String)

val wordMapper = { row: Row ->
    Word(row.string("right_word"), row.string("word"), row.string("answer"))
}
