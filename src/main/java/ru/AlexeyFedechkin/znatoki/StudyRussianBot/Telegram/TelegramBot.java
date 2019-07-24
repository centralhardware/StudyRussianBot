/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 22:15
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Statistic;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Auth;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.RSA;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

import java.net.Authenticator;

/**
 * telegram bot class
 */
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = Logger.getLogger(TelegramBot.class);
    private TelegramParser telegramParser;
    private final RSA rsa = new RSA();
    private final Resource resource = new Resource();
    private final InlineKeyboard inlineKeyboard;
    private final Sender sender;

    /**
     * set proxy setting
     * @param botOptions proxy option
     */
    private TelegramBot(DefaultBotOptions botOptions) {
        super(botOptions);
        sender = new Sender(this);
        inlineKeyboard = new InlineKeyboard(sender);
    }

    /**
     * need to create instance from main class
     */
    public TelegramBot(){
        sender = new Sender(this);
        inlineKeyboard = new InlineKeyboard(sender);
    }

    /**
     * init telegram bot and configure proxy
     */
    public void init(){
        try {
            if (Config.isUseProxy()) {
                Authenticator.setDefault(new Auth());
                var botsApi = new TelegramBotsApi();
                var botOptions = ApiContext.getInstance(DefaultBotOptions.class);
                botOptions.setProxyHost(Config.getProxyHost());
                botOptions.setProxyPort(Config.getProxyPort());
                botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
                logger.info("proxy configure");
                botsApi.registerBot(new TelegramBot(botOptions));
            } else {
                var botsApi = new TelegramBotsApi();
                botsApi.registerBot(new TelegramBot());
            }
            logger.info("bot register");
        } catch (TelegramApiRequestException e) {
            logger.fatal("bot start fail", e);
            System.exit(20);
        }
    }

    /**
     * method by which the library telegram sends
     * the received messages for processing by the server part
     * logging input message.
     * @param update received message
     */
    public void onUpdateReceived(Update update) {
        if (telegramParser == null) {
            telegramParser = new TelegramParser(sender);
        }
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }
        Statistic.getInstance().checkReceived(chatId);
        if (update.hasCallbackQuery()){
            logger.info("receive callback \"" + update.getCallbackQuery().getData() + "\" " +
                    update.getCallbackQuery().getFrom().getFirstName() + "\" " +
                    update.getCallbackQuery().getFrom().getLastName() + "\" " +
                    update.getCallbackQuery().getFrom().getUserName() + "\"");
            if (!telegramParser.getUsers().containsKey(update.getCallbackQuery().getMessage().getChatId())) {
                telegramParser.getUsers().put(update.getCallbackQuery().getMessage().getChatId(), new User(update.getCallbackQuery().getMessage().getChatId()));
            }
        } else if (update.getMessage().hasText()) {
            logger.info("receive message \"" + update.getMessage().getText() +
                    "\" from \"" + update.getMessage().getFrom().getFirstName() + "\" " +
                    update.getMessage().getFrom().getLastName() + "\" " +
                    update.getMessage().getFrom().getUserName() + "\" " +
                    update.getMessage().getFrom().getId() + "\"");
            if (!telegramParser.getUsers().containsKey(update.getMessage().getChatId())) {
                telegramParser.getUsers().put(update.getMessage().getChatId(), new User(update.getMessage().getChatId()));
            }
            if (update.getMessage().getText().toLowerCase().equals("ping")) {
                sender.send("pong",update.getMessage().getChatId());
            } else if (update.getMessage().getText().toLowerCase().equals("pong")) {
                sender.send("ping", update.getMessage().getChatId());
            }
        } else if (update.getMessage().hasVoice() || update.getMessage().hasAudio()) {
            logger.info("receive voice \"" + update.getMessage().getText() +
                    "\" from \"" + update.getMessage().getFrom().getFirstName() + "\" " +
                    update.getMessage().getFrom().getLastName() + "\" " +
                    update.getMessage().getFrom().getUserName() + "\" " +
                    update.getMessage().getFrom().getId() + "\"");
            telegramParser.parseAudio(update);
        } else if (update.getMessage().hasPhoto()) {
            logger.info("receive photo \"" + update.getMessage().getText() +
                    "\" from \"" + update.getMessage().getFrom().getFirstName() + "\" " +
                    update.getMessage().getFrom().getLastName() + "\" " +
                    update.getMessage().getFrom().getUserName() + "\" " +
                    update.getMessage().getFrom().getId() + "\"");
            telegramParser.parseImage(update);
        }
        if (!(Redis.getInstance().checkRight(chatId) || Config.getAdminsId().contains(chatId))) {
            switch (telegramParser.getUsers().get(chatId).getStatus()) {
                case WAIT_KEY:
                    if (!update.hasCallbackQuery()) {
                        if (rsa.validateKey(update.getMessage().getFrom().getUserName(), update.getMessage().getText())) {
                            telegramParser.getUsers().get(chatId).setStatus(UserStatus.NONE);
                            Redis.getInstance().setRight(chatId, update.getMessage().getText());
                            sender.send(resource.getStringByKey("STR_19"), chatId);
                            inlineKeyboard.sendMenu(chatId);
                        } else {
                            if (update.hasCallbackQuery()) {
                                telegramParser.parsCallback(update);
                                return;
                            }
                            sender.send(resource.getStringByKey("STR_21"), chatId);
                        }
                    } else {
                        telegramParser.parsCallback(update);
                    }
                    return;
                case WAIT_COUNT_OF_WORD:
                case TESTING:
                    if (update.hasCallbackQuery()) {
                        telegramParser.parsCallback(update);
                    } else {
                        telegramParser.parseText(update);
                    }
                    return;
                case WAIT_REPORT:
                    if (update.hasCallbackQuery()) {
                        telegramParser.parsCallback(update);
                    } else {
                        for (var id : Config.getAdminsId()) {
                            org.telegram.telegrambots.meta.api.objects.User user = update.getMessage().getFrom();
                            if (update.getMessage().getFrom().getUserName() == null) {
                                sender.send(resource.getStringByKey("STR_59") + user.getFirstName() + " "
                                        + user.getLastName() + " \n" + update.getMessage().getText(), id);
                            } else {
                                sender.send(resource.getStringByKey("STR_60") + user.getUserName() + " " +
                                        user.getFirstName() + " " + user.getLastName() + " \n"
                                        + update.getMessage().getText(), id);
                            }
                        }
                        sender.send(resource.getStringByKey("STR_61"), chatId);
                        telegramParser.getUsers().get(chatId).setStatus(UserStatus.NONE);
                    }
                    return;

            }
        }
        if (update.hasCallbackQuery()) {
            telegramParser.parsCallback(update);
        } else {
            telegramParser.parseText(update);
        }
    }

    /**
     * get bot user name
     * @return product or testing bot username
     */
    public String getBotUsername() {
        if (Config.isTesting()){
            logger.info("getting testing bot user name");
            return Config.getBotUserTestingName();
        } else {
            logger.info("getting production bot user name");
            return Config.getBotUserName();
        }
    }

    /**
     * get bot token
     * @return production or testing bot token
     */
    public String getBotToken() {
        if (Config.isTesting()){
            logger.info("getting testing bot token");
            return Config.getBotTestingToken();
        } else {
            logger.info("getting production bot token");
            return Config.getBotToken();
        }
    }
}