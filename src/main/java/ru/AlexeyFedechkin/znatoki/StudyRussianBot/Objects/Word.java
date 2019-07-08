/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import org.apache.log4j.Logger;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.SchoolStage;

import java.util.Objects;

/**
 *Data about word
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Word {
    private static final Logger logger = Logger.getLogger(Word.class);

    private final String wrightName;
    private final String name;
    private final String section;
    private final String answer;
    private final SchoolStage schoolStage;

    private Word(String wrightName, String name, String section, String answer, SchoolStage schoolStage) {
        this.wrightName = wrightName;
        this.name = name;
        this.answer = answer;
        this.schoolStage = schoolStage;
        this.section = section;
    }

    /**
     * parseText string from json file to object
     * name + wright name + answer + section + isHigh + isSecondary + isPrimary
     * example: суб_ективный субъективный ъ r13 1 1 0
     * splitter: space
     * %20 = space
     * @param str string for parseText
     * @return object crated from parsing string
     */
    public static Word parse(String str){
        logger.info("parsing string " + str);
        var args = str.split(" ");
        if (args.length != 7){
            logger.warn("error in data file " + str);
            return null;
        }
        var name = args[0].replace("%20", " ");
        var wrightName = args[1].replace("%20", " ");
        var answer = args[2];
        var section = args[3];
        SchoolStage stage = null;
        var isHigh = Boolean.parseBoolean(args[4]);
        var isSecondary = Boolean.parseBoolean(args[5]);
        var isPrimary = Boolean.parseBoolean(args[6]);
        if (isHigh && isSecondary) {
            stage = SchoolStage.SECONDARY_AND_HIGH;
        }
        return new Word(wrightName, name, section, answer, stage);
    }

    public String getWrightName() {
        return wrightName;
    }

    public String getName() {
        return name;
    }

    public String getAnswer() {
        return answer;
    }

    public SchoolStage getSchoolStage() {
        return schoolStage;
    }

    public String getSection() {
        return section;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return wrightName.equals(word.wrightName) &&
                name.equals(word.name) &&
                section.equals(word.section) &&
                answer.equals(word.answer) &&
                schoolStage == word.schoolStage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrightName, name, section, answer, schoolStage);
    }

    @Override
    public String toString() {
        return "Word{" +
                "wrightName='" + wrightName + '\'' +
                ", name='" + name + '\'' +
                ", section='" + section + '\'' +
                ", answer='" + answer + '\'' +
                ", schoolStage=" + schoolStage +
                '}';
    }
}