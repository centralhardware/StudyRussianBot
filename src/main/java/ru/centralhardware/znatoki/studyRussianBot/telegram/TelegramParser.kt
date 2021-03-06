package ru.centralhardware.znatoki.studyRussianBot.telegram

import org.telegram.telegrambots.meta.api.objects.Update
import ru.centralhardware.znatoki.studyRussianBot.Config
import ru.centralhardware.znatoki.studyRussianBot.WordManager
import ru.centralhardware.znatoki.studyRussianBot.objects.User
import ru.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus.*
import ru.centralhardware.znatoki.studyRussianBot.utils.RSA
import ru.centralhardware.znatoki.studyRussianBot.utils.Redis
import ru.centralhardware.znatoki.studyRussianBot.utils.Resource
import java.util.*

/**
 *parse message
 * support type:
 * - text
 * - callback
 * @property sender instance of sender class
 * Copyright © 2019-2021 Fedechkin Alexey Borisovich. Contacts: alex@centralhardware.ru
 */
class TelegramParser
/**
 * set telegramBot and create InlineKeyboard
 * @param sender object for sending message
 */(private val sender: Sender) {
    private var inlineKeyboard: InlineKeyboard = InlineKeyboard(sender)

    val users: HashMap<Long, User> = HashMap<Long, User>()

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
        when (message) {
            "/start" -> {
                user!!.reset()
                sender.send(Resource.getStringByKey("START_MESSAGE"), update.message.chatId)
                if (Redis.checkRight(chatId)) {
                    inlineKeyboard.sendMenu(chatId!!)
                } else {
                    inlineKeyboard.sendLoginInfo(chatId!!)
                }
            }
            "/help" -> sender.send(Resource.getStringByKey("HELP_MESSAGE"), chatId)
            "/rules" -> inlineKeyboard.sendRuleInlineKeyboard(update, 0)
            "/book" -> inlineKeyboard.sendBookInlineKeyBoard(update, 0)
            "/profile" -> sender.send(user!!.getProfile(), chatId)
            "/menu" -> inlineKeyboard.sendMenu(chatId!!)
            "/ping" -> sender.send("pong", chatId)
            else -> {
                when {
                    message.startsWith("/gen ") -> {
                        if (Config.admins.contains(chatId)) {
                            if (message.replace("/gen ", "").isEmpty()) {
                                sender.send(Resource.getStringByKey("STR_31"), chatId)
                                return
                            }
                            sender.send(RSA.generateKey(message.replace("/gen ", "")), chatId)
                        } else {
                            sender.send(Resource.getStringByKey("STR_47"), update.message.chatId)
                        }
                    }
                    message.startsWith("/ver ") -> {
                        val args = message.replace("/ver ", "")
                            .split(" ".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val key = args[0]
                        val msg = args[1]
                        if (Config.admins.contains(chatId)) {
                            sender.send(RSA.validateKey(msg, key).toString(), chatId!!)
                        } else {
                            sender.send(Resource.getStringByKey("STR_47"), update.message.chatId)
                        }
                    }
                    message.startsWith("/") -> sender.send(Resource.getStringByKey("STR_101"), chatId)
                    else -> when (user!!.status) {
                        WAIT_COUNT_OF_WORD -> {
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
                                    user.status = NONE
                                    user.words.clear()
                                } else {
                                    user.status = TESTING
                                    user.words.addAll(user.currRule!!.getWord(count))
                                    sender.send(user.words[0].name, chatId)
                                }
                            } catch (e: NumberFormatException) {
                                sender.send(Resource.getStringByKey("STR_2"), chatId)
                            }

                        }
                        TESTING -> if (user.words[0].answer.equals(message, ignoreCase = true)) {
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
    }

    /**
     * parse callback
     * support action:
     * - reset_testing - change status to NONE
     * - noreset_testing - rejection cancellation. delete message and send current word again
     * - testing - start testing. for demo access available only tree rule.
     * - profile - show profile data
     * - help - show help message.
     * - menu - show menu
     * - enter_key - set status to wait_key and waiting input of activated code. only for demo access
     * - login - send login inline menu. only for demo access.
     * - book - show rule help. for demo aviable only tree rule description
     * - to_$pageNumber - show page of rule
     * - book_to_#pageNumber - show page of rule description
     * @param update received message
     */
    fun parsCallback(update: Update) {
        val callback = update.callbackQuery.data
        val chatId = update.callbackQuery.message.chatId
        val user = users[chatId]
        when (callback) {
            "reset_testing" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                user!!.reset()
                inlineKeyboard.sendMenu(chatId!!)
            }
            "noreset_testing" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                sender.send(user!!.words[0].name, chatId)
            }
            "testing" -> inlineKeyboard.sendRuleInlineKeyboard(update, 0)
            "profile" -> sender.send(user!!.getProfile(), chatId)
            "help" -> if (Redis.checkRight(chatId) || Config.admins.contains(chatId)) {
                sender.send(Resource.getStringByKey("HELP_MESSAGE"), chatId)
            } else {
                sender.send(Resource.getStringByKey("STR_32"), chatId)
            }
            "menu" -> if (user!!.status !== TESTING && user!!.status !== WAIT_COUNT_OF_WORD) {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                inlineKeyboard.sendMenu(chatId!!)
            }
            "enter_key" -> {
                if (update.callbackQuery.from.userName == null){
                    sender.send("вы должны иметь заполненое имя пользователя", chatId)
                } else {
                    sender.send(Resource.getStringByKey("STR_22"), chatId)
                    user!!.status = WAIT_KEY
                }
            }
            "login" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                inlineKeyboard.sendLoginInfo(chatId!!)
            }
            "book" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                inlineKeyboard.sendBookInlineKeyBoard(update, 0)
            }
            else -> {
                when {
                    callback.startsWith("to_") -> {
                        if (user!!.status !== WAIT_COUNT_OF_WORD && user!!.status !== TESTING) {
                            inlineKeyboard.sendRuleInlineKeyboard(update, callback.replace("to_", "").toInt())
                        }
                    }
                    callback.startsWith("book_to_") ->
                        inlineKeyboard.sendBookInlineKeyBoard(update, callback.replace("book_to_", "").toInt())
                    callback.startsWith("book") && !callback.startsWith("book_to_") -> {
                        sender.send(WordManager.getRuleDescriptionById(Integer.parseInt(callback.replace("book", "")))!!.description, chatId)
                        val builder = InlineKeyboardBuilder.create(chatId.toString())
                            .setText(Resource.getStringByKey("STR_18"))
                            .row()
                            .button("↑", "book")
                            .endRow()
                        sender.send(builder.build())
                    }
                    user!!.status === NONE -> {
                        for (rule in WordManager.rules) {
                            if (rule.section == callback) {
                                user!!.status = WAIT_COUNT_OF_WORD
                                user.currRule = rule
                                sender.send(Resource.getStringByKey("STR_6") + rule.name, chatId)
                                sender.send(Resource.getStringByKey("STR_7"), chatId)
                                return
                            }
                        }
                    }
                    user!!.status != NONE -> {
                        sender.delete(chatId, update.callbackQuery.message.messageId)
                        val builder = InlineKeyboardBuilder.create(chatId.toString())
                            .setText(Resource.getStringByKey("STR_9"))
                            .row()
                            .button(Resource.getStringByKey("YES"), "reset_testing")
                            .button(Resource.getStringByKey("NO"), "noreset_testing")
                            .endRow()
                        sender.send(builder.build())
                    }
                }
            }
        }
    }
}