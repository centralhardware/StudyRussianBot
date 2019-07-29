package ru.alexeyFedechkin.znatoki.studyRussianBot.Objects

import ru.alexeyFedechkin.znatoki.studyRussianBot.WordManager
import java.util.*

data class Rule(val name: String, var parent: Rule?, val section: String, val words: ArrayList<Word>) {
    var pageNumber: Int = 0


    companion object{
        const val pageCountRule = 7
        val maxRulePage : Int by lazy  {
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