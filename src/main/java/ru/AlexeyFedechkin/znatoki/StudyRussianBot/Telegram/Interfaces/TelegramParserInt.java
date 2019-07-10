package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.Interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramParserInt {
    void parseText(Update update);

    void parsCallback(Update update);

    void parseAudio(Update update);

    void parseImage(Update update);
}
