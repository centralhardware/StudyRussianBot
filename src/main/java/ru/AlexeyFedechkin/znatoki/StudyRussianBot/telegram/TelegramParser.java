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
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Word;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Statistic;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Chart;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.RSA;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

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
    private final InlineKeyboard inlineKeyboard;
    private final Chart chart = new Chart();

    public HashMap<Long, User> getUsers() {
        return users;
    }

    /**
     * set telegramBot and create InlineKeyboard
     * @param telegramBot instance of register bot
     */
    public TelegramParser(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        inlineKeyboard = new InlineKeyboard(telegramBot);
    }

    /**
     * parse text message
     * @param update received message
     */
    public void parseText(Update update) {
        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();
        var user = users.get(chatId);
        Redis.getInstance().received(chatId);
        switch (message) {
            case "/start":
                user.reset();
                telegramBot.send(resource.getStringByKey("START_MESSAGE"),
                        update.getMessage().getChatId());
                inlineKeyboard.sendMenu(chatId);
                break;
            case "/help":
                telegramBot.send(resource.getStringByKey("HELP_MESSAGE"), chatId);
                break;
            case "/rules":
                inlineKeyboard.sendRuleInlineKeyboard(update, 0);
                break;
            case "/book":
                inlineKeyboard.sendBookInlineKeyBoard(update, 0);
                break;
            case "/profile":
                telegramBot.send(user.getProfile(), chatId);
                break;
            default:
                if (message.startsWith("/gen ")) {
                    if (Config.getInstance().getAdminsId().contains(chatId)) {
                        if (message.replace("/gen ", "").isEmpty()) {
                            telegramBot.send(resource.getStringByKey("STR_31"), chatId);
                            return;
                        }
                        telegramBot.send(rsa.generateKey(message.replace("/gen ", "")), chatId);
                    } else {
                        telegramBot.send(resource.getStringByKey("STR_47"), update.getMessage().getChatId());
                    }
                }
                if (message.startsWith("/ver ")) {
                    var args = message.replace("/ver ", "").split(" ");
                    String key = args[0];
                    String msg = args[1];
                    if (Config.getInstance().getAdminsId().contains(chatId)) {
                        telegramBot.send(String.valueOf(rsa.validateKey(msg, key)), chatId);
                    } else {
                        telegramBot.send(resource.getStringByKey("STR_47"), update.getMessage().getChatId());
                    }
                }
                if (message.startsWith("/stat")) {
                    sendStatistic(chatId);
                }
                switch (user.getStatus()) {
                    case WAIT_COUNT_OF_WORD:
                        int count;
                        try {
                            count = Integer.parseInt(message);
                            if (count <= 0) {
                                telegramBot.send(resource.getStringByKey("STR_43"), chatId);
                                return;
                            }
                            user.setCount(count);
                            if (count > user.getCurrRule().getWords().size()) {
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
                    case TESTING:
                        if (user.getWords().get(0).getAnswer().toLowerCase().equals(message.toLowerCase())) {
                            telegramBot.send(resource.getStringByKey("STR_3"), chatId);
                            user.getWords().remove(0);
                            if (user.getWords().size() == 0) {
                                telegramBot.send(resource.getStringByKey("STR_4"), chatId);
                                telegramBot.send(user.getTestingResult(), chatId);
                                Redis.getInstance().checkRule(user);
                                inlineKeyboard.sendMenu(chatId);
                                user.reset();
                                return;
                            }
                            telegramBot.send(user.getWords().get(0).getName(), chatId);
                            Redis.getInstance().checkWord(user);
                        } else {
                            telegramBot.send(resource.getStringByKey("STR_5"), chatId);
                            Redis.getInstance().checkWrongWord(user);
                            Word temp = user.getWords().get(0);
                            user.getWords().remove(0);
                            user.getWords().add(temp);
                            user.getWrongWords().add(temp);
                            telegramBot.send(user.getWords().get(0).getName(), chatId);
                        }
                        break;
                }
        }
    }

    /**
     * parse callback
     * @param update received message
     */
        public void parsCallback (Update update){
            var callback = update.getCallbackQuery().getData();
            var chatId = update.getCallbackQuery().getMessage().getChatId();
            var user = users.get(chatId);
            Redis.getInstance().received(chatId);
            switch (callback){
                case "reset_testing":
                    telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    user.reset();
                    inlineKeyboard.sendMenu(chatId);
                    break;
                case "noreset_testing":
                    telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    telegramBot.send(user.getWords().get(0).getName(), chatId);
                    return;
                case "testing":
                    inlineKeyboard.sendRuleInlineKeyboard(update, 0);
                    break;
                case "profile":
                    telegramBot.send(user.getProfile(),chatId);
                    break;
                case "help":
                    if (!(Redis.getInstance().checkRight(chatId) || Config.getInstance().getAdminsId().contains(chatId))) {
                        telegramBot.send(resource.getStringByKey("STR_32"), chatId);
                    } else {
                        telegramBot.send(resource.getStringByKey("HELP_MESSAGE"), chatId);
                    }
                    break;
                case "menu":
                    if (user.getStatus() != UserStatus.TESTING && user.getStatus() != UserStatus.WAIT_COUNT_OF_WORD) {
                        telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                        inlineKeyboard.sendMenu(chatId);
                    }
                    break;
                case "enter_key":
                    telegramBot.send(resource.getStringByKey("STR_22"), chatId);
                    user.setStatus(UserStatus.WAIT_KEY);
                    return;
                case "login":
                    telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    inlineKeyboard.sendLoginInfo(chatId);
                    return;
                case "buy_key":
//                    telegramBot.send(resource.getStringByKey("STR_33"), chatId);
                    return;
                case "book":
                    telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    inlineKeyboard.sendBookInlineKeyBoard(update, 0);
                    return;
                case "report":
                    user.setStatus(UserStatus.WAIT_REPORT);
                    telegramBot.send(resource.getStringByKey("STR_62"), chatId);
                    return;
                case "statistic":
                    sendStatistic(chatId);
                    return;
            }
            if (callback.startsWith("to_")){
                if (user.getStatus() != UserStatus.WAIT_COUNT_OF_WORD && user.getStatus() != UserStatus.TESTING) {
                    inlineKeyboard.sendRuleInlineKeyboard(update, Integer.parseInt(callback.replace("to_", "")));
                }
            }
            if (callback.startsWith("book_to_")){
                inlineKeyboard.sendBookInlineKeyBoard(update, Integer.parseInt(callback.replace("book_to_", "")));
            }
            if (callback.startsWith("book" ) && !callback.startsWith("book_to_")){
                telegramBot.send(Data.getInstance().getWordManager().getRuleDescriptionById(Integer.parseInt(callback.replace("book", ""))).getDescription(), chatId);
                var builder = InlineKeyboardBuilder.
                        create(chatId).setText(resource.getStringByKey("STR_18")).
                        row().
                        button("↑", "book").
                        endRow();
                telegramBot.send(builder.build());
            }
            if (user.getStatus() == UserStatus.NONE) {
                for (var rule : Data.getInstance().getWordManager().getRules()) {
                    if (rule.getSection().equals(callback)) {
                        user.setStatus(UserStatus.WAIT_COUNT_OF_WORD);
                        user.setCurrRule(rule);
                        telegramBot.send(resource.getStringByKey("STR_6") + rule.getName(), chatId);
                        telegramBot.send(resource.getStringByKey("STR_7"), chatId);
                        return;
                    }
                }
            } else {
                telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
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
     * send to admin bot statistic
     *
     * @param chatId id of admin user
     */
    private void sendStatistic(Long chatId) {
        if (Config.getInstance().getAdminsId().contains(chatId)) {
            var userStatistic = Statistic.getInstance().getStatistic();
            String statString = resource.getStringByKey("STR_48") + "\n" +
                    resource.getStringByKey("STR_49") + userStatistic.getTotalCountOfSend() + "\n" +
                    resource.getStringByKey("STR_50") + userStatistic.getTotalCountReceived() + "\n" +
                    resource.getStringByKey("STR_51") + userStatistic.getUserReceived().size() + "\n";
            telegramBot.send(statString, chatId);
            try {
                telegramBot.send(chart.genOneLineGraf(resource.getStringByKey("STR_52"),
                        resource.getStringByKey("STR_53"),
                        userStatistic.listToArray(userStatistic.getTotalReceived()),
                        userStatistic.getXData(userStatistic.getTotalReceived().size())), chatId);
                telegramBot.send(chart.genOneLineGraf(resource.getStringByKey("STR_55"),
                        resource.getStringByKey("STR_56"),
                        userStatistic.listToArray(userStatistic.getTotalSend()),
                        userStatistic.getXData(userStatistic.getTotalSend().size())), chatId);
            } catch (Exception e) {
                logger.warn("error while generate graf", e);
                telegramBot.send(resource.getStringByKey("STR_57"), chatId);
            }
        } else {
            telegramBot.send(resource.getStringByKey("STR_47"), chatId);
        }
    }
}