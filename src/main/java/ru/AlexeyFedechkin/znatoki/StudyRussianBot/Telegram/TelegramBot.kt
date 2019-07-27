package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram

import mu.KotlinLogging
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.UserStatus
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.User
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Statistic
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.RSA
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource
import kotlin.system.exitProcess

class TelegramBot : TelegramLongPollingBot{
    private val logger = KotlinLogging.logger {  }
    private var telegramParser: TelegramParser? = null
    private var inlineKeyboard: InlineKeyboard
    private var sender: Sender

    /**
     * set proxy setting
     * @param botOptions proxy option
     */
    constructor(botOptions: DefaultBotOptions): super(botOptions){
        sender = Sender(this)
        inlineKeyboard = InlineKeyboard(sender)
    }

    /**
     * need to create instance from main class
     */
    constructor(){
        sender = Sender(this)
        inlineKeyboard = InlineKeyboard(sender)
    }

    /**
     * init telegram bot and configure proxy
     */
    fun init() {
        try {
            if (Config.isUseProxy) {
                val botsApi = TelegramBotsApi()
                val botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)
                botOptions.proxyHost = Config.proxyHost
                botOptions.proxyPort = Config.proxyPort
                botOptions.proxyType = DefaultBotOptions.ProxyType.SOCKS5
                logger.info("proxy configure")
                botsApi.registerBot(TelegramBot(botOptions))
            } else {
                val botsApi = TelegramBotsApi()
                botsApi.registerBot(TelegramBot())
            }
            logger.info("bot register")
        } catch (e: TelegramApiRequestException) {
            logger.warn("bot start fail", e)
            exitProcess(20)
        }
    }

    /**
     * method by which the library telegram sends
     * the received messages for processing by the server part
     * logging input message.
     * @param update received message
     */
    override fun onUpdateReceived(update: Update) {
        if (telegramParser == null) {
            telegramParser = TelegramParser(sender)
        }
        val chatId: Long
        if (update.hasCallbackQuery()) {
            chatId = update.callbackQuery.message.chatId!!
        } else {
            chatId = update.message.chatId!!
        }
        Statistic.checkReceived(chatId)
        if (update.hasCallbackQuery()) {
            logger.info("receive callback \"" + update.callbackQuery.data + "\" " +
                    update.callbackQuery.from.firstName + "\" " +
                    update.callbackQuery.from.lastName + "\" " +
                    update.callbackQuery.from.userName + "\"")
            if (!telegramParser!!.users.containsKey(update.callbackQuery.message.chatId)) {
                telegramParser!!.users[update.callbackQuery.message.chatId] = User(update.callbackQuery.message.chatId)
            }
        } else if (update.message.hasText()) {
            logger.info("receive message \"" + update.message.text +
                    "\" from \"" + update.message.from.firstName + "\" " +
                    update.message.from.lastName + "\" " +
                    update.message.from.userName + "\" " +
                    update.message.from.id + "\"")
            if (!telegramParser!!.users.containsKey(update.message.chatId)) {
                telegramParser!!.users[update.message.chatId] = User(update.message.chatId)
            }
            if (update.message.text.toLowerCase() == "ping") {
                sender.send("pong", update.message.chatId!!)
            } else if (update.message.text.toLowerCase() == "pong") {
                sender.send("ping", update.message.chatId!!)
            }
        } else if (update.message.hasVoice() || update.message.hasAudio()) {
            logger.info("receive voice \"" + update.message.text +
                    "\" from \"" + update.message.from.firstName + "\" " +
                    update.message.from.lastName + "\" " +
                    update.message.from.userName + "\" " +
                    update.message.from.id + "\"")
            telegramParser!!.parseAudio(update)
        } else if (update.message.hasPhoto()) {
            logger.info("receive photo \"" + update.message.text +
                    "\" from \"" + update.message.from.firstName + "\" " +
                    update.message.from.lastName + "\" " +
                    update.message.from.userName + "\" " +
                    update.message.from.id + "\"")
            telegramParser!!.parseImage(update)
        }
        if (!(Redis.checkRight(chatId) || Config.admins.contains(chatId))) {
            when (telegramParser!!.users[chatId]!!.status) {
                UserStatus.WAIT_KEY -> {
                    if (!update.hasCallbackQuery()) {
                        if (RSA.validateKey(update.message.from.userName, update.message.text)) {
                            telegramParser!!.users[chatId]!!.status = UserStatus.NONE
                            Redis.setRight(chatId, update.message.text)
                            sender.send(Resource.getStringByKey("STR_19"), chatId)
                            inlineKeyboard.sendMenu(chatId)
                        } else {
                            if (update.hasCallbackQuery()) {
                                telegramParser!!.parsCallback(update)
                                return
                            }
                            sender.send(Resource.getStringByKey("STR_21"), chatId)
                        }
                    } else {
                        telegramParser!!.parsCallback(update)
                    }
                    return
                }
                UserStatus.WAIT_COUNT_OF_WORD, UserStatus.TESTING -> {
                    if (update.hasCallbackQuery()) {
                        telegramParser!!.parsCallback(update)
                    } else {
                        telegramParser!!.parseText(update)
                    }
                    return
                }
                UserStatus.WAIT_REPORT -> {
                    if (update.hasCallbackQuery()) {
                        telegramParser!!.parsCallback(update)
                    } else {
                        for (id in Config.admins) {
                            val user = update.message.from
                            if (update.message.from.userName == null) {
                                sender.send(Resource.getStringByKey("STR_59") + user.firstName + " "
                                        + user.lastName + " \n" + update.message.text, id)
                            } else {
                                sender.send(Resource.getStringByKey("STR_60") + user.userName + " " +
                                        user.firstName + " " + user.lastName + " \n"
                                        + update.message.text, id)
                            }
                        }
                        sender.send(Resource.getStringByKey("STR_61"), chatId)
                        telegramParser!!.users[chatId]!!.status = UserStatus.NONE
                    }
                    return
                }
            }
        }
        if (update.hasCallbackQuery()) {
            telegramParser!!.parsCallback(update)
        } else {
            telegramParser!!.parseText(update)
        }
    }

    /**
     * get bot user name
     * @return product or testing bot username
     */
    override fun getBotUsername(): String {
        return if (Config.isTesting) {
            logger.info("getting testing bot user name")
            Config.testUserName
        } else {
            logger.info("getting production bot user name")
            Config.userName
        }
    }

    /**
     * get bot token
     * @return production or testing bot token
     */
    override fun getBotToken(): String {
        return if (Config.isTesting) {
            logger.info("getting testing bot token")
            Config.testToken
        } else {
            logger.info("getting production bot token")
            Config.token
        }
    }
}