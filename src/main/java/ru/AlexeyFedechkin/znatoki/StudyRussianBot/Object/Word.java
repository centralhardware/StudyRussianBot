/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object;

import org.apache.log4j.Logger;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.SchoolStage;

/**
 *
 */
public class Word {
    private static final Logger logger = Logger.getLogger(Word.class);

    private final String wrightName;
    private final String name;
    private final String section;
    private final String answer;

    public String getSection() {
        return section;
    }

    private final SchoolStage schoolStage;

    public Word(String wrightName, String name, String section, String answer, SchoolStage schoolStage) {
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
        String[] args = str.split(" ");
        if (args.length != 7){
            logger.warn("error in data file " + str);
        }
        String name = args[0].replace("%20", " ");
        String wrightName = args[1].replace("%20", " ");
        String answer = args[2];
        String section = args[3];
        SchoolStage stage = null;
        boolean isHigh = Boolean.parseBoolean(args[4]);
        boolean isSecondary = Boolean.parseBoolean(args[5]);
        boolean isPrimary = Boolean.parseBoolean(args[6]);
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
}