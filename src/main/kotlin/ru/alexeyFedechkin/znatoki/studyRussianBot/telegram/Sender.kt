package ru.alexeyFedechkin.znatoki.studyRussianBot.telegram

import mu.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import ru.alexeyFedechkin.znatoki.studyRussianBot.Statistic
import ru.alexeyFedechkin.znatoki.studyRussianBot.Utils.Redis
import java.io.File

class Sender(private val telegramBot: TelegramBot) {
    private val logger = KotlinLogging.logger {  }

    /**
     * send message to telegram user
     * @param message string with message to user
     * @param chatId id of chat where to send message
     */
    fun send(message: String, chatId: Long) {
        logger.info("send message: $message")
        val msg = SendMessage().setChatId(chatId).setText(message)
        try {
            telegramBot.execute<Message, SendMessage>(msg)
            Redis.sent(chatId)
            Statistic.checkSent(chatId)
        } catch (e: TelegramApiException) {
            logger.warn("fail to send message", e)
        }

    }

    /**
     * send SendMessage to telegram user
     * @param sendMessage object that contain data for send message to telegram
     */
    fun send(sendMessage: SendMessage) {
        logger.info("send message: " + sendMessage.text)
        try {
            telegramBot.execute<Message, SendMessage>(sendMessage)
            Redis.sent(java.lang.Long.parseLong(sendMessage.chatId))
            Statistic.checkSent(java.lang.Long.parseLong(sendMessage.chatId))
        } catch (e: TelegramApiException) {
            logger.warn("fail to send message", e)
        }

    }

    /**
     * @param sendVoice sending audio file
     */
    fun send(sendVoice: SendVoice) {
        logger.info("send voice")
        try {
            telegramBot.execute(sendVoice)
        } catch (e: TelegramApiException) {
            logger.warn("fail to send voice", e)
            Redis.sent(java.lang.Long.parseLong(sendVoice.chatId))
            Statistic.checkSent(java.lang.Long.parseLong(sendVoice.chatId))
        }

    }


    /**
     * send file as image.
     * after sending file will be delete.
     *
     * @param file   file with image
     * @param chatId id of user
     */
    fun send(file: File, chatId: Long) {
        val sendPhoto = SendPhoto()
        sendPhoto.setChatId(chatId)
        sendPhoto.setPhoto(file)
        try {
            telegramBot.execute(sendPhoto)
            logger.info("send photo: " + file.name)
            Redis.sent(chatId)
            Statistic.checkSent(chatId)
            if (file.delete()) {
                logger.info("file \"" + file.name + "\" deleted successfully")
            } else {
                logger.warn("file \"" + file.name + "\" deleted fail")
            }
        } catch (e: TelegramApiException) {
            logger.info("send photo fail", e)
        }

    }

    /**
     * @param sendPhoto sending photo object
     */
    fun send(sendPhoto: SendPhoto) {
        try {
            telegramBot.execute(sendPhoto)
            logger.info("send photo")
            Redis.sent(java.lang.Long.parseLong(sendPhoto.chatId))
            Statistic.checkSent(java.lang.Long.parseLong(sendPhoto.chatId))
        } catch (e: TelegramApiException) {
            logger.info("send photo fail", e)
        }

    }

    /**
     * delete message. using for delete previously message with inline keyboard
     * @param chatId id of chat with user
     * @param messageId id of deleting message
     */
    fun delete(chatId: Long?, messageId: Int?) {
        logger.info("delete message $chatId - $messageId")
        val deleteMessage = DeleteMessage()
        deleteMessage.setChatId(chatId!!)
        deleteMessage.messageId = messageId
        try {
            telegramBot.execute(deleteMessage)
        } catch (e: TelegramApiException) {
            logger.info("delete fail $chatId - $messageId")
        }

    }
}