package me.centralhardware.znatoki.studyRussianBot

import org.json.JSONObject
import org.slf4j.LoggerFactory
import me.centralhardware.znatoki.studyRussianBot.objects.Rule
import me.centralhardware.znatoki.studyRussianBot.objects.Word
import me.centralhardware.znatoki.studyRussianBot.utils.Resource
import java.util.*

/**
 *object contains rule and rule description collection
 */
object WordManager {
    private val logger = LoggerFactory.getLogger(WordManager::class.java)
    /**
     * List of rules
     */
    val rules: ArrayList<Rule> = ArrayList<Rule>()

    /**
     * parseText all data file and generate collections of object
     * parsing file:
     * - word.json: all words
     * - rule.json: rule signature
     * word structure:
     * word:[]
     * rule structure:
     * "$id":{
     * "name":"",
     * "section": "",
     * "parent":
     * }
     * parent is id of parent rule. -1 if parent rule is null. rule which is parent have not own rules.
     * parent rule gets rules from child
     */
    init {
        // get Strings from files
        val wordString = Resource.loadFromPath(Config.wordPath)
        val ruleString = Resource.loadFromPath(Config.rulePath)
        // parse rule.json
        val ruleObject = JSONObject(ruleString)
        val ruleData = ruleObject.getJSONObject("data")
        for (i in 1 until ruleObject.getInt("count")) {
            ruleData.getJSONObject(i.toString()).let {
                rules.add(Rule(it.getString("name"), null, it.getString("section"), ArrayList()))
                // set parent
                if (it.getInt("parent") != -1){
                    rules[i -1].parent = rules[it.getInt("parent") -1]
                }
            }
        }

        // parse word.json
        val wordObject = JSONObject(wordString)
        val wordArray = wordObject.getJSONArray("word")
        val words = ArrayList<Word>()

        wordArray.map { it as JSONObject }.forEach {
            words.add(Word(it.getString("right_word"), it.getString("word"), it.getString("answer"), it.getString("section")))
        }

        var count = 1
        rules.forEach{
            // parse rule.json
            if (it.section == "all") {
                it.words.addAll(words)
            }
            words.forEach{ word ->
                if (it.section == word.section) {
                    it.words.add(word)
                }
            }
            // set word to child rule from parent
            it.parent?.let { it.words.addAll(it.words) }
            // set pageNumbers for rule
            it.pageNumber = count / Rule.pageCountRule
            count++
        }

        rules.forEach{ logger.info("in rule \"" + it.name + "\" added " + it.words.size + "  words") }
        logger.info("init data")
    }

    fun init(){}

    /**
     * get Rule by name. linear search
     * @param rule name of rule
     * @return rule find by giving name
     */
    fun getRuleByName(rule: String): Rule? = rules.firstOrNull { it.name == rule }
}