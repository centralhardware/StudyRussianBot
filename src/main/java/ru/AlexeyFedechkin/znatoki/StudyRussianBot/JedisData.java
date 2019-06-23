package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Word;

/**
 * wrapper for work with redis server
 */
@SuppressWarnings("HardCodedStringLiteral")
public class JedisData {
    private static final JedisData ourInstance = new JedisData();
    private final Logger logger = Logger.getLogger(Jedis.class);

    public static JedisData getInstance() {
        return ourInstance;
    }

    private Jedis jedis;

    private JedisData() {
        logger.info("redis configure");
        jedis =  new Jedis(Config.getInstance().getRedisHost(), Config.getInstance().getRedisPort());
    }

    private final String COUNT_OF_RECEIVED_MESSAGE_POSTFIX = "_count_of_received_message";
    private final String COUNT_OF_RECEIVED_MESSAGE_KEY = "count_of_received_message";
    private final String COUNT_OF_SENT_MESSAGE_POSTFIX = "_count_of_sent_message";
    private final String COUNT_OF_SENT_MESSAGE_KEY = "count_of_sent_message";
    private final String CHECKED_WORD_POSTFIX = "_checked_word";
    private final String CHECKED_RULE_POSTFIX = "_checked_rule";

    /**
     * store data in redis about count of received messages
     * @param chatId id of user
     */
    public void received(long chatId){
        String count_of_received_message_key = chatId + COUNT_OF_RECEIVED_MESSAGE_POSTFIX;
        if (jedis.get(count_of_received_message_key) == null){
            jedis.set(count_of_received_message_key, "1");
        } else {
            jedis.set(count_of_received_message_key, String.valueOf(Integer.parseInt(jedis.get(count_of_received_message_key)) + 1));
        }
        logger.info("set key " + count_of_received_message_key + " value " + jedis.get(count_of_received_message_key));

        if (jedis.get(COUNT_OF_RECEIVED_MESSAGE_KEY) == null){
            jedis.set(COUNT_OF_RECEIVED_MESSAGE_KEY, "1");
        } else {
            jedis.set(COUNT_OF_RECEIVED_MESSAGE_KEY, String.valueOf(Integer.parseInt(jedis.get(COUNT_OF_RECEIVED_MESSAGE_KEY)) + 1));
        }
        logger.info("set key " + COUNT_OF_RECEIVED_MESSAGE_KEY + " value " + jedis.get(COUNT_OF_RECEIVED_MESSAGE_KEY));
    }

    /**
     * store data in redis about count of sent messages
     * @param chatId id of user
     */
    public void sent(long chatId){
        String count_of_sent_message_key = chatId + COUNT_OF_SENT_MESSAGE_POSTFIX;
        if (jedis.get(count_of_sent_message_key) == null){
            jedis.set(count_of_sent_message_key, "1");
        } else {
            jedis.set(count_of_sent_message_key, String.valueOf(Integer.parseInt(jedis.get(count_of_sent_message_key)) + 1));
        }
        logger.info("set key " + count_of_sent_message_key + " value " + jedis.get(count_of_sent_message_key));

        if (jedis.get(COUNT_OF_SENT_MESSAGE_KEY) == null){
            jedis.set(COUNT_OF_SENT_MESSAGE_KEY, "1");
        } else {
            jedis.set(COUNT_OF_SENT_MESSAGE_KEY, String.valueOf(Integer.parseInt(jedis.get(COUNT_OF_SENT_MESSAGE_KEY)) + 1));
        }
        logger.info("set key " + COUNT_OF_SENT_MESSAGE_KEY + " value " + jedis.get(COUNT_OF_SENT_MESSAGE_KEY));
    }

    public void checkRule(User user){
        String checkWordKey = user.getChatId() + CHECKED_WORD_POSTFIX;
        String checkRuleKey = user.getChatId() + CHECKED_RULE_POSTFIX;
        int checkedCount = 0;
        for (Word word : Data.getInstance().wordManager.getRule(user.getCurrRule().getName()).getWords()){
            if (jedis.sismember(checkWordKey, word.getName())){
                checkedCount++;
            }
        }
        if (Config.getInstance().isTesting()){
            if (checkedCount > 2){
                jedis.sadd(checkRuleKey, user.getCurrRule().getName());
                logger.info("add value " + user.getCurrRule().getName() + " to set by key " + checkRuleKey);
            }
        } else {
            if (checkedCount >= user.getCurrRule().getWords().size()){
                jedis.sadd(checkRuleKey, user.getCurrRule().getName());
            }
        }
    }

    public boolean isCheckRule(long chatId, String rule){
        String checkRuleKey = chatId + CHECKED_RULE_POSTFIX;
        return jedis.sismember(checkRuleKey, rule);
    }

    public String getCountOfSentMessage(long chatId){
        String count_of_received_message_key = chatId + COUNT_OF_RECEIVED_MESSAGE_POSTFIX;
        return jedis.get(count_of_received_message_key);
    }

    public String getCountOfReceivedMessage(long chatId){
        String count_of_sent_message_key = chatId + COUNT_OF_SENT_MESSAGE_POSTFIX;
        return jedis.get(count_of_sent_message_key);
    }

    public int getCountOfCheckedWord(long chatId){
        String checkWordKey = chatId + CHECKED_WORD_POSTFIX;
        int count = 0;
        for (Rule rule : Data.getInstance().wordManager.getRules()){
            for (Word word : rule.getWords()){
                if (jedis.sismember(checkWordKey, word.getName())){
                    count++;
                }
            }
        }
        return count;
    }

    public void checkWord(User user){
        String checkWordKey = user.getChatId() + CHECKED_WORD_POSTFIX;
        jedis.sadd(checkWordKey, user.getWords().get(0).getName());
        logger.info("add value " + user.getWords().get(0).getName() + " to set by key " + checkWordKey);
    }
}