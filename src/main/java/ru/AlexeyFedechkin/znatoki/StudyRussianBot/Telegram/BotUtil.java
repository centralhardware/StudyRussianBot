package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram;

import org.apache.log4j.Logger;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Statistic;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Chart;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.ChartUtil;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

public class BotUtil {
    private final static Logger logger = Logger.getLogger(BotUtil.class);
    private final Resource resource = new Resource();
    private final Chart chart = new Chart();
    private final Sender sender;
    public BotUtil(Sender sender){
        this.sender = sender;
    }

    /**
     * send to admin bot statistic
     * with two simple graf
     * @param chatId id of admin user
     */
    public void sendStatistic(Long chatId) {
        if (Config.getAdminsId().contains(chatId)) {
            var userStatistic = Statistic.getInstance().getStatistic();
            String statString = resource.getStringByKey("STR_48") + "\n" +
                    resource.getStringByKey("STR_49") + userStatistic.getTotalCountOfSend() + "\n" +
                    resource.getStringByKey("STR_50") + userStatistic.getTotalCountReceived() + "\n" +
                    resource.getStringByKey("STR_51") + userStatistic.getUserReceived().size() + "\n";
            sender.send(statString, chatId);
            try {
                sender.send(chart.genOneLineGraf(resource.getStringByKey("STR_52"),
                        resource.getStringByKey("STR_53"),
                        ChartUtil.listToArray(userStatistic.getTotalReceived()),
                        ChartUtil.getXData(userStatistic.getTotalReceived().size())), chatId);
                sender.send(chart.genOneLineGraf(resource.getStringByKey("STR_55"),
                        resource.getStringByKey("STR_56"),
                        ChartUtil.listToArray(userStatistic.getTotalSend()),
                        ChartUtil.getXData(userStatistic.getTotalSend().size())), chatId);
            } catch (Exception e) {
                logger.warn("error while generate graf", e);
                sender.send(resource.getStringByKey("STR_57"), chatId);
            }
        } else {
            sender.send(resource.getStringByKey("STR_47"), chatId);
        }
    }
}
