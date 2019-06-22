package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;

public class TelegramBot extends TelegramLongPollingBot {

    private final Logger logger = Logger.getLogger(TelegramBot.class);
    private Parser parser;

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
        if (parser == null){
            parser = new Parser(this);
        }
        if (update.hasCallbackQuery()){
            if (!parser.getUsers().containsKey(update.getCallbackQuery().getMessage().getChatId())){
                parser.getUsers().put(update.getCallbackQuery().getMessage().getChatId(), new User(update.getCallbackQuery().getMessage().getChatId()));
            }
        } else {
            if (!parser.getUsers().containsKey(update.getMessage().getChatId())){
                parser.getUsers().put(update.getMessage().getChatId(), new User(update.getMessage().getChatId()));
            }
        }
        if (update.hasCallbackQuery()){
            logger.info("receive callback " + update.getCallbackQuery().getData() + " " +
                    update.getCallbackQuery().getFrom().getFirstName() + " " +
                    update.getCallbackQuery().getFrom().getLastName() + " " +
                    update.getCallbackQuery().getFrom().getUserName());
            parser.parsCallback(update);
        } else {
            logger.info("receive message " + update.getMessage().getText() +
                    " from " + update.getMessage().getFrom().getFirstName() + " " +
                    update.getMessage().getFrom().getLastName() + " " +
                    update.getMessage().getFrom().getUserName());
            parser.parseText(update);
        }
    }

    /**
     * @param message string with message to user
     * @param chatId id of chat where to send message
     * @return result of sending
     */
    public boolean sendMessage(String message, long chatId){
        logger.info("send message " + message);
        SendMessage msg = new SendMessage().
                setChatId(chatId).
                setText(message);
        try{
            execute(msg);
            return true;
        } catch (TelegramApiException e) {
            logger.warn("fail to send message", e);
            return false;
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
        logger.info("getting bot user name");
        return Config.getInstance().getBotUserName();
    }

    /**
     * @return
     */
    public String getBotToken() {
        logger.info("getting bot token");
        return Config.getInstance().getBotToken();
    }
}