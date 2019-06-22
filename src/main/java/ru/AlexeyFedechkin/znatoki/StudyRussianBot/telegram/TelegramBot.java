package ru.AlexeyFedechkin.znatoki.StudyRussianBot.telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;

public class TelegramBot extends TelegramLongPollingBot {

    private final Logger logger = Logger.getLogger(TelegramBot.class);
    private TelegramParser telegramParser;

    private TelegramBot(DefaultBotOptions botOptions) {
        super(botOptions);
    }

    public TelegramBot(){
    }

    /**
     * init telegram bot and configure proxy
     */
    public void init(){
//        Authenticator.setDefault(new Authenticator() {
//            @Override
//            public PasswordAuthentication requestPasswordAuthenticationInstance(String host, InetAddress addr,
//                                                                                int port, String protocol,
//                                                                                String prompt, String scheme, URL url,
//                                                                                RequestorType reqType) {
//                return new PasswordAuthentication(Config.getInstance().getProxyUser(),
//                        Config.getInstance().getProxyPassword().toCharArray());
//            }
//        });
        TelegramBotsApi botsApi = new TelegramBotsApi();
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
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
    }

    /**
     * @param update
     */
    public void onUpdateReceived(Update update) {
        if (telegramParser == null){
            telegramParser = new TelegramParser(this);
        }
        if (update.hasCallbackQuery()){
            if (!telegramParser.getUsers().containsKey(update.getCallbackQuery().getMessage().getChatId())){
                telegramParser.getUsers().put(update.getCallbackQuery().getMessage().getChatId(), new User(update.getCallbackQuery().getMessage().getChatId()));
            }
        } else {
            if (!telegramParser.getUsers().containsKey(update.getMessage().getChatId())){
                telegramParser.getUsers().put(update.getMessage().getChatId(), new User(update.getMessage().getChatId()));
            }
        }
        if (update.hasCallbackQuery()){
            logger.info("receive callback " + update.getCallbackQuery().getData() + " " +
                    update.getCallbackQuery().getFrom().getFirstName() + " " +
                    update.getCallbackQuery().getFrom().getLastName() + " " +
                    update.getCallbackQuery().getFrom().getUserName());
            telegramParser.parsCallback(update);
        } else {
            logger.info("receive message " + update.getMessage().getText() +
                    " from " + update.getMessage().getFrom().getFirstName() + " " +
                    update.getMessage().getFrom().getLastName() + " " +
                    update.getMessage().getFrom().getUserName());
            telegramParser.parseText(update);
        }
    }

    /**
     * @param message string with message to user
     * @param chatId id of chat where to send message
     * @return result of sending
     */
    public void sendMessage(String message, long chatId){
        logger.info("send message " + message);
        SendMessage msg = new SendMessage().
                setChatId(chatId).
                setText(message);
        try{
            execute(msg);
        } catch (TelegramApiException e) {
            logger.warn("fail to send message", e);
        }
    }

    public void sendMessage(SendMessage sendMessage){
        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.warn("fail to send message", e);
        }
    }

    /**
     * @return
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
     * @return
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