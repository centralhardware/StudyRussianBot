/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.telegram.TelegramBot;

@SuppressWarnings("HardCodedStringLiteral")
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    /**
     * init telegram ApiContext and telegram bot
     * @param args never use
     */
    public static void main(String[] args) {
        logger.info("program start");
        ApiContextInitializer.init();
        logger.info("api context init");
        new TelegramBot().init();
        logger.info("telegram bot run");
        Statistic.getInstance().init();
        logger.info("init statistic");
    }
}
