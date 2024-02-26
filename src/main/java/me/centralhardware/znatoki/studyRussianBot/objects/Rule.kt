package me.centralhardware.znatoki.studyRussianBot.objects

import me.centralhardware.znatoki.studyRussianBot.WordManager
import java.util.*

/**
 *data class that contain structure of rule
 */
data class Rule(
    /**
         * name of rule
         */
        val name: String,
    /**
         * parent rule
         * used for generalized rules
         */
        var parent: Rule?,
    /**
         *section identifier for mapping with word
         */
        val section: String,
    /**
         *List of words
         */
        val words: ArrayList<Word>) {
    /**
     * number of page which is located word
     */
    var pageNumber: Int = 0


    companion object {
        /**
         *number of rule on the page
         */
        const val pageCountRule: Int = 7
        /**
         *count of pages
         */
        val maxRulePage: Int by lazy {
            var max = 0
            for (rule in WordManager.rules) {
                if (rule.pageNumber > max) {
                    max = rule.pageNumber
                }
            }
            max
        }
    }

    private val random = Random()

    /**
     * get Collection with giving count of word
     *
     * @param count needed count of word
     * @return Collection of word
     */
    fun getWord(count: Int): Collection<Word> {
        val wordSet = HashSet<Word>()
        while (wordSet.size < count) {
            wordSet.add(words[random.nextInt(words.size)])
        }
        return ArrayList(wordSet)
    }
}