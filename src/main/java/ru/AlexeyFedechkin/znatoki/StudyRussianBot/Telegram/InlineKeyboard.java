package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Data;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram.Interfaces.InlineKeyboardInt;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

/**
 * methods to send InlineKeyboardMarkup
 */
public class InlineKeyboard implements InlineKeyboardInt {
    private final Resource resource = new Resource();
    private static final Logger logger = Logger.getLogger(InlineKeyboard.class);
    private final TelegramBot telegramBot;

    /**
     * set telegramBot
     *
     * @param telegramBot instance of telegram bot
     */
    public InlineKeyboard(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    /**
     * send the user a message from the language selection help menu
     * for user who have demo access limited to three points
     * @param update object with received message
     * @param pageNumber number of page to select
     */
    public void sendBookInlineKeyBoard(Update update, int pageNumber){
        long chatId;
        String message = "";
        if (update.hasCallbackQuery()){
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
            message = update.getMessage().getText();
        }
        logger.info("send book keyboard rules");
        var builder = InlineKeyboardBuilder.
                create(chatId).setText(resource.getStringByKey("STR_42"));
        long userId;
        if (update.hasCallbackQuery()){
            userId = update.getCallbackQuery().getFrom().getId();
        } else {
            userId = update.getMessage().getFrom().getId();
        }
        if (!(Redis.getInstance().checkRight(userId) || Config.getInstance().getAdminsId().contains(userId))) {
            for (var i = 0; i < 3; i++) {
                var ruleDescription = Data.getInstance().getWordManager().getRuleDescriptions().get(i);
                builder.row().
                        button(ruleDescription.getName(), "book" + ruleDescription.getId()).
                        endRow();
            }
        } else {
            for (var ruleDescription : Data.getInstance().getWordManager().getRuleDescriptions()) {
                if (ruleDescription.getPageNumber() == pageNumber){
                    builder.
                            row().
                            button(ruleDescription.getName(), "book" + ruleDescription.getId()).
                            endRow();
                }
            }
        }
        // add buttons to go to other page
        if (pageNumber == 0){
            builder.row().
                    button(resource.getStringByKey("STR_17"), "book_to_1").
                    button(resource.getStringByKey("STR_24"), "menu").
                    endRow();
            if (!message.equals("/book")){
                telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
            }
        } else if (pageNumber < Rule.getMaxPage()){
            builder.row().
                    button(resource.getStringByKey("STR_18"), "book_to_" + (pageNumber - 1)).
                    button(resource.getStringByKey("STR_17") + (pageNumber + 1), "book_to_" + (pageNumber + 1)).
                    button(resource.getStringByKey("STR_24"), "menu").
                    endRow();
            telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
            telegramBot.send(builder.build());
            return;
        } else if (pageNumber == Rule.getMaxPage()){
            builder.row().
                    button(resource.getStringByKey("STR_18"), "book_to_" + (pageNumber - 1)).
                    button(resource.getStringByKey("STR_24"), "menu").
                    endRow();
            telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
            telegramBot.send(builder.build());
            return;
        }
        telegramBot.send(builder.build());
    }

    /**
     * send the user a message from the rule selection menu
     * for user who have demo access limited to three points
     * @param update object with received message
     * @param pageNumber number of page to select
     */
    public void sendRuleInlineKeyboard(Update update, int pageNumber){
        long chatId;
        String message = "";
        if (update.hasCallbackQuery()){
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
            message = update.getMessage().getText();
        }
        logger.info("send inline keyboard rules");
        var builder = InlineKeyboardBuilder.
                create(chatId).setText(resource.getStringByKey("STR_8"));
        long userId;
        if (update.hasCallbackQuery()){
            userId = update.getCallbackQuery().getFrom().getId();
        } else {
            userId = update.getMessage().getFrom().getId();
        }
        if (!(Redis.getInstance().checkRight(userId) || Config.getInstance().getAdminsId().contains(userId))) {
            for (var i = 1; i < 4; i++) {
                var rule = Data.getInstance().getWordManager().getRules().get(i);
                builder.row();
                if (Redis.getInstance().isCheckRule(chatId, rule.getName())) {
                    builder.button("✅" + rule.getName(), rule.getSection());
                } else {
                    builder.button(rule.getName(), rule.getSection());
                }
                builder.endRow();
            }
        } else {
            for (var rule : Data.getInstance().getWordManager().getRules()) {
                if (rule.getPageNumber() == pageNumber){
                    builder.row();
                    if (Redis.getInstance().isCheckRule(chatId, rule.getName())) {
                        builder.button("✅" + rule.getName(), rule.getSection());
                    } else {
                        builder.button(rule.getName(), rule.getSection());
                    }
                    builder.endRow();
                }
            }
        }
        // add buttons to got to other pages
        if (pageNumber == 0){
            builder.row().
                    button(resource.getStringByKey("STR_17"), "to_1").
                    button(resource.getStringByKey("STR_24"), "menu").
                    endRow();
            if (!message.equals("/rules")){
                telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
            }
        } else if (pageNumber < Rule.getMaxPage()){
            builder.row().
                    button(resource.getStringByKey("STR_18"), "to_" + (pageNumber - 1)).
                    button(resource.getStringByKey("STR_17") + (pageNumber + 1), "to_" + (pageNumber + 1)).
                    button(resource.getStringByKey("STR_24"), "menu").
                    endRow();
            telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
            telegramBot.send(builder.build());
            return;
        } else if (pageNumber == Rule.getMaxPage()){
            builder.row().
                    button(resource.getStringByKey("STR_18"), "to_" + (pageNumber - 1)).
                    button(resource.getStringByKey("STR_24"), "menu").
                    endRow();
            telegramBot.delete(chatId, update.getCallbackQuery().getMessage().getMessageId());
            telegramBot.send(builder.build());
            return;
        }
        telegramBot.send(builder.build());
    }

    /**
     * send login menu for user who have demo access
     * don't send for user who have full or admin access
     * @param chatId id of user
     */
    public void sendLoginInfo(long chatId) {
        if (Redis.getInstance().checkRight(chatId) || Config.getInstance().getAdminsId().contains(chatId)) {
            telegramBot.send(resource.getStringByKey("STR_44"), chatId);
        } else {
            var builder = InlineKeyboardBuilder.create(chatId).
                    setText(resource.getStringByKey("STR_28")).
                    row().
                    button(resource.getStringByKey("STR_29"), "enter_key").
                    endRow().
                    row().
                    button(resource.getStringByKey("STR_41"), "menu").
                    endRow().
                    row().
                    button(resource.getStringByKey("STR_40"), "buy_key").
                    endRow().
                    row().
                    button(resource.getStringByKey("STR_26"), "help").
                    endRow();
            telegramBot.send(builder.build());
        }
    }

    /**
     * send main menu
     * for user who have demo access will show button with text "get full access"
     * @param chatId id of user
     */
    public void sendMenu(long chatId){
        logger.info("send inline keyboard menu");
        var builder = InlineKeyboardBuilder.
                create(chatId).
                setText(resource.getStringByKey("STR_24")).
                row().
                button(resource.getStringByKey("STR_23"), "testing").
                button(resource.getStringByKey("STR_25"), "profile").
                button(resource.getStringByKey("STR_26"), "help").
                endRow().
                row().
                button(resource.getStringByKey("STR_34"), "book").
                endRow();
        if (!Redis.getInstance().checkRight(chatId) && !Config.getInstance().getAdminsId().contains(chatId)) {
            builder.row().
                    button(resource.getStringByKey("STR_36"), "login").
                    endRow();
        }
        if (!Config.getInstance().getAdminsId().contains(chatId)) {
            builder.
                    row().
                    button(resource.getStringByKey("STR_58"), "report").
                    endRow();
        } else {
            builder.
                    row().
                    button(resource.getStringByKey("STR_100"), "statistic").
                    endRow();
        }
        telegramBot.send(builder.build());
    }
}
