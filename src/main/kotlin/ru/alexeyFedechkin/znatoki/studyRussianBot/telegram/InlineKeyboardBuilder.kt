package ru.alexeyFedechkin.znatoki.studyRussianBot.telegram

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.*

/**
 *builder of inlineKeyboardMarkup
 */
class InlineKeyboardBuilder
/**
 * need to ban creation of instance of class without create method
 */ private constructor() {
    private var chatId: Long? = null
    private var text: String? = null

    private val keyboard = ArrayList<List<InlineKeyboardButton>>()
    private var row: MutableList<InlineKeyboardButton>? = null

    companion object {
        /**
         * create builder without args
         *
         * @return instance of InlineKeyboardBuilder
         */
        fun create(): InlineKeyboardBuilder {
            return InlineKeyboardBuilder()
        }

        /**
         * create builder with given chat id
         * @param chatId id of user
         * @return instance of InlineKeyboardBuilder with given chat id
         */
        fun create(chatId: Long): InlineKeyboardBuilder {
            val builder = InlineKeyboardBuilder()
            builder.setChatId(chatId)
            return builder
        }
    }


    /**
     * set text of inline keyboard
     * @param text string with sing of inline keyboard
     * @return instance of this class
     */
    fun setText(text: String): InlineKeyboardBuilder {
        this.text = text
        return this
    }

    /**
     * set chat id
     * @param chatId id of user
     * @return instance of this class
     */
    fun setChatId(chatId: Long): InlineKeyboardBuilder {
        this.chatId = chatId
        return this
    }

    /**
     * set start of row
     * @return instance of this class
     */
    fun row(): InlineKeyboardBuilder {
        this.row = ArrayList()
        return this
    }

    /**
     * @param text text of button
     * @param callbackData text of callback
     * @return instance of this class
     */
    fun button(text: String, callbackData: String): InlineKeyboardBuilder {
        row!!.add(InlineKeyboardButton().setText(text).setCallbackData(callbackData))
        return this
    }

    /**
     * set end of row
     * @return instance of this class
     */
    fun endRow(): InlineKeyboardBuilder {
        this.keyboard.add(this.row!!)
        this.row = null
        return this
    }


    /**
     * build Inline keyboard
     * @return SendMessage
     */
    fun build(): SendMessage {
        val message = SendMessage()
        message.setChatId(chatId!!)
        message.text = text
        val keyboardMarkup = InlineKeyboardMarkup()
        keyboardMarkup.keyboard = keyboard
        message.replyMarkup = keyboardMarkup
        return message
    }
}