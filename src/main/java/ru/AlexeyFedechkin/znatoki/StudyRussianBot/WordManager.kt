package ru.AlexeyFedechkin.znatoki.StudyRussianBot

import org.apache.log4j.Logger
import org.json.JSONArray
import org.json.JSONObject
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Rule
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.RuleDescription
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Word
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource
import java.io.IOException
import java.util.ArrayList

object WordManager {
    private val logger = Logger.getLogger(WordManager::class.java)
    val rules = ArrayList<Rule>()
    val ruleDescriptions = ArrayList<RuleDescription>()

    /**
     * parseText all data file and generate collections of object
     * parsing file:
     * - word.json: all words
     * - rule.json: rule signature
     * - ruleDesc.json: description of rule
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
     * ruleDesc structure:
     * "$id":{
     * "name":"",
     * "description": "",
     * }
     * @throws IOException file not find or other IO problem
     */
    @Throws(IOException::class)
    fun init() {
        // get Strings from files
        val wordString = Resource.getStringFromResources("word.json")
        val ruleString = Resource.getStringFromResources("rule.json")
        val ruleDescriptionString = Resource.getStringFromResources("ruleDesc.json")
        // parse rule.json
        val ruleObject = JSONObject(ruleString)
        val ruleData = ruleObject.getJSONObject("data")
        for (i in 1 until ruleObject.getInt("count")) {
            val `object` = ruleData.getJSONObject(i.toString())
            rules.add(Rule(`object`.getString("name"), null, `object`.getString("section"), ArrayList()))
        }
        // set parent
        for (i in 1 until ruleObject.getInt("count")) {
            val `object` = ruleData.getJSONObject(i.toString())
            if (`object`.getInt("parent") != -1) {
                rules[i].parent = rules[`object`.getInt("parent") - 1]
            }
        }
        // parse word.json
        val wordObject = JSONObject(wordString)
        val wordArray = wordObject.getJSONArray("word")
        val words = ArrayList<Word>()
        for (i in 0 until wordArray.length()) {
            val word = Word.parse(wordArray.getString(i))
            if (word != null) {
                words.add(word)
            }
        }
        // parse rule.json
        for (r in rules) {
            if (r.section.equals("all")) {
                r.words.addAll(words)
            }
            for (w in words) {
                if (r.section.equals(w.section)) {
                    r.words.add(w)
                }
            }
        }
        // set word to child rule from parent
        for (r in rules) {
            if (r.parent != null) {
                r.parent!!.words.addAll(r.words)
            }
        }
        // set pageNumbers for rule
        var count = 1
        for (rule in rules) {
            rule.pageNumber = (count / Rule.pageCountRule) as Byte
            count++
        }
        for (r in rules) {
            logger.info("in rule \"" + r.name + "\" added " + r.words.size + "  words")
        }
        // parse ruleDesc.json
        val ruleDescriptionArray = JSONArray(ruleDescriptionString)
        for (ruleDesc in ruleDescriptionArray) {
            val `object` = ruleDesc as JSONObject
            ruleDescriptions.add(RuleDescription(`object`.getString("name"), `object`.getString("description"), `object`.getInt("id")))
        }
        // set pageNumbers for rule description
        count = 1
        for (ruleDesc in ruleDescriptions) {
            ruleDesc.pageNumber = ((count / Rule.pageCountRule).toByte())
            count++
        }
    }

    /**
     * get RuleDescription by id. linear search
     * @param id id of needed rule description
     * @return RuleDescription find by giving id
     */
    fun getRuleDescriptionById(id: Int): RuleDescription? {
        for (ruleDescription in ruleDescriptions) {
            if (ruleDescription.id === id) {
                return ruleDescription
            }
        }
        return null
    }

    /**
     * get Rule by name. linear search
     * @param rule name of rule
     * @return rule find by giving name
     */
    fun getRuleByName(rule: String): Rule? {
        for (r in rules) {
            if (r.name.equals(rule)) {
                return r
            }
        }
        return null
    }
}