package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.Interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.InlineKeyboardBuilder;

public interface InlineKeyboardBuilderInt {
    InlineKeyboardBuilder setText(String text);

    InlineKeyboardBuilder setChatId(Long chatId);

    InlineKeyboardBuilder row();

    InlineKeyboardBuilder button(String text, String callbackData);

    SendMessage build();
}
