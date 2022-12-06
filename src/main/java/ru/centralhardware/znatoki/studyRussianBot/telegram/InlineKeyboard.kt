package ru.centralhardware.znatoki.studyRussianBot.telegram

import mu.KotlinLogging
import org.telegram.telegrambots.meta.api.objects.Update
import ru.centralhardware.znatoki.studyRussianBot.Config
import ru.centralhardware.znatoki.studyRussianBot.WordManager
import ru.centralhardware.znatoki.studyRussianBot.objects.Rule
import ru.centralhardware.znatoki.studyRussianBot.utils.Redis
import ru.centralhardware.znatoki.studyRussianBot.utils.Resource

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
    private val logger = KotlinLogging.logger { }

    /**
     * send the user a message from the rule selection menu
     * for user who have demo access limited to three points
     * @param update object with received message
     * @param pageNumber number of page to select
     */
    fun sendRuleInlineKeyboard(update: Update, pageNumber: Int) {
        val chatId: Long
        var message = ""
        if (update.hasCallbackQuery()) {
            chatId = update.callbackQuery.message.chatId!!
        } else {
            chatId = update.message.chatId!!
            message = update.message.text
        }
        logger.info("send inline keyboard rules")
        val builder = InlineKeyboardBuilder.create(chatId.toString())
            .setText(Resource.getStringByKey("STR_8"))
        val userId: Long = if (update.hasCallbackQuery()) {
            update.callbackQuery.from.id
        } else {
            update.message.from.id
        }
        if (!(Redis.checkRight(userId) || !Config.admins.contains(userId))) {
            for (i in 1..3) {
                val rule = WordManager.rules[i]
                builder.row()
                if (Redis.isCheckRule(chatId, rule.name)) {
                    builder.button("✅" + rule.name, rule.section)
                } else {
                    builder.button(rule.name, rule.section)
                }
                builder.endRow()
            }
            builder.row()
                .button(Resource.getStringByKey("STR_24"), "menu")
                .endRow()
            sender.delete(chatId, update.callbackQuery.message.messageId)
            sender.send(builder.build())
        } else {
            for (rule in WordManager.rules) {
                if (rule.pageNumber == pageNumber) {
                    builder.row()
                    if (Redis.isCheckRule(chatId, rule.name)) {
                        builder.button("✅" + rule.name, rule.section)
                    } else {
                        builder.button(rule.name, rule.section)
                    }
                    builder.endRow()
                }
            }
            // add buttons to got to other pages
            if (pageNumber == 0) {
                builder.row()
                    .button(Resource.getStringByKey("STR_17"), "to_1")
                    .button(Resource.getStringByKey("STR_24"), "menu")
                    .endRow()
                if (message != "/rules") {
                    sender.delete(chatId, update.callbackQuery.message.messageId)
                    sender.send(builder.build())
                }
            } else if (pageNumber < Rule.maxRulePage) {
                builder.row()
                    .button(Resource.getStringByKey("STR_18") + " - " + pageNumber, "to_" + (pageNumber - 1))
                    .button(Resource.getStringByKey("STR_17") + " - " + (pageNumber + 2), "to_" + (pageNumber + 1))
                    .button(Resource.getStringByKey("STR_24"), "menu")
                    .endRow()
                sender.delete(chatId, update.callbackQuery.message.messageId)
                sender.send(builder.build())
                return
            } else if (pageNumber == Rule.maxRulePage) {
                builder.row()
                    .button(Resource.getStringByKey("STR_18"), "to_" + (pageNumber - 1))
                    .button(Resource.getStringByKey("STR_24"), "menu")
                    .endRow()
                sender.delete(chatId, update.callbackQuery.message.messageId)
                sender.send(builder.build())
                return
            }
        }
    }

    /**
     * send login menu for user who have demo access
     * don't send for user who have full or admin access
     * @param chatId id of user
     */
    fun sendLoginInfo(chatId: Long) {
        if (Redis.checkRight(chatId) || Config.admins.contains(chatId)) {
            sender.send(Resource.getStringByKey("STR_44"), chatId)
        } else {
            val builder = InlineKeyboardBuilder.create(chatId.toString())
                .setText(Resource.getStringByKey("STR_28"))
                .row()
                .button(Resource.getStringByKey("STR_29"), "enter_key")
                .endRow()
                .row()
                .button(Resource.getStringByKey("STR_41"), "menu")
                .endRow()
                .row()
                .button(Resource.getStringByKey("STR_26"), "help")
                .endRow()
            sender.send(builder.build())
        }
    }

    /**
     * send main menu
     * for user who have demo access will show button with text "get full access"
     * @param chatId id of user
     */
    fun sendMenu(chatId: Long) {
        logger.info("send inline keyboard menu")
        val builder = InlineKeyboardBuilder.create(chatId.toString())
            .setText(Resource.getStringByKey("STR_24"))
            .row()
            .button(Resource.getStringByKey("STR_23"), "testing")
            .button(Resource.getStringByKey("STR_25"), "profile")
            .button(Resource.getStringByKey("STR_26"), "help")
            .endRow()
        if (!Redis.checkRight(chatId) && !Config.admins.contains(chatId)) {
            builder.row()
                .button(Resource.getStringByKey("STR_36"), "login")
                .endRow()
        }
        sender.send(builder.build())
    }
}