package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects

import mu.KotlinLogging
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.SchoolStage

class Word(val wrightName: String,
           val name: String,
           val section: String,
           val answer: String,
           private val schoolStage: SchoolStage) {

    companion object{
        private val logger = KotlinLogging.logger {  }
        /**
         * parseText string from json file to object
         * name + wright name + answer + section + isHigh + isSecondary + isPrimary
         * example: суб_ективный субъективный ъ r13 1 1 0
         * splitter: space
         * %20 = space
         * @param str string for parseText
         * @return object crated from parsing string
         */
        fun parse(str: String): Word? {
            if (Config.isTesting) {
                logger.info("parsing string $str")
            }
            val args = str.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (args.size != 7) {
                logger.warn("error in data file $str")
                return null
            }
            val name = args[0].replace("%20", " ")
            val wrightName = args[1].replace("%20", " ")
            val answer = args[2]
            val section = args[3]
            val isHigh = java.lang.Boolean.parseBoolean(args[4])
            val isSecondary = java.lang.Boolean.parseBoolean(args[5])
            val isPrimary = java.lang.Boolean.parseBoolean(args[6])
            val stage = SchoolStage.SECONDARY_AND_HIGH
            return Word(wrightName, name, section, answer, stage)
        }
    }
}