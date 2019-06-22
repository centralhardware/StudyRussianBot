package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums.UserStatus;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Word;

import java.io.IOException;
import java.util.HashMap;

public class Parser {
    private final Logger logger = Logger.getLogger(Parser.class);
    private TelegramBot telegramBot;
    private WordManager wordManager;
    private HashMap<Long, User> users = new HashMap<>();

    public HashMap<Long, User> getUsers() {
        return users;
    }

    /**
     * @param telegramBot
     */
    public Parser(TelegramBot telegramBot) {
        wordManager = new WordManager();
        try {
            wordManager.init();
        } catch (IOException e) {
            logger.fatal("load data fail", e);
        }
        this.telegramBot = telegramBot;
    }

    /**
     * @param update
     */
    public void parseText(Update update) {
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
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
                if (users.get(chatId).getStatus() == UserStatus.WAIT_COUNT_OF_WORD) {
                    int count = 0;
                    try {
                        count = Integer.parseInt(message);
                        users.get(chatId).setCount(count);
                        if (count > users.get(chatId).getCurrRule().getWords().size()){
                            telegramBot.sendMessage("к сожалению у нас нету столько слов", chatId);
                            users.get(chatId).setStatus(UserStatus.NONE);
                            users.get(chatId).getWords().clear();
                        } else {
                            users.get(chatId).setStatus(UserStatus.TESTING);
                            users.get(chatId).getWords().addAll(users.get(chatId).getCurrRule().getWord(count));
                            telegramBot.sendMessage(users.get(chatId).getWords().get(0).getName(), chatId);
                        }
                    } catch (NumberFormatException e) {
                        telegramBot.sendMessage("введите число", chatId);
                    }
                    break;
                } else if (users.get(chatId).getStatus() == UserStatus.TESTING){

                    if (users.get(chatId).getCurrRule().getWords().get(0).getAnswer().equals(message)){
                        telegramBot.sendMessage("правильно", chatId);
                        users.get(chatId).getWords().remove(0);
                        if (users.get(chatId).getWords().size() == 0){
                            telegramBot.sendMessage("вы завершили прохождение правила", chatId);
                            telegramBot.sendMessage(users.get(update.getMessage().getChatId()).getResult(), chatId);
                            users.get(chatId).reset();
                            return;
                        }
                        telegramBot.sendMessage(users.get(chatId).getWords().get(0).getName(), chatId);
                    } else{
                        telegramBot.sendMessage("не правильно", chatId);
                        Word temp = users.get(chatId).getWords().get(0);
                        users.get(chatId).getWords().remove(0);
                        users.get(chatId).getWords().add(temp);
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
            if (users.get(chatId).getStatus() == UserStatus.NONE) {
                for (Rule rule : wordManager.getRules()) {
                    if (rule.getSection().equals(callback)) {
                        users.get(chatId).setStatus(UserStatus.WAIT_COUNT_OF_WORD);
                        users.get(chatId).setCurrRule(rule);
                        telegramBot.sendMessage("вы выбрали правило " + rule.getName() , chatId);
                        telegramBot.sendMessage("введите количество слов для тестирования", chatId);
                        return;
                    }
                }
            } else {

            }
        }

    /**
     * @param chatId
     */
        private void sendRuleInlineKeyboard ( long chatId){
            logger.info("send inline keyboard rules");
            InlineKeyboardBuilder inlineKeyboardBuilder = InlineKeyboardBuilder.
                    create(chatId).setText("доступные правила");
            for (Rule rule : wordManager.getRules()) {
                inlineKeyboardBuilder.row();
                inlineKeyboardBuilder.button(rule.getName(), rule.getSection());
                inlineKeyboardBuilder.endRow();
            }
            telegramBot.sendMessage(inlineKeyboardBuilder.build());
        }
}