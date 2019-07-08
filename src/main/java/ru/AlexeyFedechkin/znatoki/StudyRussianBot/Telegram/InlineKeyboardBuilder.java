package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.Interfaces.InlineKeyboardBuilderInt;

import java.util.ArrayList;
import java.util.List;

/**
 * builder for inlineKeyboardMarkup
 */
@SuppressWarnings({"CanBeFinal", "unused"})
public class InlineKeyboardBuilder implements InlineKeyboardBuilderInt {
    private Long chatId;
    private String text;

    private List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    private List<InlineKeyboardButton> row = null;

    /**
     * need to ban creation of instance of class without create method
     */
    private InlineKeyboardBuilder() {}

    /**
     * create builder without args
     *
     * @return instance of InlineKeyboardBuilder
     */
    public static InlineKeyboardBuilder create() {
        return new InlineKeyboardBuilder();
    }

    /**
     * create builder with given chat id
     * @param chatId id of user
     * @return instance of InlineKeyboardBuilder with given chat id
     */
    public static InlineKeyboardBuilder create(Long chatId) {
        var builder = new InlineKeyboardBuilder();
        builder.setChatId(chatId);
        return builder;
    }

    /**
     * set text of inline keyboard
     * @param text string with sing of inline keyboard
     * @return instance of this class
     */
    public InlineKeyboardBuilder setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * set chat id
     * @param chatId id of user
     * @return instance of this class
     */
    public InlineKeyboardBuilder setChatId(Long chatId) {
        this.chatId = chatId;
        return this;
    }

    /**
     * set start of row
     * @return instance of this class
     */
    public InlineKeyboardBuilder row() {
        this.row = new ArrayList<>();
        return this;
    }

    /**
     * @param text text of button
     * @param callbackData text of callback
     * @return instance of this class
     */
    public InlineKeyboardBuilder button(String text, String callbackData) {
        row.add(new InlineKeyboardButton().setText(text).setCallbackData(callbackData));
        return this;
    }

    /**
     * set end of row
     * @return instance of this class
     */
    public InlineKeyboardBuilder endRow() {
        this.keyboard.add(this.row);
        this.row = null;
        return this;
    }


    /**
     * build Inline keyboard
     * @return SendMessage
     */
    public SendMessage build() {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

//    public InlineKeyboardMarkup buildMarkup(){
//        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
//        keyboardMarkup.setKeyboard(keyboard);
//        return keyboardMarkup;
//    }
}
