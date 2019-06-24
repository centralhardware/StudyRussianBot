/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object;

import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Resource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class User {
    private final long chatId;
    private Rule currRule;
    private UserStatus status;
    private final ArrayList<Word> words;
    private final ArrayList<Word> wrongWords;
    private int count = 0;

    public User(long chatId){
        this.chatId = chatId;
        this.status = UserStatus.NONE;
        words = new ArrayList<>();
        wrongWords = new ArrayList<>();
    }


    public ArrayList<Word> getWords() {
        return words;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public long getChatId() {
        return chatId;
    }

    public Rule getCurrRule() {
        return currRule;
    }

    public void setCurrRule(Rule currRule) {
        this.currRule = currRule;
    }

    public ArrayList<Word> getWrongWords() {
        return wrongWords;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void reset() {
        status = UserStatus.NONE;
        words.clear();
        wrongWords.clear();
        count = 0;
    }

    private final Resource resource = new Resource();
    public String getResult() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(resource.getStringByKey("STR_10")).append(count).append("\n");
        stringBuilder.append(resource.getStringByKey("STR_11")).append("\n");
        HashMap<String, Integer> result = new HashMap<>();
        for (Word word : wrongWords){
            if (!result.containsKey(word.getWrightName())){
                result.put(word.getWrightName(), 1);
            } else {
                result.put(word.getWrightName(), result.get(word.getWrightName()) + 1);
            }
        }
        for (String key : result.keySet()){
            stringBuilder.append(key).append(" - ").append(result.get(key)).append("\n");
        }
        stringBuilder.append("всего ").append(result.size()).append(" слов").append("\n");
        return stringBuilder.toString();
    }
}