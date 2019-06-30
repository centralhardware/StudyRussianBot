package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.UserStatistic;

import java.util.*;

public class Statistic {
    private static Statistic ourInstance = new Statistic();
    private final Logger logger = Logger.getLogger(Statistic.class);
    private final String TOTAL_SENT_KEY = "total_sent";
    private final String TOTAL_RECEIVED_KEY = "total_received";
    private int PERIOD;
    private volatile int totalSent = 0;
    private volatile int totalReceived = 0;
    private Map<Long, Integer> countReceivedForUser = Collections.synchronizedMap(new HashMap<>());
    private Map<Long, Integer> countSentForUser = Collections.synchronizedMap(new HashMap<>());

    private Statistic() {
    }

    public static Statistic getInstance() {
        return ourInstance;
    }

    /**
     * save statistic to redis
     * start timer schedule
     */
    public void init() {
        if (Config.getInstance().isTesting()) {
            PERIOD = 1;
        } else {
            PERIOD = 60;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("save statistic");
                JedisData.getInstance().addToList(TOTAL_SENT_KEY, String.valueOf(totalSent));
                JedisData.getInstance().addToList(TOTAL_RECEIVED_KEY, String.valueOf(totalReceived));
                for (long key : countSentForUser.keySet()) {
                    JedisData.getInstance().addToList(key + "_send", String.valueOf(countSentForUser.get(key)));
                }
                for (long key : countReceivedForUser.keySet()) {
                    JedisData.getInstance().addToList(key + "_received", String.valueOf(countReceivedForUser.get(key)));
                }
                clearVariable();
            }
        }, 0, 1000 * 60 * PERIOD);
    }

    /**
     * clear all variable
     */
    private void clearVariable() {
        totalReceived = 0;
        totalSent = 0;
        countReceivedForUser.clear();
        countSentForUser.clear();
    }

    /**
     * store metric about received message
     *
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
     *
     * @return object with all statistic
     */
    public UserStatistic getStatistic() {
        var res = new UserStatistic();
        List<String> totalSent = JedisData.getInstance().getList(TOTAL_SENT_KEY);
        List<String> totalReceived = JedisData.getInstance().getList(TOTAL_RECEIVED_KEY);
        Set<String> userSent = JedisData.getInstance().getAllKeys("*_send");
        Set<String> userReceived = JedisData.getInstance().getAllKeys("*_received");

        HashMap<Long, ArrayList<Integer>> userSentRes = new HashMap<>();
        HashMap<Long, ArrayList<Integer>> userReceivedRes = new HashMap<>();

        for (String str : totalSent) {
            res.getTotalSend().add(Integer.valueOf(str));
        }
        for (String str : totalReceived) {
            res.getTotalReceived().add(Integer.valueOf(str));
        }

        for (String str : userSent) {
            List<String> listString = JedisData.getInstance().getList(str);
            ArrayList<Integer> list = new ArrayList<>();
            for (String s : listString) {
                list.add(Integer.valueOf(s));
            }
            if (!str.equals("total_send")) {
                userSentRes.put(Long.parseLong(str.replace("_send", "")), list);
            }
        }
        for (String str : userReceived) {
            List<String> listString = JedisData.getInstance().getList(str);
            ArrayList<Integer> list = new ArrayList<>();
            for (String s : listString) {
                list.add(Integer.valueOf(s));
            }
            if (!str.equals("total_received")) {
                userReceivedRes.put(Long.parseLong(str.replace("_received", "")), list);
            }
        }

        res.setUserReceived(userReceivedRes);
        res.setUserSend(userSentRes);

        res.setTotalCountOfSend(Integer.parseInt(JedisData.getInstance().getValue(JedisData.getInstance().COUNT_OF_SENT_MESSAGE_KEY)));
        res.setTotalCountReceived(Integer.parseInt(JedisData.getInstance().getValue(JedisData.getInstance().COUNT_OF_RECEIVED_MESSAGE_KEY)));

        Set<String> usersSent = JedisData.getInstance().getAllKeys("*" + JedisData.getInstance().COUNT_OF_SENT_MESSAGE_POSTFIX);
        Set<String> usersReceived = JedisData.getInstance().getAllKeys("*" + JedisData.getInstance().COUNT_OF_RECEIVED_MESSAGE_POSTFIX);

        for (String str : usersSent) {
            res.getUserCountSent().put(Long.valueOf(str.replace(JedisData.getInstance().COUNT_OF_SENT_MESSAGE_POSTFIX, "")),
                    Integer.parseInt(JedisData.getInstance().getValue(str)));

        }

        for (String str : usersReceived) {
            res.getUserCountSent().put(Long.valueOf(str.replace(JedisData.getInstance().COUNT_OF_RECEIVED_MESSAGE_POSTFIX, "")),
                    Integer.parseInt(JedisData.getInstance().getValue(str)));
        }
        return res;
    }
}
