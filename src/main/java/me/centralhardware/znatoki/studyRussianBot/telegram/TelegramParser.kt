package me.centralhardware.znatoki.studyRussianBot.telegram

import org.telegram.telegrambots.meta.api.objects.Update
import me.centralhardware.znatoki.studyRussianBot.WordManager
import me.centralhardware.znatoki.studyRussianBot.objects.User
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus.*
import me.centralhardware.znatoki.studyRussianBot.utils.Redis
import me.centralhardware.znatoki.studyRussianBot.utils.Resource
import java.util.*

/**
 *parse message
 * support type:
 * - text
 * - callback
 * @property sender instance of sender class
 */
class TelegramParser
/**
 * set telegramBot and create InlineKeyboard
 * @param sender object for sending message
 */(private val sender: Sender) {
    private var keyboards: InlineKeyboard = InlineKeyboard(sender)

    val users: HashMap<Long, User> = HashMap<Long, User>()
    /**
     * parse text message
     * support command
     * - /start: start command. also reset current testing.
     * - /help: show help message.
     * - /rules: show choose rule inline menu
     * - /profile: show profile data
     * - /menu: show menu
     * if message have not command: next action depending on status
     * - WAIT_COUNT_OF_WORD: start testing with giving count of word
     * - TESTING: message is answer on word with missing later. if answer is wright - send next word or result
     * if answer is wrong send "неправильно" and send next word. current word moves to the end of the word queue
     * @param update received message
     */
    suspend fun parseText(update: Update) {
        val message = update.message.text
        val chatId = update.message.chatId
        val user = users[chatId]
        when (message) {
            "/start" -> {
                user!!.reset()
                sender.send(Resource.getStringByKey("START_MESSAGE"), update.message.chatId)
                keyboards.sendMenu(chatId!!)
            }
            "/help" -> sender.send(Resource.getStringByKey("HELP_MESSAGE"), chatId)
            "/rules" -> keyboards.sendRuleInlineKeyboard(update, 0)
            "/profile" -> sender.send(user!!.getProfile(), chatId)
            "/menu" -> keyboards.sendMenu(chatId!!)
            else -> {
                when {
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
                                keyboards.sendMenu(chatId!!)
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
                        else -> {}
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
     * - to_$pageNumber - show page of rule
     * @param update received message
     */
    suspend fun parsCallback(update: Update) {
        val callback = update.callbackQuery.data
        val chatId = update.callbackQuery.message.chatId
        val user = users[chatId]
        when (callback) {
            "reset_testing" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                user!!.reset()
                keyboards.sendMenu(chatId!!)
            }
            "noreset_testing" -> {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                sender.send(user!!.words[0].name, chatId)
            }
            "testing" -> keyboards.sendRuleInlineKeyboard(update, 0)
            "profile" -> sender.send(user!!.getProfile(), chatId)
            "help" -> sender.send(Resource.getStringByKey("HELP_MESSAGE"), chatId)
            "menu" -> if (user!!.status !== TESTING && user!!.status !== WAIT_COUNT_OF_WORD) {
                sender.delete(chatId, update.callbackQuery.message.messageId)
                keyboards.sendMenu(chatId!!)
            }
            else -> {
                when {
                    callback.startsWith("to_") -> {
                        if (user!!.status !== WAIT_COUNT_OF_WORD && user!!.status !== TESTING) {
                            keyboards.sendRuleInlineKeyboard(update, callback.replace("to_", "").toInt())
                        }
                    }
                    user!!.status === NONE -> {
                        WordManager.rules.filter { it.section == callback }.forEach {
                            user!!.status = WAIT_COUNT_OF_WORD
                            user.currRule = it
                            sender.send("${Resource.getStringByKey("STR_6")}${it.name}", chatId)
                            sender.send(Resource.getStringByKey("STR_7"), chatId)
                            return
                        }

                    }
                    user!!.status != NONE -> {
                        sender.delete(chatId, update.callbackQuery.message.messageId)
                        sender.send(inlineKeyboard {
                            text(Resource.getStringByKey("STR_9"))
                            chatId(chatId)
                            row {
                                btn(Resource.getStringByKey("YES"), "reset_testing")
                                btn(Resource.getStringByKey("NO"), "noreset_testing")
                            }
                        }.build())
                    }
                }
            }
        }
    }
}