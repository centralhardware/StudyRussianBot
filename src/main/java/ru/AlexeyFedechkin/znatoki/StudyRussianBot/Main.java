package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    /**
     * @param args never use
     */
    public static void main(String[] args) {
        logger.info("program start");
        ApiContextInitializer.init();
        logger.info("api context init");
        TelegramBot telegramBot = new TelegramBot();
        telegramBot.init();
        logger.info("telegram bot run");
    }
}
