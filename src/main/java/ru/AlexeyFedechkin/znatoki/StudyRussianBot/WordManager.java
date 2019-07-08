/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 14:08
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.RuleDescription;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Word;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * class that parse json file and generate object collections, provide collection to other classes
 */
@SuppressWarnings("HardCodedStringLiteral")
public class WordManager {
    private static final Logger logger = Logger.getLogger(WordManager.class);
    private final ArrayList<Rule> rules = new ArrayList<>(1600);
    private final ArrayList<RuleDescription> ruleDescriptions = new ArrayList<>();
    private final Resource resource = new Resource();

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
     *     "name":"",
     *     "section": "",
     *     "parent":
     * }
     * parent is id of parent rule. -1 if parent rule is null. rule which is parent have not own rules.
     * parent rule gets rules from child
     * ruleDesc structure:
     * "$id":{
     *     "name":"",
     *     "description": "",
     * }
     * @throws IOException file not find or other IO problem
     */
    public void init() throws IOException {
        // get Strings from files
        var wordString = resource.getStringFromResources("word.json");
        var ruleString = resource.getStringFromResources("rule.json");
        var ruleDescriptionString = resource.getStringFromResources("ruleDesc.json");
        // parse rule.json
        var ruleObject = new JSONObject(ruleString);
        var ruleData = ruleObject.getJSONObject("data");
        for (int i = 1; i < ruleObject.getInt("count"); i++) {
            JSONObject object = ruleData.getJSONObject(String.valueOf(i));
            rules.add(new Rule(object.getString("name"), null, object.getString("section"), new ArrayList<>()));
        }
        // set parent
        for (int i = 1; i < ruleObject.getInt("count"); i++) {
            var object = ruleData.getJSONObject(String.valueOf(i));
            if (object.getInt("parent") != -1){
                rules.get(i).setParent(rules.get(object.getInt("parent")-1));
            }
        }
        // parse word.json
        var wordObject = new JSONObject(wordString);
        var wordArray = wordObject.getJSONArray("word");
        var words = new ArrayList<Word>();
        for (int i = 0; i < wordArray.length(); i++) {
            Word word = Word.parse(wordArray.getString(i));
            if (word != null){
                words.add(word);
            }
        }
        // parse rule.json
        for (var r : rules){
            if (r.getSection().equals("all")){
                r.getWords().addAll(words);
            }
            for (var w : words){
                if (r.getSection().equals(w.getSection())){
                    r.getWords().add(w);
                }
            }
        }
        // set word to child rule from parent
        for (var r : rules){
            if (r.getParent() != null){
                r.getParent().getWords().addAll(r.getWords());
            }
        }
        // set pageNumbers for rule
        var count = 1;
        for (var rule : rules) {
            rule.setPageNumber((byte) (count/ Rule.pageCountRule));
            count++;
        }
        for (var r : rules){
            logger.info("in rule \"" + r.getName() + "\" added " + r.getWords().size() + "  words");
        }
        // parse ruleDesc.json
        var ruleDescriptionArray = new JSONArray(ruleDescriptionString);
        for (var ruleDesc : ruleDescriptionArray){
            var object = (JSONObject) ruleDesc;
            ruleDescriptions.add(new RuleDescription(object.getString("name"), object.getString("description"), object.getInt("id")));
        }
        // set pageNumbers for rule description
        count = 1;
        for (var ruleDesc : ruleDescriptions) {
            ruleDesc.setPageNumber((byte) (count/ Rule.pageCountRule));
            count++;
        }
    }

    /**
     * get RuleDescription by id. linear search
     * @param id id of needed rule description
     * @return RuleDescription find by giving id
     */
    public RuleDescription getRuleDescriptionById(int id){
        for (var ruleDescription : ruleDescriptions){
            if (ruleDescription.getId() == id){
                return ruleDescription;
            }
        }
        return null;
    }

    /**
     * get Rule by name. linear search
     * @param rule name of rule
     * @return rule find by giving name
     */
    public Rule getRuleByName(String rule) {
        for (var r : rules){
            if (r.getName().equals(rule)){
                return r;
            }
        }
        return null;
    }

    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public List<RuleDescription> getRuleDescriptions() {
        return Collections.unmodifiableList(ruleDescriptions);
    }
}