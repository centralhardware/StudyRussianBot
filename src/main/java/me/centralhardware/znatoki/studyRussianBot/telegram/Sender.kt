package me.centralhardware.znatoki.studyRussianBot.telegram

import me.centralhardware.znatoki.studyRussianBot.Config
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage

/**
 *telegram message sender
 *@property telegramBot instance of telegram bot
 */
class Sender {
    private val logger = LoggerFactory.getLogger(Sender::class.java)
    private val client = OkHttpTelegramClient(Config.token)

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

        runCatching { client.execute(msg) }
            .onFailure { logger.warn("", it) }
    }

    /**
     * send SendMessage to telegram user
     * @param sendMessage object that contain data for send message to telegram
     */
    fun send(sendMessage: SendMessage) {
        logger.info("send message: " + sendMessage.text)

        runCatching { client.execute(sendMessage) }
            .onFailure { logger.warn("", it) }
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

        runCatching { client.execute(deleteMessage) }
            .onFailure { logger.warn("", it) }
    }
}