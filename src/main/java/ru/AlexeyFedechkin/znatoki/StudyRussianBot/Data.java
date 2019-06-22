package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;

import java.io.IOException;

public class Data {
    private final Logger logger = Logger.getLogger(Data.class);
    private static final Data ourInstance = new Data();

    public static Data getInstance() {
        return ourInstance;
    }

    private Data() {
        wordManager = new WordManager();
        try {
            wordManager.init();
        } catch (IOException e) {
            logger.fatal("load data fail", e);
        }
    }

    public final WordManager wordManager;

    public WordManager getWordManager() {
        return wordManager;
    }
}
