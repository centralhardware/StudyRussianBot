package me.centralhardware.znatoki.studyRussianBot.telegram

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import kotlin.properties.Delegates

class InlineKeyboardDsl {

    private lateinit var text: String
    private var chatId by Delegates.notNull<Long>()
    private val keyboard: MutableList<InlineKeyboardRow> = mutableListOf()

    fun text(text: String) {
        this.text = text
    }

    fun chatId(chatId: Long) {
        this.chatId = chatId
    }

    fun row(initializer: Row.() -> Unit) {
        keyboard.add(Row().apply(initializer).row)
    }

    fun build(): SendMessage {
        return SendMessage.builder()
            .text(text)
            .chatId(chatId)
            .replyMarkup(InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard).build()
        ).build()
    }
}

fun inlineKeyboard(initializer: InlineKeyboardDsl.() -> Unit): InlineKeyboardDsl {
    return InlineKeyboardDsl().apply(initializer)
}

class Row {

    val row: InlineKeyboardRow = InlineKeyboardRow()

    fun btn(text: String, callbackData: String) {
        row.add(InlineKeyboardButton.builder().text(text).callbackData(callbackData).build())
    }

}