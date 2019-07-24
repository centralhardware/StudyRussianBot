/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;


import java.io.IOException;

/**
 * provides access to instance WordManager
 */
public class Data {
    private static final Logger logger = Logger.getLogger(Data.class);
    private static final Data ourInstance = new Data();

    public static Data getInstance() {
        return ourInstance;
    }

    /**
     * init wordManager
     * throw runtimeException if load data fail
     */
    private Data() {
        wordManager = new WordManager();
        try {
            wordManager.init();
            logger.info("data load");
        } catch (IOException e) {
            logger.fatal("load data fail", e);
            throw new RuntimeException();
        }
    }

    private final WordManager wordManager;

    public WordManager getWordManager() {
        return wordManager;
    }
}
