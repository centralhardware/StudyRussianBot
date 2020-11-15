package ru.alexeyFedechkin.znatoki.studyRussianBot.telegram

import mu.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

/**
 *telegram message sender
 *@property telegramBot instance of telegram bot
 */
class Sender(private val telegramBot: TelegramBot) {
    private val logger = KotlinLogging.logger { }

    /**
     * send message to telegram user
     * @param message string with message to user
     * @param chatId id of chat where to send message
     */
    fun send(message: String, chatId: Long) {
        logger.info("send message: $message")
        val msg = SendMessage.
            builder().
            chatId(chatId.toString()).
            text(message).
            build()
        try {
            telegramBot.execute(msg)
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
            telegramBot.execute(sendMessage)
        } catch (e: TelegramApiException) {
            logger.warn("fail to send message", e)
        }
    }


    /**
     * delete message. using for delete previously message with inline keyboard
     * @param chatId id of chat with user
     * @param messageId id of deleting message
     */
    fun delete(chatId: Long?, messageId: Int?) {
        logger.info("delete message $chatId - $messageId")
        val deleteMessage = DeleteMessage.
                                builder().
                                chatId(chatId.toString()).
                                messageId(messageId!!).
                                build()
        try {
            telegramBot.execute(deleteMessage)
        } catch (e: TelegramApiException) {
            logger.info("delete fail $chatId - $messageId")
        }

    }
}