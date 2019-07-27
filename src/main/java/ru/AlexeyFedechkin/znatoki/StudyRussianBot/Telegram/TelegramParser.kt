package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Telegram

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.objects.Update
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Config
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums.UserStatus
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.User
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.RSA
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Redis
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.WordManager
import java.util.HashMap

class TelegramParser {
    private val sender: Sender
    private var inlineKeyboard: InlineKeyboard
    private var botUtil: BotUtil
    val users = HashMap<Long, User>()

    /**
     * set telegramBot and create InlineKeyboard
     * @param sender object for sending message
     */
    constructor(sender: Sender){
        this.sender = sender
        inlineKeyboard = InlineKeyboard(sender)
        botUtil = BotUtil(sender)
    }

    /**
     * parse text message
     * support command
     * - /start: start command. also reset current testing.
     * - /help: show help message.
     * - /rules: show choose rule inline menu
     * - /profile: show profile data
     * - /menu: show menu
     * - /gen: generate activated code. Param: userName. only for admin
     * - /ver: verify activated code. Param: key userName. only for admin
     * - /stat: show bot statistic. only for admin
     * if message have not command: next action depending on status
     * - WAIT_COUNT_OF_WORD: start testing with giving count of word
     * - TESTING: message is answer on word with missing later. if answer is wright - send next word or result
     * if answer is wrong send "неправильно" and send next word. current word moves to the end of the word queue
     * @param update received message
     */
    fun parseText(update: Update) {
        val message = update.message.text
        val chatId = update.message.chatId
        val user = users[chatId]
        Redis.received(chatId)
        when (message) {
            "/start" -> {
                user!!.reset()
                if (Redis.checkRight(chatId)) {
                    sender.send(Resource.getStringByKey("START_MESSAGE"),
                            update.message.chatId)
                    inlineKeyboard.sendMenu(chatId!!)
                } else {
                    sender.send(Resource.getStringByKey("START_MESSAGE"),
                            update.message.chatId)
                    inlineKeyboard.sendLoginInfo(chatId!!)
                }
            }
            "/help" -> sender.send(Resource.getStringByKey("HELP_MESSAGE"), chatId)
            "/rules" -> inlineKeyboard.sendRuleInlineKeyboard(update, 0)
            "/book" -> inlineKeyboard.sendBookInlineKeyBoard(update, 0)
            "/profile" -> sender.send(user!!.getProfile(), chatId)
            "/menu" -> {
                inlineKeyboard.sendMenu(chatId!!)
                if (message.startsWith("/gen ")) {
                    if (Config.admins.contains(chatId)) {
                        if (message.replace("/gen ", "").isEmpty()) {
                            sender.send(Resource.getStringByKey("STR_31"), chatId)
                            return
                        }
                        sender.send(RSA.generateKey(message.replace("/gen ", "")), chatId)
                    } else {
                        sender.send(Resource.getStringByKey("STR_47"), update.message.chatId)
                    }
                } else if (message.startsWith("/ver ")) {
                    val args = message.replace("/ver ", "").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val key = args[0]
                    val msg = args[1]
                    if (Config.admins.contains(chatId)) {
                        sender.send(RSA.validateKey(msg, key).toString(), chatId)
                    } else {
                        sender.send(Resource.getStringByKey("STR_47"), update.message.chatId)
                    }
                } else if (message.startsWith("/stat")) {
                    botUtil.sendStatistic(chatId)
                } else if (message.startsWith("/")) {
                    sender.send(Resource.getStringByKey("STR_101"), chatId)
                }
                when (user!!.status) {
                    UserStatus.WAIT_COUNT_OF_WORD -> {
                        val count: Int
                        try {
                            count = Integer.parseInt(message)
                            if (count <= 0) {
                                sender.send(Resource.getStringByKey("STR_43"), chatId)
                                return
                            }
                            user.count = count
                            if (count > user.currRule!!.words.size) {
                                sender.send(Resource.getStringByKey("STR_1"), chatId)
                                user.status = UserStatus.NONE
                                user.words.clear()
                            } else {
                                user.status = UserStatus.TESTING
                                user.words.addAll(user.currRule!!.getWord(count))
                                sender.send(user.words[0].name, chatId)
                            }
                        } catch (e: NumberFormatException) {
                            sender.send(Resource.getStringByKey("STR_2"), chatId)
                        }

                    }
                    UserStatus.TESTING -> if (user.words[0].answer.toLowerCase() == message.toLowerCase()) {
                        sender.send(Resource.getStringByKey("STR_3"), chatId)
                        user.words.removeAt(0)
                        if (user.words.isEmpty()) {
                            sender.send(Resource.getStringByKey("STR_4"), chatId)
                            sender.send(user.getTestingResult(), chatId)
                            Redis.checkRule(user)
                            inlineKeyboard.sendMenu(chatId)
                            user.reset()
                            return
                        }
                        sender.send(user.words[0].name, chatId)
                        Redis.checkWord(user)
                    } else {
                        sender.send(Resource.getStringByKey("STR_5"), chatId)
                        Redis.checkWrongWord(user)
                        val temp = user.words[0]
                        user.words.removeAt(0)
                        user.words.add(temp)
                        user.wrongWords.add(temp)
                        sender.send(user.words[0].name, chatId)
                    }
                }
            }
            else -> {
                if (message.startsWith("/gen ")) {
                    if (Config.admins.contains(chatId)) {
                        if (message.replace("/gen ", "").isEmpty()) {
                            sender.send(Resource.getStringByKey("STR_31"), chatId)
                            return
                        }
                        sender.send(RSA.generateKey(message.replace("/gen ", "")), chatId)
                    } else {
                        sender.send(Resource.getStringByKey("STR_47"), update.message.chatId)
                    }
                } else if (message.startsWith("/ver ")) {
                    val args = message.replace("/ver ", "").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val key = args[0]
                    val msg = args[1]
                    if (Config.admins.contains(chatId)) {
                        sender.send(RSA.validateKey(msg, key).toString(), chatId!!)
                    } else {
                        sender.send(Resource.getStringByKey("STR_47"), update.message.chatId)
                    }
                } else if (message.startsWith("/stat")) {
                    botUtil.sendStatistic(chatId)
                } else if (message.startsWith("/")) {
                    sender.send(Resource.getStringByKey("STR_101"), chatId)
                }
                when (user!!.status) {
                    UserStatus.WAIT_COUNT_OF_WORD -> {
                        val count: Int
                        try {
                            count = Integer.parseInt(message)
                            if (count <= 0) {
                                sender.send(Resource.getStringByKey("STR_43"), chatId)
                                return
                            }
                            user.count = count
                            if (count > user.currRule!!.words.size) {
                                sender.send(Resource.getStringByKey("STR_1"), chatId)
                                user.status = UserStatus.NONE
                                user.words.clear()
                            } else {
                                user.status = UserStatus.TESTING
                                user.words.addAll(user.currRule!!.getWord(count))
                                sender.send(user.words[0].name, chatId)
                            }
                        } catch (e: NumberFormatException) {
                            sender.send(Resource.getStringByKey("STR_2"), chatId)
                        }

                    }
                    UserStatus.TESTING -> if (user.words[0].answer.toLowerCase() == message.toLowerCase()) {
                        sender.send(Resource.getStringByKey("STR_3"), chatId)
                        user.words.removeAt(0)
                        if (user.words.isEmpty()) {
                            sender.send(Resource.getStringByKey("STR_4"), chatId)
                            sender.send(user.getTestingResult(), chatId)
                            Redis.checkRule(user)
                            inlineKeyboard.sendMenu(chatId!!)
                            user.reset()
                            return
                        }
                        sender.send(user.words[0].name, chatId)
                        Redis.checkWord(user)
                    } else {
                        sender.send(Resource.getStringByKey("STR_5"), chatId)
                        Redis.checkWrongWord(user)
                        val temp = user.words[0]
                        user.words.removeAt(0)
                        user.words.add(temp)
                        user.wrongWords.add(temp)
                        sender.send(user.words[0].name, chatId)
                    }
                }
            }
        }
    }

    /**
     * parse callback
     * support action:
     * - reset_testing - change status to NONE
     * - noreset_testing - rejection cancellation. delete message and send current word again
     * - testing - start testing. for demo access aviable only tree rule.
     * - profile - show profile data
     * - help - show help message.
     * - menu - show menu
     * - enter_key - set status to wait_key and waiting input of activated code. only for demo access
     * - login - send login inline menu. only for demo access.
     * - buy_key - send message with data about buy access. only for demo access.
     * - book - show rule help. for demo aviable only tree rule description
     * - report - send message to admins. only for demo or full access
     * - statistic - show statistic. only for admin
     * - to_$pageNumber - show page of rule
     * - book_to_#pageNumber - show page of rule description
     * @param update received message
     */
    fun parsCallback(update: Update) {
        val callback = update.callbackQuery.data
        val chatId = update.callbackQuery.message.chatId
        val user = users[chatId]
        Redis.received(chatId)
        when (callback) {
            "reset_testing" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                user!!.reset()
                inlineKeyboard.sendMenu(chatId!!)
            }
            "noreset_testing" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                sender.send(user!!.words[0].name, chatId)
                return
            }
            "testing" -> inlineKeyboard.sendRuleInlineKeyboard(update, 0)
            "profile" -> sender.send(user!!.getProfile(), chatId)
            "help" -> if (Redis.checkRight(chatId) || Config.admins.contains(chatId)) {
                sender.send(Resource.getStringByKey("HELP_MESSAGE"), chatId)
            } else {
                sender.send(Resource.getStringByKey("STR_32"), chatId)
            }
            "menu" -> if (user!!.status !== UserStatus.TESTING && user!!.status !== UserStatus.WAIT_COUNT_OF_WORD) {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                inlineKeyboard.sendMenu(chatId!!)
            }
            "enter_key" -> {
                sender.send(Resource.getStringByKey("STR_22"), chatId)
                user!!.status = UserStatus.WAIT_KEY
                return
            }
            "login" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                inlineKeyboard.sendLoginInfo(chatId!!)
                return
            }
            "buy_key" ->
                // telegramBot.send(resource.getStringByKey("STR_33"), chatId);
                return
            "book" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                inlineKeyboard.sendBookInlineKeyBoard(update, 0)
                return
            }
            "report" -> {
                user!!.status = UserStatus.WAIT_REPORT
                sender.send(Resource.getStringByKey("STR_62"), chatId)
                return
            }
            "statistic" -> {
                botUtil.sendStatistic(chatId)
                return
            }
        }
        if (callback.startsWith("to_")) {
            if (user!!.status !== UserStatus.WAIT_COUNT_OF_WORD && user!!.status !== UserStatus.TESTING) {
                inlineKeyboard.sendRuleInlineKeyboard(update, callback.replace("to_", "").toInt())
            }
        }
        if (callback.startsWith("book_to_")) {
            inlineKeyboard.sendBookInlineKeyBoard(update, callback.replace("book_to_", "").toInt())
        }
        if (callback.startsWith("book") && !callback.startsWith("book_to_")) {
            sender.send(WordManager.getRuleDescriptionById(Integer.parseInt(callback.replace("book", "")))!!.description, chatId)
            val builder = InlineKeyboardBuilder.create(chatId).setText(Resource.getStringByKey("STR_18")).row().button("↑", "book").endRow()
            sender.send(builder.build())
        }
        if (user!!.status === UserStatus.NONE) {
            for (rule in WordManager.rules) {
                if (rule.section == callback) {
                    user!!.status = UserStatus.WAIT_COUNT_OF_WORD
                    user.currRule = rule
                    sender.send(Resource.getStringByKey("STR_6") + rule.name, chatId)
                    sender.send(Resource.getStringByKey("STR_7"), chatId)
                    return
                }
            }
        } else {
            sender.delete(chatId, update.callbackQuery.message.messageId)
            val builder = InlineKeyboardBuilder.create(chatId).setText(Resource.getStringByKey("STR_9")).row().button(Resource.getStringByKey("YES"), "reset_testing").button(Resource.getStringByKey("NO"), "noreset_testing").endRow()
            sender.send(builder.build())
        }
    }

    /**
     * parse audio message using only for reporting to admin
     * @param update
     */
    fun parseAudio(update: Update) {
        if (users[update.message.chatId]!!.status === UserStatus.WAIT_REPORT) {
            for (chatId in Config.admins) {
                val sendVoice = SendVoice()
                sendVoice.chatId = chatId.toString()
                sendVoice.setVoice(update.message.voice.fileId)
                sender.send(sendVoice)
            }
        } else {
            sender.send(Resource.getStringByKey("STR_102"), update.message.chatId)
        }
    }

    /**
     * parse image message using only for reporting to admin
     * @param update
     */
    fun parseImage(update: Update) {
        if (users[update.message.chatId]!!.status === UserStatus.WAIT_REPORT) {
            for (chatId in Config.admins) {
                val photo = update.message.photo[update.message.photo.size - 1]
                val sendPhoto = SendPhoto()
                sendPhoto.chatId = chatId.toString()
                sendPhoto.setPhoto(photo.fileId)
                sender.send(sendPhoto)
            }
        } else {
            sender.send(Resource.getStringByKey("STR_103"), update.message.chatId)
        }
    }
}