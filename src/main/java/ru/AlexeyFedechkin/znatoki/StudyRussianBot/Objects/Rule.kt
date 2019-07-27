package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects

import ru.AlexeyFedechkin.znatoki.StudyRussianBot.WordManager
import java.util.*

class Rule {
    val name: String
    var parent: Rule? = null
    val section: String
    val words: ArrayList<Word>
    var pageNumber: Byte = 0


    constructor(name: String, parent: Rule?, section: String, words: ArrayList<Word>){
        this.name = name
        this.parent = parent
        this.words = words
        this.section = section
    }

    companion object{
        const val pageCountRule = 10
        val maxRulePage : Byte
            public get() {
                var max = 0
                for (rule in WordManager.rules) {
                    if (rule.pageNumber > max) {
                        max = rule.pageNumber.toInt()
                    }
                }
                return max.toByte()
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