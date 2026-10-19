package me.centralhardware.znatoki.studyRussianBot.objects

import kotliquery.Row

/** A rule as listed: its words are loaded only when a test of it starts. */
data class Rule(val id: Int, val name: String, val parent: Int)

val ruleMapper = { row: Row -> Rule(row.int("id"), row.string("name"), row.int("parent")) }
