package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Rule;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.User;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Word;

/**
 * wrapper for work with redis server
 */
@SuppressWarnings("HardCodedStringLiteral")
public class JedisData {
    private static final JedisData ourInstance = new JedisData();
    private final Logger logger = Logger.getLogger(Jedis.class);
    private final RSA rsa = new RSA();

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
    private final String KEY_POSTFIX = "_key";

    /**
     * store data in redis about count of received messages
     * @param chatId id of user
     */
    public void received(long chatId){
        var count_of_received_message_key = chatId + COUNT_OF_RECEIVED_MESSAGE_POSTFIX;
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
        var count_of_sent_message_key = chatId + COUNT_OF_SENT_MESSAGE_POSTFIX;
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

    /**
     * store data about passing rule task
     * @param user user that pass rule
     */
    public void checkRule(User user){
        var checkWordKey = user.getChatId() + CHECKED_WORD_POSTFIX;
        var checkRuleKey = user.getChatId() + CHECKED_RULE_POSTFIX;
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

    /**
     * check on passing rule
     * @param chatId id of user
     * @param rule name of rule
     * @return
     */
    public boolean isCheckRule(long chatId, String rule){
        var checkRuleKey = chatId + CHECKED_RULE_POSTFIX;
        return jedis.sismember(checkRuleKey, rule);
    }

    /**
     * get count of send message for user
     * @param chatId id of user
     * @return count of sen message
     */
    public String getCountOfSentMessage(long chatId){
        var count_of_received_message_key = chatId + COUNT_OF_RECEIVED_MESSAGE_POSTFIX;
        return jedis.get(count_of_received_message_key);
    }

    /**
     * get count of received message for user
     * @param chatId id of user
     * @return count of received message
     */
    public String getCountOfReceivedMessage(long chatId){
        var count_of_sent_message_key = chatId + COUNT_OF_SENT_MESSAGE_POSTFIX;
        return jedis.get(count_of_sent_message_key);
    }

    /**
     * get count of checked word
     * @param chatId id of user
     * @return count of checked word
     */
    public int getCountOfCheckedWord(long chatId){
        var checkWordKey = chatId + CHECKED_WORD_POSTFIX;
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

    /**
     * note that the word was answered correctly for the given user
     * @param user user that check word
     */
    public void checkWord(User user){
        var checkWordKey = user.getChatId() + CHECKED_WORD_POSTFIX;
        jedis.sadd(checkWordKey, user.getWords().get(0).getName());
        logger.info("add value " + user.getWords().get(0).getName() + " to set by key " + checkWordKey);
    }

    /**
     * check license status
     * @param user_id
     * @return
     */
    public boolean checkRight(long user_id){
        var checkRightKey = user_id + KEY_POSTFIX;
        var key = jedis.get(checkRightKey);
        if (key != null){
            logger.info("right for user = " + user_id + " valid");
            return true;
        } else {
            logger.info("right for user = " + user_id + " don't valid");
            return false;
        }
    }

    /**
     * set activated code
     * @param user_id id of user
     * @param key activated code
     */
    public void setRight(long user_id, String key){
        var checkRightKey = user_id + KEY_POSTFIX;
        jedis.set(checkRightKey, key);
        logger.info("set right for key = " + key + " and user = " + user_id);
    }

    /**
     * delete value by key
     * @param key key of value to delete
     */
    public void deleteKey(String key){
        jedis.del(key);
    }

    /**
     * get value by giving key
     * @param key key for getting
     * @return String with value by giving key
     */
    public String getvalue(String key) {
        return jedis.get(key);
    }
}