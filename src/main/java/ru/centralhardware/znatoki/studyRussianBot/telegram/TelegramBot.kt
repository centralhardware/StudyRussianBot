package ru.centralhardware.znatoki.studyRussianBot.telegram

import mu.KotlinLogging
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.centralhardware.znatoki.studyRussianBot.Clickhouse
import ru.centralhardware.znatoki.studyRussianBot.Config
import ru.centralhardware.znatoki.studyRussianBot.objects.User
import ru.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus
import ru.centralhardware.znatoki.studyRussianBot.utils.RSA
import ru.centralhardware.znatoki.studyRussianBot.utils.Redis
import ru.centralhardware.znatoki.studyRussianBot.utils.Resource
import kotlin.system.exitProcess


/**
 *telegram bot class
 */
class TelegramBot(options: DefaultBotOptions) : TelegramLongPollingBot(options, Config.token) {
    private var telegramParser: TelegramParser? = null
    private var inlineKeyboard: InlineKeyboard
    private var sender: Sender = Sender(this)

    init {
        inlineKeyboard = InlineKeyboard(sender)
    }

    companion object{
        private val logger = KotlinLogging.logger { }

        /**
         * init telegram bot and configure proxy
         */
        fun init() {
            try {
                val options = DefaultBotOptions()
                options.baseUrl = Config.TELEGRAM_API_BOT_URL
                val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
                botsApi.registerBot(TelegramBot(options))
                logger.info("bot register")
            } catch (e: TelegramApiRequestException) {
                logger.warn("bot start fail", e)
                exitProcess(20)
            }
        }
    }


    private val clickhouse = Clickhouse();

    /**
     * method by which the library telegram sends
     * the received messages for processing by the server part
     * logging input message.
     * @param update received message
     */
    override fun onUpdateReceived(update: Update) {
        clickhouse.insert(update)

        if (telegramParser == null) telegramParser = TelegramParser(sender)

        val chatId: Long = if (update.hasCallbackQuery()) {
            update.callbackQuery.message.chatId!!
        } else {
            update.message.chatId!!
        }
        when {
            update.hasCallbackQuery() -> {
                logger.info("receive callback \"" + update.callbackQuery.data + "\" " +
                        update.callbackQuery.from.firstName + "\" " +
                        update.callbackQuery.from.lastName + "\" " +
                        update.callbackQuery.from.userName + "\"")
                if (!telegramParser!!.users.containsKey(update.callbackQuery.message.chatId)) {
                    telegramParser!!.users[update.callbackQuery.message.chatId] = User(update.callbackQuery.message.chatId)
                }
                telegramParser!!.parsCallback(update)
            }
            update.message.hasText() -> {
                logger.info("receive message \"" + update.message.text +
                        "\" from \"" + update.message.from.firstName + "\" " +
                        update.message.from.lastName + "\" " +
                        update.message.from.userName + "\" " +
                        update.message.from.id + "\"")
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
                if (!update.hasCallbackQuery()) {
                    if (RSA.validateKey(update.message.from.userName, update.message.text)) {
                        telegramParser!!.users[chatId]!!.status = UserStatus.NONE
                        Redis.setRight(chatId, update.message.text)
                        sender.send(Resource.getStringByKey("STR_19"), chatId)
                        inlineKeyboard.sendMenu(chatId)
                    } else {
                        sender.send(Resource.getStringByKey("STR_21"), chatId)
                    }
                }
            }
            else -> {}
        }
    }

    /**
     * get bot user name
     * @return bot username
     */
    override fun getBotUsername(): String {
        return Config.userName
    }

}