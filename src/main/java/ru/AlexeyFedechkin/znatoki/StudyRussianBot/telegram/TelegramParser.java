/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 15:53
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.*;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Word;

import java.util.HashMap;

/**
 * parse message from telegram
 */
public class TelegramParser {
    private final Logger logger = Logger.getLogger(TelegramParser.class);
    private final TelegramBot telegramBot;
    private final HashMap<Long, User> users = new HashMap<>();
    private final Resource resource = new Resource();
    private final RSA rsa = new RSA();

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
        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();
        var user = users.get(chatId);
        JedisData.getInstance().received(chatId);
        switch (message) {
            case "/start":
                telegramBot.send(Config.getInstance().getStartupMessage(),
                        update.getMessage().getChatId());
                sendMenu(chatId);
                break;
            case "/help":
                telegramBot.send(Config.getInstance().getHelpMessage(), chatId);
                break;
            case "/rules":
                sendRuleInlineKeyboard(update, 0);
                break;
            case "/profile":
                sendProfile(user);
                break;
            case "ping":
                telegramBot.send("pong", chatId);
                break;
            case "pong":
                telegramBot.send(":(", chatId);
                break;
            default:
                if (message.startsWith("/gen ")){
                    if (update.getMessage().getFrom().getId() == Config.getInstance().getAdminId()){
                        telegramBot.send(rsa.generateKey(message.replace("/gen ", "")), chatId);

                    } else {
                        telegramBot.send("access denied", update.getMessage().getChatId());
                    }
                } else if (message.startsWith("/ver ")) {
                    var args = message.replace("/ver ", "").split(" ");
                    String key = args[0];
                    String msg = args[1];
                    if (update.getMessage().getFrom().getId() == Config.getInstance().getAdminId()){
                        telegramBot.send(String.valueOf(rsa.validateKey(msg, key)), chatId);
                    } else {
                        telegramBot.send("access denied", update.getMessage().getChatId());
                    }
                }
                if (user.getStatus() == UserStatus.WAIT_COUNT_OF_WORD) {
                    int count;
                    try {
                        count = Integer.parseInt(message);
                        user.setCount(count);
                        if (count > user.getCurrRule().getWords().size()){
                            telegramBot.send(resource.getStringByKey("STR_1"), chatId);
                            user.setStatus(UserStatus.NONE);
                            user.getWords().clear();
                        } else {
                            user.setStatus(UserStatus.TESTING);
                            user.getWords().addAll(user.getCurrRule().getWord(count));
                            telegramBot.send(user.getWords().get(0).getName(), chatId);
                        }
                    } catch (NumberFormatException e) {
                        telegramBot.send(resource.getStringByKey("STR_2"), chatId);
                    }
                    break;
                } else if (user.getStatus() == UserStatus.TESTING){
                    if (user.getWords().get(0).getAnswer().toLowerCase().equals(message.toLowerCase())){
                        telegramBot.send(resource.getStringByKey("STR_3"), chatId);
                        user.getWords().remove(0);
                        if (user.getWords().size() == 0){
                            telegramBot.send(resource.getStringByKey("STR_4"), chatId);
                            telegramBot.send(user.getResult(), chatId);
                            JedisData.getInstance().checkRule(user);
                            sendMenu(chatId);
                            user.reset();
                            return;
                        }
                        telegramBot.send(user.getWords().get(0).getName(), chatId);
                        JedisData.getInstance().checkWord(user);
                    } else{
                        telegramBot.send(resource.getStringByKey("STR_5"), chatId);
                        Word temp = user.getWords().get(0);
                        user.getWords().remove(0);
                        user.getWords().add(temp);
                        user.getWrongWords().add(temp);
                        telegramBot.send(user.getWords().get(0).getName(),chatId);
                    }
                }
        }
    }

    /**
     * parse callback
     * @param update
     */
        public void parsCallback (Update update){
            var callback = update.getCallbackQuery().getData();
            var chatId = update.getCallbackQuery().getMessage().getChatId();
            var user = users.get(chatId);
            JedisData.getInstance().received(chatId);
            switch (callback){
                case "reset_testing":
                    user.reset();
                    sendMenu(chatId);
                    break;
                case "noreset_testing":
                    telegramBot.send(user.getWords().get(0).getName(), chatId);
                    return;
                case "testing":
                    sendRuleInlineKeyboard(update, 0);
                    break;
                case "profile":
                    sendProfile(user);
                    break;
                case "help":
                    telegramBot.send(Config.getInstance().getHelpMessage(), chatId);
                    break;
                case "menu":
                    telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    sendMenu(chatId);
                    break;
                case "info":
                    return;
                case "enter_key":
                    telegramBot.send(resource.getStringByKey("STR_22"), chatId);
                    user.setStatus(UserStatus.WAIT_KEY);
                    return;
                case "login":
                    telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    sendLoginInfo(chatId);
                    return;
            }
            if (callback.startsWith("to_")){
                sendRuleInlineKeyboard(update, Integer.parseInt(callback.replace("to_", "")));
            }
            if (user.getStatus() == UserStatus.NONE) {
                for (var rule : Data.getInstance().getWordManager().getRules()) {
                    if (rule.getSection().equals(callback)) {
                        user.setStatus(UserStatus.WAIT_COUNT_OF_WORD);
                        user.setCurrRule(rule);
                        telegramBot.send(resource.getStringByKey("STR_6") + rule.getName() , chatId);
                        telegramBot.send(resource.getStringByKey("STR_7"), chatId);
                        return;
                    }
                }
            } else {
                var builder = InlineKeyboardBuilder.
                        create(chatId).
                        setText(resource.getStringByKey("STR_9")).
                        row().
                        button(resource.getStringByKey("YES"), "reset_testing").
                        button(resource.getStringByKey("NO"), "noreset_testing").
                        endRow();
                telegramBot.send(builder.build());
            }
        }

    /**
     * @param chatId
     */
    public void sendMenu(long chatId){
        logger.info("send inline keyboard menu");
        var builder = InlineKeyboardBuilder.
                create(chatId).
                setText(resource.getStringByKey("STR_24")).
                row().
                button(resource.getStringByKey("STR_23"), "testing").
                button(resource.getStringByKey("STR_25"), "profile").
                button(resource.getStringByKey("STR_26"), "help").
                endRow();
        if (!JedisData.getInstance().checkRight(chatId) && chatId != Config.getInstance().getAdminId()){
            builder.row().
                    button("получить полный доступ", "login").
                    endRow();
        }
        telegramBot.send(builder.build());
    }

    /**
     * send inlineKeyboard with list of available rule
     */
        private void sendRuleInlineKeyboard (Update update, int pageNumber){
            long chatId;
            String message = "";
            if (update.hasCallbackQuery()){
                chatId = update.getCallbackQuery().getMessage().getChatId();
            } else {
              chatId = update.getMessage().getChatId();
              message = update.getMessage().getText();
            }
            logger.info("send inline keyboard rules");
            var builder = InlineKeyboardBuilder.
                    create(chatId).setText(resource.getStringByKey("STR_8"));
            long userId;
            if (update.hasCallbackQuery()){
                userId = update.getCallbackQuery().getFrom().getId();
            } else {
                userId = update.getMessage().getFrom().getId();
            }
            if (!JedisData.getInstance().checkRight(userId) && userId != Config.getInstance().getAdminId()){
                for (var i = 0; i < 3; i++) {
                    var rule = Data.getInstance().wordManager.getRules().get(i);
                    builder.row();
                    if (JedisData.getInstance().isCheckRule(chatId, rule.getName())){
                        builder.button("✅" + rule.getName(), rule.getSection());
                    } else {
                        builder.button(rule.getName(), rule.getSection());
                    }
                    builder.endRow();
                }
            } else {
                for (var rule : Data.getInstance().getWordManager().getRules()) {
                    if (rule.getPageNumber() == pageNumber){
                        builder.row();
                        if (JedisData.getInstance().isCheckRule(chatId, rule.getName())){
                            builder.button("✅" + rule.getName(), rule.getSection());
                        } else {
                            builder.button(rule.getName(), rule.getSection());
                        }
                        builder.endRow();
                    }
                }
            }
            if (pageNumber == 0){
                builder.row().
                        button(resource.getStringByKey("STR_17"), "to_1").
                        button(resource.getStringByKey("STR_24"), "menu").
                        endRow();
                if (!message.equals("/rules")){
                    telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                }
            } else if (pageNumber < Rule.getMaxPage()){
                builder.row().
                        button(resource.getStringByKey("STR_18"), "to_" + (pageNumber - 1)).
                        button(resource.getStringByKey("STR_17") + (pageNumber + 1), "to_" + (pageNumber + 1)).
                        button(resource.getStringByKey("STR_24"), "menu").
                        endRow();
                telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                telegramBot.send(builder.build());
                return;
            } else if (pageNumber == Rule.getMaxPage()){
                builder.row().
                        button(resource.getStringByKey("STR_18"), "to_" + (pageNumber - 1)).
                        button(resource.getStringByKey("STR_24"), "menu").
                        endRow();
                telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                telegramBot.send(builder.build());
                return;
            }
            telegramBot.send(builder.build());
        }

    /**
     * send profile data
     * @param user the user for whom to send information about profile
     */
    private void sendProfile(User user) {
        var builder = new StringBuilder();
        builder.append(resource.getStringByKey("STR_12")).append("\n").
                append(resource.getStringByKey("STR_13")).append(JedisData.getInstance().getCountOfSentMessage(user.getChatId())).append("\n")
                .append(resource.getStringByKey("STR_14")).append(JedisData.getInstance().getCountOfReceivedMessage(user.getChatId())).append("\n").
                append(resource.getStringByKey("STR_15")).append(JedisData.getInstance().getCountOfCheckedWord(user.getChatId())).append("\n").
                append(resource.getStringByKey("STR_16")).append("\n");
        for (var rule : Data.getInstance().getWordManager().getRules()){
            if (JedisData.getInstance().isCheckRule(user.getChatId(), rule.getName())){
                builder.append(" - ").append("\"").append(rule.getName()).append("\"").append("\n");
            }
        }
        if (Config.getInstance().getAdminId() == user.getChatId()){
            builder.append("тип профиля:").append(" администратор");
        } else if  (JedisData.getInstance().checkRight(user.getChatId())){
            builder.append("тип профиля:").append(" полный доступ");
        } else {
            builder.append("тип профиля:").append(" демо доступ");
        }
        telegramBot.send(builder.toString(), user.getChatId());
    }

    /**
     * @param chatId
     */
    public void sendLoginInfo(long chatId){
        var builder = InlineKeyboardBuilder.create(chatId).
                setText(resource.getStringByKey("STR_28")).
                row().
                button(resource.getStringByKey("STR_29"), "enter_key").
                button(resource.getStringByKey("STR_30"), "info").
                endRow().
                row().
                button("демо", "menu").
                endRow();
        telegramBot.send(builder.build());
    }
}