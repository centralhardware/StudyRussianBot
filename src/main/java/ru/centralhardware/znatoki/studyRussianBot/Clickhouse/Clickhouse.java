package ru.centralhardware.znatoki.studyRussianBot.Clickhouse;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

public class Clickhouse {

    public void insert(Update update){
        User from = getFrom(update);
        StatisticMapper.insertStatistic(
                LocalDateTime.now(),
                from.getId(),
                from.getUserName(),
                from.getFirstName(),
                from.getLastName(),
                from.getLanguageCode(),
                from.getIsPremium(),
                getText(update)
        );
    }

    private String getText(Update update){
        if (update.hasMessage()){
            return update.getMessage().getText();
        } else if (update.hasCallbackQuery()){
            return update.getCallbackQuery().getData();
        } else if (update.hasInlineQuery()){
            return update.getInlineQuery().getQuery();
        }

        return "";
    }

    private User getFrom(Update update){
        if (update.hasMessage()){
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()){
            return update.getCallbackQuery().getFrom();
        } else if (update.hasInlineQuery()){
            return update.getInlineQuery().getFrom();
        }

        return null;
    }


}
