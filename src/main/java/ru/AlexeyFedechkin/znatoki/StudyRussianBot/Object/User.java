package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object;

import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.UserStatus;

import java.util.ArrayList;

/**
 *
 */
public class User {
    private long chatId;
    private Rule currRule;
    private UserStatus status;
    private ArrayList<Word> words;
    private ArrayList<Word> wrongWords;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void reset() {
        status = UserStatus.NONE;
        words.clear();
        wrongWords.clear();
    }

    public String getResult() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("всего слов в тестирование " + count + "\n");
        stringBuilder.append("слова в которых допущены ошибки" + "\n");
        for (Word words : wrongWords){
            stringBuilder.append(words.getWrightName() + "\n");
        }
        stringBuilder.append("всего " + wrongWords.size() + " слов" + "\n");
        return stringBuilder.toString();
    }
}