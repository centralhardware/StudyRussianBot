/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 22:15
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.JedisData;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.RSA;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Resource;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 * telegram bot class
 */
public class TelegramBot extends TelegramLongPollingBot {

    private final Logger logger = Logger.getLogger(TelegramBot.class);
    private TelegramParser telegramParser;
    private final RSA rsa = new RSA();
    private final Resource resource = new Resource();
    private final InlineKeyboard inlineKeyboard = new InlineKeyboard(this);

    /**
     * set proxy setting
     * @param botOptions proxy option
     */
    private TelegramBot(DefaultBotOptions botOptions) {
        super(botOptions);
    }

    /**
     * need to create class from main class
     */
    public TelegramBot(){
    }

    /**
     * init telegram bot and configure proxy
     */
    public void init(){
        if (Config.getInstance().isUseProxy()){
            Authenticator.setDefault(new Authenticator() {
                @Override
                public PasswordAuthentication requestPasswordAuthenticationInstance(String host, InetAddress addr,
                                                                                    int port, String protocol,
                                                                                    String prompt, String scheme, URL url,
                                                                                    RequestorType reqType) {
                    return new PasswordAuthentication(Config.getInstance().getProxyUser(),
                            Config.getInstance().getProxyPassword().toCharArray());
                }
            });
            var botsApi = new TelegramBotsApi();
            var botOptions = ApiContext.getInstance(DefaultBotOptions.class);
            botOptions.setProxyHost(Config.getInstance().getProxyHost());
            botOptions.setProxyPort(Config.getInstance().getProxyPort());
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            logger.info("proxy configure");
            try {
                botsApi.registerBot(new TelegramBot(botOptions));
                logger.info("bot register");
            } catch (TelegramApiRequestException e) {
                logger.fatal("bot start fail", e);
                System.exit(20);
            }
        } else {
            var botsApi = new TelegramBotsApi();
            try {
                botsApi.registerBot(new TelegramBot());
                logger.info("bot register");
            } catch (TelegramApiRequestException e) {
                logger.fatal("bot start fail", e);
                System.exit(20);
            }
        }
    }

    /**
     * @param update
     */
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()){
            logger.info("receive callback " + update.getCallbackQuery().getData() + " " +
                    update.getCallbackQuery().getFrom().getFirstName() + " " +
                    update.getCallbackQuery().getFrom().getLastName() + " " +
                    update.getCallbackQuery().getFrom().getUserName());
        } else {
            logger.info("receive message " + update.getMessage().getText() +
                    " from " + update.getMessage().getFrom().getFirstName() + " " +
                    update.getMessage().getFrom().getLastName() + " " +
                    update.getMessage().getFrom().getUserName() + " " +
                    update.getMessage().getFrom().getId().toString());
            if (update.getMessage().getText().equals("ping")){
                send("pong",update.getMessage().getChatId());
            } else if (update.getMessage().getText().equals("pong")){
                send("ping", update.getMessage().getChatId());
            }
        }
        if (telegramParser == null){
            telegramParser = new TelegramParser(this);
        }
        long chatId;
        if (update.hasCallbackQuery()){
            if (!telegramParser.getUsers().containsKey(update.getCallbackQuery().getMessage().getChatId())){
                telegramParser.getUsers().put(update.getCallbackQuery().getMessage().getChatId(), new User(update.getCallbackQuery().getMessage().getChatId()));
            }
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            if (!telegramParser.getUsers().containsKey(update.getMessage().getChatId())){
                telegramParser.getUsers().put(update.getMessage().getChatId(), new User(update.getMessage().getChatId()));
            }
            chatId = update.getMessage().getChatId();
        }
        if (!JedisData.getInstance().checkRight(chatId) &&  !Config.getInstance().getAdminsId().contains(chatId)){
            if (telegramParser.getUsers().get(chatId).getStatus() == UserStatus.WAIT_KEY){
                if (!update.hasCallbackQuery()){
                    if (rsa.validateKey(update.getMessage().getFrom().getUserName(), update.getMessage().getText())){
                        telegramParser.getUsers().get(chatId).setStatus(UserStatus.NONE);
                        JedisData.getInstance().setRight(chatId, update.getMessage().getText());
                        send(resource.getStringByKey("STR_19"), chatId);
                        inlineKeyboard.sendMenu(chatId);
                    } else {
                        if (update.hasCallbackQuery()){
                            telegramParser.parsCallback(update);
                            return;
                        }
                        send(resource.getStringByKey("STR_21"), chatId);
                    }
                }
            } else if (telegramParser.getUsers().get(chatId).getStatus() == UserStatus.WAIT_COUNT_OF_WORD ||
                    telegramParser.getUsers().get(chatId).getStatus() == UserStatus.TESTING){
                telegramParser.parseText(update);
            } else {
                if (update.hasCallbackQuery()){
                  telegramParser.parsCallback(update);
                } else {
                    inlineKeyboard.sendLoginInfo(chatId);
                }
            }
        } else {
            if (update.hasCallbackQuery()){
                telegramParser.parsCallback(update);
            } else {
                telegramParser.parseText(update);
            }
        }
    }

    /**
     * send message to telegram user
     * @param message string with message to user
     * @param chatId id of chat where to send message
     */
    public void send(String message, long chatId){
        logger.info("send message " + message);
        var msg = new SendMessage().
                setChatId(chatId).
                setText(message);
        try{
            execute(msg);
            JedisData.getInstance().sent(chatId);
        } catch (TelegramApiException e) {
            logger.warn("fail to send message", e);
        }
    }

    /**
     * send SendMessage to telegram user
     * @param sendMessage
     */
    public void send(SendMessage sendMessage){
        logger.info("send message " + sendMessage.getText());
        try{
            execute(sendMessage);
            JedisData.getInstance().sent(Long.parseLong(sendMessage.getChatId()));
        } catch (TelegramApiException e) {
            logger.warn("fail to send message", e);
        }
    }

    /**
     * @param chatId
     * @param messageId
     */
    public void delete(Long chatId, Integer messageId){
        logger.info("delete message " + chatId + " " + messageId);
        var deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try{
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            logger.info("delete fail " + chatId + " " + messageId);
        }
    }

    /**
     * @return product or testing bot username
     */
    public String getBotUsername() {
        if (Config.getInstance().isTesting()){
            logger.info("getting testing bot user name");
            return Config.getInstance().getBotUserTestingName();
        } else {
            logger.info("getting production bot user name");
            return Config.getInstance().getBotUserName();
        }
    }

    /**
     * @return production or testing bot token
     */
    public String getBotToken() {
        if (Config.getInstance().isTesting()){
            logger.info("getting testing bot token");
            return Config.getInstance().getBotTestingToken();
        } else {
            logger.info("getting production bot token");
            return Config.getInstance().getBotToken();
        }
    }
}