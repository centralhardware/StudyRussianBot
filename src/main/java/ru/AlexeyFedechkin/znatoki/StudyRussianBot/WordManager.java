/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 14:08
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.RuleDescription;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Word;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * class that parse json file and generate object collections, provide collection to other classes
 */
@SuppressWarnings("HardCodedStringLiteral")
public class WordManager {
    private final Logger logger = Logger.getLogger(WordManager.class);
    private final ArrayList<String> wrongMessages = new ArrayList<>();
    private final ArrayList<Rule> rules = new ArrayList<>();
    private final ArrayList<RuleDescription> ruleDescriptions = new ArrayList<>();

    /**
     * parseText all data file and generate collections of object
     * @throws IOException file not find or other IO problem
     */
    public void init() throws IOException {
        // get Strings from files
        var wordString = getStringFromResources("word.json");
        var ruleString = getStringFromResources("rule.json");
        var wrongMessageString = getStringFromResources("wrongMessage.json");
        var ruleDescriptionString = getStringFromResources("ruleDesc.json");
        // parse wrongMessage.json
        var wrongMessageObject = new JSONObject(wrongMessageString);
        var wrongMessageArray = wrongMessageObject.getJSONArray("data");
        for (int i = 0; i < wrongMessageArray.length(); i++) {
            wrongMessages.add(wrongMessageArray.getString(i));
        }
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
        // set pageNumbers
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
        // set pageNumbers
        count = 1;
        for (var ruleDesc : ruleDescriptions) {
            ruleDesc.setPageNumber((byte) (count/ Rule.pageCountRule));
            count++;
        }
        System.gc();
    }

    /**
     * get string from resource folder file
     * @param fileName name of file to search
     * @return string from file that placed in resource folder
     */
    private String getStringFromResources(String fileName) throws IOException {
        var classLoader = ClassLoader.getSystemClassLoader();
        var inputStream = classLoader.getResourceAsStream(fileName);
        var writer = new StringWriter();
        IOUtils.copy(Objects.requireNonNull(inputStream), writer);
        return writer.toString();
    }

    private final Random random = new Random();

    /**
     * get random wrong message
     * @return random string with remark about error
     */
    public String getRandomWrongMessage(){
        return (String) wrongMessages.toArray()[random.nextInt(wrongMessages.size())];
    }

    public ArrayList<Rule> getRules(){
        return rules;
    }

    public ArrayList<RuleDescription> getRuleDescriptions() {
        return ruleDescriptions;
    }

    /**
     * get RuleDescription by id
     *
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
     * get Rule by name
     * @param rule name of rule
     * @return rule find by giving name
     */
    public Rule getRule(String rule){
        for (var r : rules){
            if (r.getName().equals(rule)){
                return r;
            }
        }
        return null;
    }
}