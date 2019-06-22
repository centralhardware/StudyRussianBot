package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Word;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class WordManager {
    private final Logger logger = Logger.getLogger(WordManager.class);
    private ArrayList<String> wrongMessages = new ArrayList<>();
    private ArrayList<Rule> rules = new ArrayList<>();

    /**
     * parseText all data file and generate collections of object
     * @throws IOException
     */
    public void init() throws IOException {
        File word = getFileFromResources("word.json");
        File rule = getFileFromResources("rule.json");
        File wrongMessage = getFileFromResources("wrongMessage.json");

        String wordString = FileUtils.readFileToString(word, "utf-8");
        String ruleString = FileUtils.readFileToString(rule, "utf-8");
        String wrongMessageString = FileUtils.readFileToString(wrongMessage, "utf-8");

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

        for (Rule r : rules){
            logger.info("in rule " + r.getName() + " added " + r.getWords().size() + "  words");
        }
    }

    /**
     * get File from resource folder
     * @param fileName name of file to search
     * @return
     */
    private File getFileFromResources(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile().replace("%20"," "));
        }
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
}