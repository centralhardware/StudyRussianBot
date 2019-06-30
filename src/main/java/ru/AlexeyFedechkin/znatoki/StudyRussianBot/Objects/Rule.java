/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 *Data about rule
 */
public class Rule {
    public String getName() {
        return name;
    }

    private final String name;
    private Rule parent;
    private final String section;
    private ArrayList<Word> words;
    private byte pageNumber;
    public static final int pageCountRule = 10;

    public String getSection() {
        return section;
    }

    public Rule(String name, Rule parent, String section, ArrayList<Word> words) {
        this.name = name;
        this.parent = parent;
        this.words = words;
        this.section = section;
    }

    public byte getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(byte pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Rule getParent() {
        return parent;
    }

    public void setParent(Rule parent) {
        this.parent = parent;
    }

    public ArrayList<Word> getWords() {
        return words;
    }

    private final Random random = new Random();

    /**
     * get max number of page
     * @return max count of page
     */
    public static int getMaxPage(){
        int max = 0;
        for (var rule : Data.getInstance().getWordManager().getRules()) {
            if (rule.getPageNumber() > max){
                max = rule.getPageNumber();
            }
        }
        return max;
    }

    /**
     * get Collection with giving count of word
     *
     * @param count needed count of word
     * @return Collection of word
     */
    public Collection<Word> getWord(int count) {
        HashSet<Word> wordSet = new HashSet<>();
        while (wordSet.size() < count) {
            wordSet.add(words.get(random.nextInt(words.size())));
        }
        return new ArrayList<>(wordSet);
    }
}
