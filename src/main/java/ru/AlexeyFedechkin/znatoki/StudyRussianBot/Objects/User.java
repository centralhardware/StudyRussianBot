/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Data;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

import java.util.*;

/**
 *Data about user
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

    public void reset() {
        status = UserStatus.NONE;
        words.clear();
        wrongWords.clear();
        count = 0;
    }


    public List<Word> getWords() {
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

    public List<Word> getWrongWords() {
        return wrongWords;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    private final Resource resource = new Resource();

    /**
     * get string with result of testing for last rule task
     * @return result string
     */
    public String getTestingResult() {
        var builder = new StringBuilder();
        builder.append(resource.getStringByKey("STR_10")).append(count).append("\n");
        builder.append(resource.getStringByKey("STR_11")).append("\n");
        HashMap<String, Integer> result = new HashMap<>();
        for (var word : wrongWords){
            if (!result.containsKey(word.getWrightName())){
                result.put(word.getWrightName(), 1);
            } else {
                result.put(word.getWrightName(), result.get(word.getWrightName()) + 1);
            }
        }
        for (var key : result.keySet()){
            builder.append(key).append(" - ").append(result.get(key)).append("\n");
        }
        builder.append("всего ").append(result.size()).append(" слов").append("\n");
        return builder.toString();
    }

    /**
     * get String with data about profile of user
     *
     * @return string with profile data
     */
    public String getProfile(){
        int rightPercent;
        double wright = Redis.getInstance().getCountOfCheckedWord(chatId);
        double wrong = Redis.getInstance().getCountOfWrongCheckedWord(chatId);
        if (wrong != 0 && wright < wrong) {
            rightPercent = (int) ((wright / wrong) * 100);
        } else {
            rightPercent = 100;
        }
        var builder = new StringBuilder();
        builder.append(resource.getStringByKey("STR_12")).append("\n").
                append(resource.getStringByKey("STR_13")).append(Redis.getInstance().getCountOfSentMessage(chatId)).append("\n")
                .append(resource.getStringByKey("STR_14")).append(Redis.getInstance().getCountOfReceivedMessage(chatId)).append("\n").
                append(resource.getStringByKey("STR_15")).append(Redis.getInstance().getCountOfCheckedWord(chatId)).append("\n").
                append(resource.getStringByKey("STR_45")).append(Redis.getInstance().getCountOfWrongCheckedWord(chatId)).append("\n").
                append(resource.getStringByKey("STR_46")).append(rightPercent).append("%").append("\n").
                append(resource.getStringByKey("STR_16")).append("\n");
        for (var rule : Data.getInstance().getWordManager().getRules()){
            if (Redis.getInstance().isCheckRule(chatId, rule.getName())) {
                builder.append(" - ").append("\"").append(rule.getName()).append("\"").append("\n");
            }
        }
        if (Config.getInstance().getAdminsId().contains(chatId)){
            builder.append(resource.getStringByKey("STR_35")).append(resource.getStringByKey("STR_37"));
        } else if (Redis.getInstance().checkRight(chatId)) {
            builder.append(resource.getStringByKey("STR_35")).append(resource.getStringByKey("STR_38"));
        } else {
            builder.append(resource.getStringByKey("STR_35")).append(resource.getStringByKey("STR_39"));
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return chatId == user.chatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", currRule=" + currRule +
                ", status=" + status +
                ", words=" + words +
                ", wrongWords=" + wrongWords +
                ", count=" + count +
                '}';
    }
}