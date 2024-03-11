package me.centralhardware.znatoki.studyRussianBot.telegram

import kotlinx.coroutines.runBlocking
import me.centralhardware.telegram.bot.common.ClickhouseRuben
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import me.centralhardware.znatoki.studyRussianBot.Config
import me.centralhardware.znatoki.studyRussianBot.objects.User
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus
import me.centralhardware.znatoki.studyRussianBot.utils.RSA
import me.centralhardware.znatoki.studyRussianBot.utils.Redis
import me.centralhardware.znatoki.studyRussianBot.utils.Resource
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import kotlin.system.exitProcess


/**
 *telegram bot class
 */
class TelegramBot : LongPollingSingleThreadUpdateConsumer {
    private var telegramParser: TelegramParser? = null
    private var inlineKeyboard: InlineKeyboard
    private var sender: Sender = Sender()

    init {
        inlineKeyboard = InlineKeyboard(sender)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TelegramBot::class.java)

        /**
         * init telegram bot and configure proxy
         */
        fun init() {
            try {
                val application = TelegramBotsLongPollingApplication()
                application.registerBot(Config.token, TelegramBot())
                logger.info("bot register")
            } catch (e: TelegramApiRequestException) {
                logger.warn("bot start fail", e)
                exitProcess(20)
            }
        }
    }

    private val clickhouse = ClickhouseRuben();

    /**
     * method by which the library telegram sends
     * the received messages for processing by the server part
     * logging input message.
     * @param update received message
     */
    override fun consume(update: Update){
        runBlocking {
            try {
                process(update)
            } catch (t: Throwable) {
                logger.warn("", t)
            }
        }
    }

    private suspend fun process(update: Update) {
        clickhouse.log(update, "StudyRussianBot")

        if (telegramParser == null) telegramParser = TelegramParser(sender)

        val chatId: Long = if (update.hasCallbackQuery()) {
            update.callbackQuery.message.chatId!!
        } else {
            update.message.chatId!!
        }
        when {
            update.hasCallbackQuery() -> {
                logger.info(
                    "receive callback \"" + update.callbackQuery.data + "\" " +
                            update.callbackQuery.from.firstName + "\" " +
                            update.callbackQuery.from.lastName + "\" " +
                            update.callbackQuery.from.userName + "\""
                )
                if (!telegramParser!!.users.containsKey(update.callbackQuery.message.chatId)) {
                    telegramParser!!.users[update.callbackQuery.message.chatId] =
                        User(update.callbackQuery.message.chatId)
                }
                telegramParser!!.parsCallback(update)
            }

            update.message.hasText() -> {
                logger.info(
                    "receive message \"" + update.message.text +
                            "\" from \"" + update.message.from.firstName + "\" " +
                            update.message.from.lastName + "\" " +
                            update.message.from.userName + "\" " +
                            update.message.from.id + "\""
                )
                when {
                    !telegramParser!!.users.containsKey(update.message.chatId) ->
                        telegramParser!!.users[update.message.chatId] = User(update.message.chatId)

                    update.message.text.lowercase() == "ping" ->
                        sender.send("pong", update.message.chatId!!)

                    update.message.text.lowercase() == "pong" ->
                        sender.send("ping", update.message.chatId!!)
                }
                telegramParser!!.parseText(update)
            }
        }
        when (telegramParser!!.users[chatId]!!.status) {
            UserStatus.WAIT_KEY -> {
                if (update.hasCallbackQuery()) return

                if (RSA.validateKey(update.message.from.userName, update.message.text)) {
                    telegramParser!!.users[chatId]!!.status = UserStatus.NONE
                    Redis.setRight(chatId, update.message.text)
                    sender.send(Resource.getStringByKey("STR_19"), chatId)
                    inlineKeyboard.sendMenu(chatId)
                } else {
                    sender.send(Resource.getStringByKey("STR_21"), chatId)
                }
            }

            else -> {}
        }
    }
}