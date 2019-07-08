package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.Interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface InlineKeyboardInt {
    void sendBookInlineKeyBoard(Update update, int pageNumber);

    void sendRuleInlineKeyboard(Update update, int pageNumber);

    void sendLoginInfo(long chatId);

    void sendMenu(long chatId);
}
