package ru.AlexeyFedechkin.znatoki.StudyRussianBot.telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Data;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Word;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Resource;

import java.util.HashMap;

public class TelegramParser {
    private final Logger logger = Logger.getLogger(TelegramParser.class);
    private final TelegramBot telegramBot;
    private HashMap<Long, User> users = new HashMap<>();
    private Resource resource = new Resource();

    public HashMap<Long, User> getUsers() {
        return users;
    }

    /**
     * @param telegramBot
     */
    public TelegramParser(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * @param update
     */
    public void parseText(Update update) {
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        User user = users.get(chatId);
        switch (message) {
            case "/start":
                telegramBot.sendMessage(Config.getInstance().getStartupMessage(),
                        update.getMessage().getChatId());
                sendRuleInlineKeyboard(chatId);
                break;
            case "/info":
                telegramBot.sendMessage(Config.getInstance().getHelpMessage(), chatId);
                break;
            case "/rules":
                sendRuleInlineKeyboard(chatId);
            default:
                if (user.getStatus() == UserStatus.WAIT_COUNT_OF_WORD) {
                    int count;
                    try {
                        count = Integer.parseInt(message);
                        user.setCount(count);
                        if (count > user.getCurrRule().getWords().size()){
                            telegramBot.sendMessage(resource.getStringByKey("STR_1"), chatId);
                            user.setStatus(UserStatus.NONE);
                            user.getWords().clear();
                        } else {
                            user.setStatus(UserStatus.TESTING);
                            user.getWords().addAll(user.getCurrRule().getWord(count));
                            telegramBot.sendMessage(user.getWords().get(0).getName(), chatId);
                        }
                    } catch (NumberFormatException e) {
                        telegramBot.sendMessage(resource.getStringByKey("STR_2"), chatId);
                    }
                    break;
                } else if (user.getStatus() == UserStatus.TESTING){
                    if (user.getCurrRule().getWords().get(0).getAnswer().equals(message)){
                        telegramBot.sendMessage(resource.getStringByKey("STR_3"), chatId);
                        user.getWords().remove(0);
                        if (user.getWords().size() == 0){
                            telegramBot.sendMessage(resource.getStringByKey("STR_4"), chatId);
                            telegramBot.sendMessage(users.get(update.getMessage().getChatId()).getResult(), chatId);
                            user.reset();
                            return;
                        }
                        telegramBot.sendMessage(user.getWords().get(0).getName(), chatId);
                    } else{
                        telegramBot.sendMessage(resource.getStringByKey("STR_5"), chatId);
                        Word temp = user.getWords().get(0);
                        user.getWords().remove(0);
                        user.getWords().add(temp);
                    }
                }
        }
    }

    /**
     * @param update
     */
        public void parsCallback (Update update){
            String callback = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            User user = users.get(chatId);
            if (callback.equals("reset_testing")){
                user.reset();
                sendRuleInlineKeyboard(chatId);
            } else if (callback.equals("noreset_testing")){
                telegramBot.sendMessage(user.getWords().get(0).getName(), chatId);
                return;
            }
            if (user.getStatus() == UserStatus.NONE) {
                for (Rule rule : Data.getInstance().getWordManager().getRules()) {
                    if (rule.getSection().equals(callback)) {
                        user.setStatus(UserStatus.WAIT_COUNT_OF_WORD);
                        user.setCurrRule(rule);
                        telegramBot.sendMessage(resource.getStringByKey("STR_6") + rule.getName() , chatId);
                        telegramBot.sendMessage(resource.getStringByKey("STR_7"), chatId);
                        return;
                    }
                }
            } else {
                InlineKeyboardBuilder builder = InlineKeyboardBuilder.
                        create(chatId).
                        setText(resource.getStringByKey("STR_9")).
                        row().
                        button("да", "reset_testing").
                        button("нет", "noreset_testing").
                        endRow();
                telegramBot.sendMessage(builder.build());
            }
        }

    /**
     * @param chatId
     */
        private void sendRuleInlineKeyboard ( long chatId){
            logger.info("send inline keyboard rules");
            InlineKeyboardBuilder builder = InlineKeyboardBuilder.
                    create(chatId).setText(resource.getStringByKey("STR_8"));
            for (Rule rule : Data.getInstance().getWordManager().getRules()) {
                builder.row();
                builder.button(rule.getName(), rule.getSection());
                builder.endRow();
            }
            telegramBot.sendMessage(builder.build());
        }
}