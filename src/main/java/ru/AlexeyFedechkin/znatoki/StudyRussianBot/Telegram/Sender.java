package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Statistic;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis;

import java.io.File;

public class Sender {
    private final static Logger logger = Logger.getLogger(Sender.class);

    private final TelegramBot telegramBot;

    public Sender(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    /**
     * send message to telegram user
     * @param message string with message to user
     * @param chatId id of chat where to send message
     */
    public void send(String message, long chatId){
        logger.info("send message: " + message);
        var msg = new SendMessage().
                setChatId(chatId).
                setText(message);
        try{
            telegramBot.execute(msg);
            Redis.getInstance().sent(chatId);
            Statistic.getInstance().checkSent(chatId);
        } catch (TelegramApiException e) {
            logger.warn("fail to send message", e);
        }
    }

    /**
     * send SendMessage to telegram user
     * @param sendMessage object that contain data for send message to telegram
     */
    public void send(SendMessage sendMessage){
        logger.info("send message: " + sendMessage.getText());
        try{
            telegramBot.execute(sendMessage);
            Redis.getInstance().sent(Long.parseLong(sendMessage.getChatId()));
            Statistic.getInstance().checkSent(Long.parseLong(sendMessage.getChatId()));
        } catch (TelegramApiException e) {
            logger.warn("fail to send message", e);
        }
    }

    /**
     * @param sendVoice
     */
    public void send(SendVoice sendVoice) {
        logger.info("send voice");
        try {
            telegramBot.execute(sendVoice);
        } catch (TelegramApiException e) {
            logger.warn("fail to send voice", e);
            Redis.getInstance().sent(Long.parseLong(sendVoice.getChatId()));
            Statistic.getInstance().checkSent(Long.parseLong(sendVoice.getChatId()));
        }
    }


    /**
     * send file as image.
     * after sending file will be delete.
     *
     * @param file   file with image
     * @param chatId id of user
     */
    public void send(File file, long chatId) {
        var sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(file);
        try {
            telegramBot.execute(sendPhoto);
            logger.info("send photo: " + file.getName());
            Redis.getInstance().sent(chatId);
            Statistic.getInstance().checkSent(chatId);
            if (file.delete()) {
                logger.info("file \"" + file.getName() + "\" deleted successfully");
            } else {
                logger.warn("file \"" + file.getName() + "\" deleted fail");
            }
        } catch (TelegramApiException e) {
            logger.info("send photo fail", e);
        }
    }

    /**
     * @param sendPhoto
     */
    public void send(SendPhoto sendPhoto) {
        try {
            telegramBot.execute(sendPhoto);
            logger.info("send photos");
            Redis.getInstance().sent(Long.parseLong(sendPhoto.getChatId()));
            Statistic.getInstance().checkSent(Long.parseLong(sendPhoto.getChatId()));
        } catch (TelegramApiException e) {
            logger.info("send photo fail", e);
        }
    }

    /**
     * delete message. using for delete previously message with inline keyboard
     * @param chatId id of chat with user
     * @param messageId id of deleting message
     */
    public void delete(Long chatId, Integer messageId){
        logger.info("delete message " + chatId + " - " + messageId + "");
        var deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try{
            telegramBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            logger.info("delete fail " + chatId + " - " + messageId);
        }
    }
}
