/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 15:53
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Data;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Word;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.Interfaces.TelegramParserInt;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.RSA;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * parse message from telegram
 */
public class TelegramParser implements TelegramParserInt {
    private final Sender sender;
    private final Resource resource = new Resource();
    private final RSA rsa = new RSA();
    private final InlineKeyboard inlineKeyboard;
    private final BotUtil botUtil;
    private final HashMap<Long, User> users = new HashMap<>();

    public Map<Long, User> getUsers() {
        return users;
    }

    /**
     * set telegramBot and create InlineKeyboard
     * @param sender
     */
    public TelegramParser(Sender sender) {
        this.sender = sender;
        inlineKeyboard = new InlineKeyboard(sender);
        botUtil = new BotUtil(sender);
    }

    /**
     * parse text message
     * support command
     * - /start: start command. also reset current testing.
     * - /help: show help message.
     * - /rules: show choose rule inline menu
     * - /profile: show profile data
     * - /menu: show menu
     * - /gen: generate activated code. Param: userName. only for admin
     * - /ver: verify activated code. Param: key userName. only for admin
     * - /stat: show bot statistic. only for admin
     * if message have not command: depending on status
     * - WAIT_COUNT_OF_WORD: start testing with giving count of word
     * - TESTING: message is answer on word with missing later. if answer is wright - send next word or result
     * if answer is wrong send "неправильно" and send next word. current word moves to the end of the word queue
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
                if (Redis.getInstance().checkRight(chatId)){
                    sender.send(resource.getStringByKey("START_MESSAGE"),
                            update.getMessage().getChatId());
                    inlineKeyboard.sendMenu(chatId);
                } else {
                    sender.send(resource.getStringByKey("START_MESSAGE"),
                            update.getMessage().getChatId());
                    inlineKeyboard.sendLoginInfo(chatId);
                }
                break;
            case "/help":
                sender.send(resource.getStringByKey("HELP_MESSAGE"), chatId);
                break;
            case "/rules":
                inlineKeyboard.sendRuleInlineKeyboard(update, 0);
                break;
            case "/book":
                inlineKeyboard.sendBookInlineKeyBoard(update, 0);
                break;
            case "/profile":
                sender.send(user.getProfile(), chatId);
                break;
            case "/menu":
                inlineKeyboard.sendMenu(chatId);
            default:
                if (message.startsWith("/gen ")) {
                    if (Config.getAdminsId().contains(chatId)) {
                        if (message.replace("/gen ", "").isEmpty()) {
                            sender.send(resource.getStringByKey("STR_31"), chatId);
                            return;
                        }
                        sender.send(rsa.generateKey(message.replace("/gen ", "")), chatId);
                    } else {
                        sender.send(resource.getStringByKey("STR_47"), update.getMessage().getChatId());
                    }
                } else if (message.startsWith("/ver ")) {
                    var args = message.replace("/ver ", "").split(" ");
                    String key = args[0];
                    String msg = args[1];
                    if (Config.getAdminsId().contains(chatId)) {
                        sender.send(String.valueOf(rsa.validateKey(msg, key)), chatId);
                    } else {
                        sender.send(resource.getStringByKey("STR_47"), update.getMessage().getChatId());
                    }
                } else if (message.startsWith("/stat")) {
                    botUtil.sendStatistic(chatId);
                } else if(message.startsWith("/")) {
                    sender.send(resource.getStringByKey("STR_101"), chatId);
                }
                switch (user.getStatus()) {
                    case WAIT_COUNT_OF_WORD:
                        int count;
                        try {
                            count = Integer.parseInt(message);
                            if (count <= 0) {
                                sender.send(resource.getStringByKey("STR_43"), chatId);
                                return;
                            }
                            user.setCount(count);
                            if (count > user.getCurrRule().getWords().size()) {
                                sender.send(resource.getStringByKey("STR_1"), chatId);
                                user.setStatus(UserStatus.NONE);
                                user.getWords().clear();
                            } else {
                                user.setStatus(UserStatus.TESTING);
                                user.getWords().addAll(user.getCurrRule().getWord(count));
                                sender.send(user.getWords().get(0).getName(), chatId);
                            }
                        } catch (NumberFormatException e) {
                            sender.send(resource.getStringByKey("STR_2"), chatId);
                        }
                        break;
                    case TESTING:
                        if (user.getWords().get(0).getAnswer().toLowerCase().equals(message.toLowerCase())) {
                            sender.send(resource.getStringByKey("STR_3"), chatId);
                            user.getWords().remove(0);
                            if (user.getWords().isEmpty()) {
                                sender.send(resource.getStringByKey("STR_4"), chatId);
                                sender.send(user.getTestingResult(), chatId);
                                Redis.getInstance().checkRule(user);
                                inlineKeyboard.sendMenu(chatId);
                                user.reset();
                                return;
                            }
                            sender.send(user.getWords().get(0).getName(), chatId);
                            Redis.getInstance().checkWord(user);
                        } else {
                            sender.send(resource.getStringByKey("STR_5"), chatId);
                            Redis.getInstance().checkWrongWord(user);
                            Word temp = user.getWords().get(0);
                            user.getWords().remove(0);
                            user.getWords().add(temp);
                            user.getWrongWords().add(temp);
                            sender.send(user.getWords().get(0).getName(), chatId);
                        }
                        break;
                }
        }
    }

    /**
     * parse callback
     * support action:
     * - reset_testing - change status to NONE
     * - noreset_testing - rejection cancellation. delete message and send current word again
     * - testing - start testing. for demo access aviable only tree rule.
     * - profile - show profile data
     * - help - show help message.
     * - menu - show menu
     * - enter_key - set status to wait_key and waiting input of activated code. only for demo access
     * - login - send login inline menu. only for demo access.
     * - buy_key - send message with data about buy access. only for demo access.
     * - book - show rule help. for demo aviable only tree rule description
     * - report - send message to admins. only for demo or full access
     * - statistic - show statistic. only for admin
     * - to_$pageNumber - show page of rule
     * - book_to_#pageNumber - show page of rule description
     * @param update received message
     */
        public void parsCallback (Update update){
            var callback = update.getCallbackQuery().getData();
            var chatId = update.getCallbackQuery().getMessage().getChatId();
            var user = users.get(chatId);
            Redis.getInstance().received(chatId);
            switch (callback){
                case "reset_testing":
                    sender.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    user.reset();
                    inlineKeyboard.sendMenu(chatId);
                    break;
                case "noreset_testing":
                    sender.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    sender.send(user.getWords().get(0).getName(), chatId);
                    return;
                case "testing":
                    inlineKeyboard.sendRuleInlineKeyboard(update, 0);
                    break;
                case "profile":
                    sender.send(user.getProfile(),chatId);
                    break;
                case "help":
                    if ((Redis.getInstance().checkRight(chatId) || Config.getAdminsId().contains(chatId))) {
                        sender.send(resource.getStringByKey("HELP_MESSAGE"), chatId);
                    } else {
                        sender.send(resource.getStringByKey("STR_32"), chatId);
                    }
                    break;
                case "menu":
                    if (user.getStatus() != UserStatus.TESTING && user.getStatus() != UserStatus.WAIT_COUNT_OF_WORD) {
                        sender.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                        inlineKeyboard.sendMenu(chatId);
                    }
                    break;
                case "enter_key":
                    sender.send(resource.getStringByKey("STR_22"), chatId);
                    user.setStatus(UserStatus.WAIT_KEY);
                    return;
                case "login":
                    sender.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    inlineKeyboard.sendLoginInfo(chatId);
                    return;
                case "buy_key":
//                    telegramBot.send(resource.getStringByKey("STR_33"), chatId);
                    return;
                case "book":
                    sender.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    inlineKeyboard.sendBookInlineKeyBoard(update, 0);
                    return;
                case "report":
                    user.setStatus(UserStatus.WAIT_REPORT);
                    sender.send(resource.getStringByKey("STR_62"), chatId);
                    return;
                case "statistic":
                    botUtil.sendStatistic(chatId);
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
                sender.send(Data.getInstance().getWordManager().getRuleDescriptionById(Integer.parseInt(callback.replace("book", ""))).getDescription(), chatId);
                var builder = InlineKeyboardBuilder.
                        create(chatId).setText(resource.getStringByKey("STR_18")).
                        row().
                        button("↑", "book").
                        endRow();
                sender.send(builder.build());
            }
            if (user.getStatus() == UserStatus.NONE) {
                for (var rule : Data.getInstance().getWordManager().getRules()) {
                    if (rule.getSection().equals(callback)) {
                        user.setStatus(UserStatus.WAIT_COUNT_OF_WORD);
                        user.setCurrRule(rule);
                        sender.send(resource.getStringByKey("STR_6") + rule.getName(), chatId);
                        sender.send(resource.getStringByKey("STR_7"), chatId);
                        return;
                    }
                }
            } else {
                sender.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
                var builder = InlineKeyboardBuilder.
                        create(chatId).
                        setText(resource.getStringByKey("STR_9")).
                        row().
                        button(resource.getStringByKey("YES"), "reset_testing").
                        button(resource.getStringByKey("NO"), "noreset_testing").
                        endRow();
                sender.send(builder.build());
            }
        }

    /**
     * parse audio message using only for reporting to admin
     * @param update
     */
    @Override
    public void parseAudio(Update update) {
        if (users.get(update.getMessage().getChatId()).getStatus() == UserStatus.WAIT_REPORT) {
            for (long chatId : Config.getAdminsId()) {
                SendVoice sendVoice = new SendVoice();
                sendVoice.setChatId(chatId);
                sendVoice.setVoice(update.getMessage().getVoice().getFileId());
                sender.send(sendVoice);
            }
        } else {
            sender.send(resource.getStringByKey("STR_102"), update.getMessage().getChatId());
        }
    }

    /**
     * parse image message using only for reporting to admin
     * @param update
     */
    @Override
    public void parseImage(Update update) {
        if (users.get(update.getMessage().getChatId()).getStatus() == UserStatus.WAIT_REPORT) {
            for (long chatId : Config.getAdminsId()) {
                var photo = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1);
                var sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(photo.getFileId());
                sender.send(sendPhoto);
            }
        } else {
            sender.send(resource.getStringByKey("STR_103"), update.getMessage().getChatId());
        }
    }
}