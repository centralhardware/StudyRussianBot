package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.UserStatistic;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis;

import java.util.*;

/**
 * collect statistic to redis
 * period of collect is one hour for production mode
 * and one minute for testing mode
 * all modified variable are thread safe
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Statistic {
    private static final Statistic ourInstance = new Statistic();
    private final Logger logger = Logger.getLogger(Statistic.class);
    private final String TOTAL_SENT_KEY = "total_sent";
    private final String TOTAL_RECEIVED_KEY = "total_received";
    private final String USER_SEND_KEY = "_send";
    private final String USER_RECEIVED_KEY = "_received";
    private volatile int totalSent = 0;
    private volatile int totalReceived = 0;
    private final Map<Long, Integer> countReceivedForUser = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<Long, Integer> countSentForUser = Collections.synchronizedMap(new LinkedHashMap<>());

    private Statistic() {
    }

    public static Statistic getInstance() {
        return ourInstance;
    }

    /**
     * save statistic to redis
     * start timer schedule.
     */
    public void init() {
        int period;
        if (Config.getInstance().isTesting()) {
            period = 1;
        } else {
            period = 60;
        }
        Timer timer = new Timer();
        final int MILLISECONDS_IN_SECOND = 1000;
        final int SECOND_IN_MINUTE = 60;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("save statistic");
                Redis.getInstance().addToList(TOTAL_SENT_KEY, String.valueOf(totalSent));
                Redis.getInstance().addToList(TOTAL_RECEIVED_KEY, String.valueOf(totalReceived));
                for (long key : countSentForUser.keySet()) {
                    Redis.getInstance().addToList(key + USER_SEND_KEY, String.valueOf(countSentForUser.get(key)));
                }
                for (long key : countReceivedForUser.keySet()) {
                    Redis.getInstance().addToList(key + USER_RECEIVED_KEY, String.valueOf(countReceivedForUser.get(key)));
                }
                clearVariable();
            }
        }, 0, MILLISECONDS_IN_SECOND * SECOND_IN_MINUTE * period);
    }

    /**
     * clear all variable for start new period of statistics collection
     */
    private void clearVariable() {
        totalReceived = 0;
        totalSent = 0;
        countReceivedForUser.clear();
        countSentForUser.clear();
    }

    /**
     * store metric about received message
     * @param chatId id of user
     */
    public void checkReceived(long chatId) {
        totalReceived++;
        if (countReceivedForUser.containsKey(chatId)) {
            countReceivedForUser.put(chatId, countReceivedForUser.get(chatId) + 1);
        } else {
            countReceivedForUser.put(chatId, 1);
        }
    }

    /**
     * store metric about sent message
     * @param chatId id of user
     */
    public void checkSent(long chatId) {
        totalSent++;
        if (countSentForUser.containsKey(chatId)) {
            countSentForUser.put(chatId, countSentForUser.get(chatId) + 1);
        } else {
            countSentForUser.put(chatId, 1);
        }
    }

    /**
     * get statistic
     * @return object with all statistic
     */
    public UserStatistic getStatistic() {
        var res = new UserStatistic();
        //get row data from redis
        List<String> totalSent = Redis.getInstance().getListByKey(TOTAL_SENT_KEY);
        List<String> totalReceived = Redis.getInstance().getListByKey(TOTAL_RECEIVED_KEY);
        Set<String> userSent = Redis.getInstance().getAllKeys("*" + USER_SEND_KEY);
        Set<String> userReceived = Redis.getInstance().getAllKeys("*" + USER_RECEIVED_KEY);
        Set<String> usersSent = Redis.getInstance().getAllKeys("*" + Redis.getInstance().COUNT_OF_SENT_MESSAGE_POSTFIX);
        Set<String> usersReceived = Redis.getInstance().getAllKeys("*" + Redis.getInstance().COUNT_OF_RECEIVED_MESSAGE_POSTFIX);

        HashMap<Long, ArrayList<Integer>> userSentRes = new HashMap<>();
        HashMap<Long, ArrayList<Integer>> userReceivedRes = new HashMap<>();

        for (var str : userSent) {
            List<String> listString = Redis.getInstance().getListByKey(str);
            ArrayList<Integer> list = new ArrayList<>();
            for (var s : listString) {
                list.add(Integer.valueOf(s));
            }
            if (!str.equals("total_send")) {
                userSentRes.put(Long.parseLong(str.replace(USER_SEND_KEY, "")), list);
            }
        }
        for (String str : userReceived) {
            List<String> listString = Redis.getInstance().getListByKey(str);
            ArrayList<Integer> list = new ArrayList<>();
            for (var s : listString) {
                list.add(Integer.valueOf(s));
            }
            if (!str.equals("total_received")) {
                userReceivedRes.put(Long.parseLong(str.replace(USER_RECEIVED_KEY, "")), list);
            }
        }
        //set data to userStatistic object
        for (String str : totalSent) {
            res.getTotalSend().add(Integer.valueOf(str));
        }
        for (String str : totalReceived) {
            res.getTotalReceived().add(Integer.valueOf(str));
        }

        for (String str : usersSent) {
            res.getUserCountSent().put(Long.valueOf(str.replace(Redis.getInstance().COUNT_OF_SENT_MESSAGE_POSTFIX, "")),
                    Integer.parseInt(Redis.getInstance().getValue(str)));

        }

        for (String str : usersReceived) {
            res.getUserCountSent().put(Long.valueOf(str.replace(Redis.getInstance().COUNT_OF_RECEIVED_MESSAGE_POSTFIX, "")),
                    Integer.parseInt(Redis.getInstance().getValue(str)));
        }

        res.setUserReceived(userReceivedRes);
        res.setUserSend(userSentRes);

        res.setTotalCountOfSend(Integer.parseInt(Redis.getInstance().getValue(Redis.getInstance().COUNT_OF_SENT_MESSAGE_KEY)));
        res.setTotalCountReceived(Integer.parseInt(Redis.getInstance().getValue(Redis.getInstance().COUNT_OF_RECEIVED_MESSAGE_KEY)));
        return res;
    }
}
