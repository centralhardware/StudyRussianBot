package ru.centralhardware.znatoki.studyRussianBot.objects

import mu.KotlinLogging

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
        val section: String,
        /**
         * user answer that considered correct
         */
        val answer: String) {

    companion object {
        private val logger = KotlinLogging.logger { }
        /**
         * parseText string from json file to object
         * name + wright name + answer + section
         * example: суб_ективный субъективный ъ r13 1 1 0
         * splitter: space
         * %20 = space
         * @param str string for parseText
         * @return object crated from parsing string
         */
        fun parse(str: String): Word? {
            logger.info("parsing string $str")
            val args = str.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (args.size != 4) {
                logger.warn("error in string  $str")
                return null
            }
            val name = args[0].replace("%20", " ")
            val wrightName = args[1].replace("%20", " ")
            val answer = args[2]
            val section = args[3]
            return Word(wrightName, name, section, answer)
        }
    }
}