package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram

import mu.KotlinLogging
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Statistic
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Chart
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.ChartUtil
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource

class BotUtil(private val sender: Sender) {
    private val logger = KotlinLogging.logger {  }

    /**
     * send to admin bot statistic
     * with two simple graf
     * @param chatId id of admin user
     */
    fun sendStatistic(chatId: Long) {
        if (Config.admins.contains(chatId)) {
            val userStatistic = Statistic.getStatistic()
            val statString = Resource.getStringByKey("STR_48") + "\n" +
                    Resource.getStringByKey("STR_49") + userStatistic.totalCountOfSend + "\n" +
                    Resource.getStringByKey("STR_50") + userStatistic.totalCountReceived + "\n" +
                    Resource.getStringByKey("STR_51") + userStatistic.userReceived.size + "\n"
            sender.send(statString, chatId!!)
            try {
                sender.send(Chart.genOneLineGraf(Resource.getStringByKey("STR_52"),
                        Resource.getStringByKey("STR_53"),
                        ChartUtil.listToArray(userStatistic.totalReceived),
                        ChartUtil.getXData(userStatistic.totalReceived.size)), chatId)
                sender.send(Chart.genOneLineGraf(Resource.getStringByKey("STR_55"),
                        Resource.getStringByKey("STR_56"),
                        ChartUtil.listToArray(userStatistic.totalSend),
                        ChartUtil.getXData(userStatistic.totalSend.size)), chatId)
            } catch (e: Exception) {
                logger.warn("error while generate graf", e)
                sender.send(Resource.getStringByKey("STR_57"), chatId)
            }

        } else {
            sender.send(Resource.getStringByKey("STR_47"), chatId)
        }
    }
}