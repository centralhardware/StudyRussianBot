package me.centralhardware.znatoki.studyRussianBot.telegram

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update
import me.centralhardware.znatoki.studyRussianBot.Config
import me.centralhardware.znatoki.studyRussianBot.WordManager
import me.centralhardware.znatoki.studyRussianBot.objects.Rule
import me.centralhardware.znatoki.studyRussianBot.utils.Redis
import me.centralhardware.znatoki.studyRussianBot.utils.Resource

/**
 * send inlineKeyboardMarkup
 * @property sender instance of sender class
 */
class InlineKeyboard
/**
 * set telegramBot
 *
 * @param sender instance of telegram bot
 */(private val sender: Sender) {
    private val logger = LoggerFactory.getLogger(InlineKeyboard::class.java)

    /**
     * send the user a message from the rule selection menu
     * for user who have demo access limited to three points
     * @param update object with received message
     * @param pageNumber number of page to select
     */
    suspend fun sendRuleInlineKeyboard(update: Update, pageNumber: Int) {
        val chatId: Long
        var message = ""
        if (update.hasCallbackQuery()) {
            chatId = update.callbackQuery.message.chatId!!
        } else {
            chatId = update.message.chatId!!
            message = update.message.text
        }

        logger.info("send inline keyboard rules")
        kotlin.runCatching { sender.delete(chatId, update.callbackQuery.message.messageId) }
        sender.send(inlineKeyboard {
            text(Resource.getStringByKey("STR_8"))
            chatId(chatId)
            if (runBlocking { !Redis.checkRight(chatId) } || !Config.admins.contains(chatId)) {
                for (i in 1..3) {
                    val rule = WordManager.rules[i]

                    row {
                        if (runBlocking { Redis.isCheckRule(chatId, rule.name) }) {
                            btn("✅" + rule.name, rule.section)
                        } else {
                            btn(rule.name, rule.section)
                        }
                    }
                    row { btn(Resource.getStringByKey("STR_24"), "menu") }
                }
            } else {
                WordManager.rules.filter { it.pageNumber == pageNumber }.forEach {
                    row {
                        if (runBlocking { Redis.isCheckRule(chatId, it.name) }) {
                            btn("✅" + it.name, it.section)
                        } else {
                            btn(it.name, it.section)
                        }
                    }
                }
                when{
                    pageNumber == 0 && message != "/rules" -> {

                        row {
                            btn(Resource.getStringByKey("STR_17"), "to_1")
                            btn(Resource.getStringByKey("STR_24"), "menu")
                        }
                    }
                    pageNumber < Rule.maxRulePage -> row {
                        btn(Resource.getStringByKey("STR_18") + " - " + pageNumber, "to_" + (pageNumber - 1))
                        btn(Resource.getStringByKey("STR_17") + " - " + (pageNumber + 2), "to_" + (pageNumber + 1))
                        btn(Resource.getStringByKey("STR_24"), "menu")
                    }
                    pageNumber == Rule.maxRulePage -> row {
                        btn(Resource.getStringByKey("STR_18"), "to_" + (pageNumber - 1))
                        btn(Resource.getStringByKey("STR_24"), "menu")
                    }
                }
            }
        }.build())
    }

    /**
     * send login menu for user who have demo access
     * don't send for user who have full or admin access
     * @param chatId id of user
     */
    suspend fun sendLoginInfo(chatId: Long) {
        if (Redis.checkRight(chatId) || Config.admins.contains(chatId)) {
            sender.send(Resource.getStringByKey("STR_44"), chatId)
        } else {
            sender.send(inlineKeyboard {
                text(Resource.getStringByKey("STR_28"))
                chatId(chatId)
                row { btn(Resource.getStringByKey("STR_29"), "enter_key") }
                row { btn(Resource.getStringByKey("STR_41"), "menu") }
                row { btn(Resource.getStringByKey("STR_26"), "help") }
            }.build())
        }
    }

    /**
     * send main menu
     * for user who have demo access will show button with text "get full access"
     * @param chatId id of user
     */
    suspend fun sendMenu(chatId: Long) {
        logger.info("send inline keyboard menu")

        sender.send(inlineKeyboard {
            text(Resource.getStringByKey("STR_24"))
            chatId(chatId)
            row {
                btn(Resource.getStringByKey("STR_23"), "testing")
                btn(Resource.getStringByKey("STR_25"), "profile")
                btn(Resource.getStringByKey("STR_26"), "help")
            }

            val hasNotRight = runBlocking { !Redis.checkRight(chatId) }

            if (hasNotRight && !Config.admins.contains(chatId)) {
                row { btn(Resource.getStringByKey("STR_36"), "login") }
            }
        }.build())
    }
}