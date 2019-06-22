package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 *
 */
public class Rule {
    public String getName() {
        return name;
    }

    private final String name;
    private Rule parent;
    private final String section;
    private ArrayList<Word> words;

    public String getSection() {
        return section;
    }

    public Rule(String name, Rule parent, String section, ArrayList<Word> words) {
        this.name = name;
        this.parent = parent;
        this.words = words;
        this.section = section;
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

    private Random random = new Random();
    public  Collection<Word> getWord(int count) {
        HashSet<Word> wordSet = new HashSet<>();
        while (wordSet.size() < count){
            wordSet.add(words.get(random.nextInt(words.size())));
        }
        return new ArrayList<>(wordSet);
    }
}
