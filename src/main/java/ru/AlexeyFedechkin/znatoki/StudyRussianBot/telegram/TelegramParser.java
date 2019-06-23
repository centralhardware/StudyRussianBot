/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 15:53
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Data;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.JedisData;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Word;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Resource;

import java.util.HashMap;

/**
 * parse message from telegram
 */
public class TelegramParser {
    private final Logger logger = Logger.getLogger(TelegramParser.class);
    private final TelegramBot telegramBot;
    private final HashMap<Long, User> users = new HashMap<>();
    private final Resource resource = new Resource();

    public HashMap<Long, User> getUsers() {
        return users;
    }

    /**
     * @param telegramBot instance of register bot
     */
    public TelegramParser(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * parse text message
     * @param update
     */
    public void parseText(Update update) {
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        User user = users.get(chatId);
        JedisData.getInstance().received(chatId);
        switch (message) {
            case "/start":
                telegramBot.sendMessage(Config.getInstance().getStartupMessage(),
                        update.getMessage().getChatId());
                sendMenu(chatId);
                break;
            case "/info":
                telegramBot.sendMessage(Config.getInstance().getHelpMessage(), chatId);
                break;
            case "/rules":
                sendRuleInlineKeyboard(chatId);
                break;
            case "/profile":
                sendProfile(user);
                break;
            default:
                if (user.getStatus() == UserStatus.WAIT_COUNT_OF_WORD) {
                    int count;
                    try {
                        count = Integer.parseInt(message);
                        user.setCount(count);
                        if (count > user.getCurrRule().getWords().size()){
                            telegramBot.sendMessage(resource.getStringByKey("STR_1"), chatId);
                            user.setStatus(UserStatus.NONE);
                            user.getWords().clear();
                        } else {
                            user.setStatus(UserStatus.TESTING);
                            user.getWords().addAll(user.getCurrRule().getWord(count));
                            telegramBot.sendMessage(user.getWords().get(0).getName(), chatId);
                        }
                    } catch (NumberFormatException e) {
                        telegramBot.sendMessage(resource.getStringByKey("STR_2"), chatId);
                    }
                    break;
                } else if (user.getStatus() == UserStatus.TESTING){
                    if (user.getWords().get(0).getAnswer().toLowerCase().equals(message.toLowerCase())){
                        telegramBot.sendMessage(resource.getStringByKey("STR_3"), chatId);
                        user.getWords().remove(0);
                        if (user.getWords().size() == 0){
                            telegramBot.sendMessage(resource.getStringByKey("STR_4"), chatId);
                            telegramBot.sendMessage(user.getResult(), chatId);
                            JedisData.getInstance().checkRule(user);
                            sendMenu(chatId);
                            user.reset();
                            return;
                        }
                        telegramBot.sendMessage(user.getWords().get(0).getName(), chatId);
                        JedisData.getInstance().checkWord(user);
                    } else{
                        telegramBot.sendMessage(resource.getStringByKey("STR_5"), chatId);
                        Word temp = user.getWords().get(0);
                        user.getWords().remove(0);
                        user.getWords().add(temp);
                        user.getWrongWords().add(temp);
                        telegramBot.sendMessage(user.getWords().get(0).getName(),chatId);
                    }
                }
        }
    }

    /**
     * parse callback
     * @param update
     */
        public void parsCallback (Update update){
            String callback = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            User user = users.get(chatId);
            JedisData.getInstance().received(chatId);
            switch (callback){
                case "reset_testing":
                    user.reset();
                    sendMenu(chatId);
                    break;
                case "noreset_testing":
                    telegramBot.sendMessage(user.getWords().get(0).getName(), chatId);
                    return;
                case "testing":
                    sendRuleInlineKeyboard(chatId);
                    break;
                case "profile":
                    sendProfile(user);
                    break;
                case "help":
                    telegramBot.sendMessage(Config.getInstance().getHelpMessage(), chatId);
                    break;
            }
            if (user.getStatus() == UserStatus.NONE) {
                for (Rule rule : Data.getInstance().getWordManager().getRules()) {
                    if (rule.getSection().equals(callback)) {
                        user.setStatus(UserStatus.WAIT_COUNT_OF_WORD);
                        user.setCurrRule(rule);
                        telegramBot.sendMessage(resource.getStringByKey("STR_6") + rule.getName() , chatId);
                        telegramBot.sendMessage(resource.getStringByKey("STR_7"), chatId);
                        return;
                    }
                }
            } else {
                InlineKeyboardBuilder builder = InlineKeyboardBuilder.
                        create(chatId).
                        setText(resource.getStringByKey("STR_9")).
                        row().
                        button(resource.getStringByKey("YES"), "reset_testing").
                        button(resource.getStringByKey("NO"), "noreset_testing").
                        endRow();
                telegramBot.sendMessage(builder.build());
            }
        }

    private void sendMenu(long chatId){
        logger.info("send inline keyboard menu");
        InlineKeyboardBuilder builder = InlineKeyboardBuilder.
                create(chatId).
                setText("меню").
                row().
                button("выбор правила", "testing").
//                endRow().
//                row().
                button("профиль", "profile").
//                endRow().
//                row().
                button("справка", "help").
                endRow();
        telegramBot.sendMessage(builder.build());
    }

    /**
     * send inlineKeyboard with list of available rule
     * @param chatId id of telegram user
     */
        private void sendRuleInlineKeyboard (long chatId){
            logger.info("send inline keyboard rules");
            InlineKeyboardBuilder builder = InlineKeyboardBuilder.
                    create(chatId).setText(resource.getStringByKey("STR_8"));
            for (Rule rule : Data.getInstance().getWordManager().getRules()) {
                builder.row();
                if (JedisData.getInstance().isCheckRule(chatId, rule.getName())){
                    builder.button("✅" + rule.getName(), rule.getSection());
                } else {
                    builder.button(rule.getName(), rule.getSection());
                }
                builder.endRow();
            }
            telegramBot.sendMessage(builder.build());
        }

    private void sendProfile(User user) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("статистика:").append("\n").
                append("сообщений отправлено: ").append(JedisData.getInstance().getCountOfSentMessage(user.getChatId())).append("\n")
                .append("сообщений принято: ").append(JedisData.getInstance().getCountOfReceivedMessage(user.getChatId())).append("\n").
                append("слов отвечено правильно: ").append(JedisData.getInstance().getCountOfCheckedWord(user.getChatId())).append("\n").
                append("пройденные правила:").append("\n");
        for (Rule rule : Data.getInstance().getWordManager().getRules()){
            if (JedisData.getInstance().isCheckRule(user.getChatId(), rule.getName())){
                stringBuilder.append(" - ").append("\"").append(rule.getName()).append("\"").append("\n");
            }
        }
        telegramBot.sendMessage(stringBuilder.toString(), user.getChatId());
    }
}