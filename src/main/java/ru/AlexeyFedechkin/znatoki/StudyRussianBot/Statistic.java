package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Statistic {
    private static Statistic ourInstance = new Statistic();
    private final Logger logger = Logger.getLogger(Statistic.class);
    private final String TOTAL_SENT_PREFIX = "_total_sent";
    private final String TOTAL_RECEIVED_PREFIX = "_total_received";
    private final String USER_SENT_PREFIX = "_user_sent";
    private final String USER_RECEIVED_PREFIX = "_user_received";
    private Date date;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd:hh.mm");
    private int PERIOD;
    private int totalSent;
    private int totalReceived;
    private HashMap<Long, Integer> countReceivedForUser = new HashMap<>();
    private HashMap<Long, Integer> countSentForUser = new HashMap<>();

    private Statistic() {
    }

    public static Statistic getInstance() {
        return ourInstance;
    }

    public void init() {
        if (Config.getInstance().isTesting()) {
            PERIOD = 1;
        } else {
            PERIOD = 10;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("save statistic");
                date = new Date();
                String dateString = dateFormat.format(date);
                JedisData.getInstance().setKey(dateString + TOTAL_SENT_PREFIX, String.valueOf(totalSent));
                JedisData.getInstance().setKey(dateString + TOTAL_RECEIVED_PREFIX, String.valueOf(totalReceived));
                for (Long key : countSentForUser.keySet()) {
                    JedisData.getInstance().setKey(key + USER_SENT_PREFIX, String.valueOf(countSentForUser.get(key)));
                }
                for (Long key : countReceivedForUser.keySet()) {
                    JedisData.getInstance().setKey(key + USER_RECEIVED_PREFIX, String.valueOf(countReceivedForUser.get(key)));
                }
                clearVariable();
            }
        }, 0, 1000 * 60 * PERIOD);
    }

    private void clearVariable() {
        totalReceived = 0;
        totalSent = 0;
        countReceivedForUser.clear();
        countSentForUser.clear();
    }

    public void checkReceived(long chatId) {
        totalReceived++;
        if (countReceivedForUser.containsKey(chatId)) {
            countReceivedForUser.put(chatId, countReceivedForUser.get(chatId) + 1);
        } else {
            countReceivedForUser.put(chatId, 1);
        }
    }

    public void checkSent(long chatId) {
        totalSent++;
        if (countSentForUser.containsKey(chatId)) {
            countSentForUser.put(chatId, countSentForUser.get(chatId) + 1);
        } else {
            countSentForUser.put(chatId, 1);
        }
    }

}
