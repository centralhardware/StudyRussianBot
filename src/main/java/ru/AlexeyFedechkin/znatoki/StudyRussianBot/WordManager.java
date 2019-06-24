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
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Word;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * class that parse json file and generate object collections, provide collection to other classes
 */
public class WordManager {
    private final Logger logger = Logger.getLogger(WordManager.class);
    private ArrayList<String> wrongMessages = new ArrayList<>();
    private ArrayList<Rule> rules = new ArrayList<>();


    /**
     * parseText all data file and generate collections of object
     * @throws IOException file not find or other IO problem
     */
    public void init() throws IOException {
        String wordString = getStringFromResources("word.json");
        String ruleString = getStringFromResources("rule.json");
        String wrongMessageString = getStringFromResources("wrongMessage.json");

        JSONObject wrongMessageObject = new JSONObject(wrongMessageString);
        JSONArray wrongMessageArray = wrongMessageObject.getJSONArray("data");
        for (int i = 0; i < wrongMessageArray.length(); i++) {
            wrongMessages.add(wrongMessageArray.getString(i));
        }

        JSONObject ruleObject = new JSONObject(ruleString);
        JSONObject ruleData = ruleObject.getJSONObject("data");
        for (int i = 1; i < ruleObject.getInt("count"); i++) {
            JSONObject object = ruleData.getJSONObject(String.valueOf(i));
            rules.add(new Rule(object.getString("name"), null, object.getString("section"), new ArrayList<>()));
        }
        for (int i = 1; i < ruleObject.getInt("count"); i++) {
            JSONObject object = ruleData.getJSONObject(String.valueOf(i));
            if (object.getInt("parent") != -1){
                rules.get(i).setParent(rules.get(object.getInt("parent")-1));
            }
        }

        JSONObject wordObject = new JSONObject(wordString);
        JSONArray wordArray = wordObject.getJSONArray("word");
        ArrayList<Word> words = new ArrayList<>();
        for (int i = 0; i < wordArray.length(); i++) {
            words.add(Word.parse(wordArray.getString(i)));
        }

        for (Rule r : rules){
            if (r.getSection().equals("all")){
                r.getWords().addAll(words);
            }
            for (Word w : words){
                if (r.getSection().equals(w.getSection())){
                    r.getWords().add(w);
                }
            }
        }

        for (Rule r : rules){
            if (r.getParent() != null){
                r.getParent().getWords().addAll(r.getWords());
            }
        }

        int count = 1;
        for (var rule : rules) {
            rule.setPageNumber((byte) (count/ Rule.pageCountRule));
            count++;
        }

        for (Rule r : rules){
            logger.info("in rule " + r.getName() + " added " + r.getWords().size() + "  words");
        }
    }

    /**
     * get string from resource folder file
     * @param fileName name of file to search
     * @return string from file that placed in resource folder
     */
    private String getStringFromResources(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        StringWriter writer = new StringWriter();
        IOUtils.copy(Objects.requireNonNull(inputStream), writer);
        return writer.toString();
    }

    private Random random = new Random();

    /**
     * @return random string with remark about error
     */
    public String getRandomWrongMessage(){
        return (String) wrongMessages.toArray()[random.nextInt(wrongMessages.size())];
    }

    public ArrayList<Rule> getRules(){
        return rules;
    }

    public Rule getRule(String rule){
        for (Rule r : rules){
            if (r.getName().equals(rule)){
                return r;
            }
        }
        return null;
    }
}