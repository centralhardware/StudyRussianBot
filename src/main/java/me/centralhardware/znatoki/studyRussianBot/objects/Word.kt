package me.centralhardware.znatoki.studyRussianBot.objects

/**
 *date class containing the structure of the word used for testing
 */
data class Word(
        /**
         *word without missing character
         */
        val wrightName: String,
        /**
         *word with missed character in place in which you need to insert the correct character
         */
        val name: String,
        /**
         *section identifier that used for mapping word to its rule
         */
        val answer: String,
        /**
         * user answer that considered correct
         */
        val section: String)