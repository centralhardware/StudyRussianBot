package me.centralhardware.znatoki.studyRussianBot

import dev.inmo.micro_utils.common.Warning
import dev.inmo.tgbotapi.AppConfig
import dev.inmo.tgbotapi.Trace
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.filters.CommonMessageFilterExcludeCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onUnhandledCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onUnhandledDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.message
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.longPolling
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature
import dev.inmo.tgbotapi.utils.row
import me.centralhardware.znatoki.studyRussianBot.objects.TelegramUser
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus.*

val users: MutableMap<Chat, TelegramUser> = mutableMapOf()

@OptIn(RiskFeature::class, Warning::class, PreviewFeature::class)
suspend fun main() {
    AppConfig.init("studyRussianBot")
    longPolling {
            setMyCommands(
                BotCommand("start", "Стартовая команда. Сбрасывает текущее тестирование"),
                BotCommand("help", "Справка"),
                BotCommand("rules", "Показать меню выбора правил"),
                BotCommand("profile", "Вывести данные профиля"),
                BotCommand("menu", "показать меню"),
            )
            onCommand("start") {
                getUser(it.from).reset()
                send(
                    it.chat,
                    text = "Здравствуйте, это бот для тренировки правил русского языка",
                    replyMarkup = InlineKeyboard.getMenu(),
                )
            }
            onCommand("help") {
                Trace.save("helpCommand", mapOf())
                sendTextMessage(
                    it.chat,
                    """
                Доступные команды: 
                 - /start: стартовая команда. Сбрасывает текущее тестирование
                 - /help: показать данное сообщение.
                 - /rules: показать меню выбора правил для начала тестирования.
                 - /profile: показать информацию об профиле.
                 - /menu: показать меню.
                 Автор - @centralhardware
            """
                        .trimIndent(),
                )
            }
            onCommand("rules") {
                Trace.save("rulesCommand", mapOf())
                send(it.chat, text = "правила", replyMarkup = InlineKeyboard.getRules(0, it.from!!))
            }
            onCommand("profile") {
                Trace.save("profileCommand", mapOf())
                sendTextMessage(it.chat, getUser(it.from).getProfile())
            }
            onCommand("menu") {
                Trace.save("menuCommand", mapOf())
                send(it.chat, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
            }
            onUnhandledCommand {
                Trace.save("unhanldedCommand", mapOf("command" to it.text!!))
                sendTextMessage(it.chat, "команда не распознана")
            }
            onDataCallbackQuery("reset_testing") {
                Trace.save("resetTestingCallback", mapOf())
                deleteMessage(it.from.id, it.message!!.messageId)
                getUser(it.from).reset()
                send(it.from, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
            }
            onDataCallbackQuery("noreset_testing") {
                Trace.save("noresetTestingCallback", mapOf())
                deleteMessage(it.from.id, it.message!!.messageId)
                sendTextMessage(it.from, getUser(it.from).words[0].name)
            }
            onDataCallbackQuery("testing") {
                Trace.save("testingCallback", mapOf())
                send(it.from, text = "правила", replyMarkup = InlineKeyboard.getRules(0, it.from))
            }
            onDataCallbackQuery("profile") {
                Trace.save("profileCallback", mapOf())
                sendTextMessage(it.from, getUser(it.from).getProfile())
            }
            onDataCallbackQuery("help") {
                Trace.save("helpCallback", mapOf())
                sendTextMessage(
                    it.from,
                    """
                Доступные команды: 
                 - /start: стартовая команда. Сбрасывает текущее тестирование
                 - /help: показать данное сообщение.
                 - /rules: показать меню выбора правил для начала тестирования.
                 - /profile: показать информацию об профиле.
                 - /menu: показать меню.
                 Автор - @centralhardware
            """
                        .trimIndent(),
                )
            }
            onDataCallbackQuery("menu") {
                Trace.save("menuCallback", mapOf())
                val user = getUser(it.from)
                if (user.status !== TESTING && user.status !== WAIT_COUNT_OF_WORD) {
                    deleteMessage(it.from.id, it.message!!.messageId)
                    send(it.from, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
                }
            }
            onDataCallbackQuery(Regex("to_\\d+")) {
                val user = getUser(it.from)
                if (user.status !== WAIT_COUNT_OF_WORD && user.status !== TESTING) {
                    editMessageReplyMarkup(
                        it.from,
                        it.message!!.messageId,
                        replyMarkup =
                            InlineKeyboard.getRules(it.data.replace("to_", "").toInt(), it.from),
                    )
                }
            }
            onUnhandledDataCallbackQuery {
                val user = getUser(it.from)
                when {
                    user.status === NONE -> {
                        WordMapper.getRuleById(it.data.toInt())?.let { rule ->
                            getUser(it.from).status = WAIT_COUNT_OF_WORD
                            getUser(it.from).currRule = rule
                            sendTextMessage(it.from, "вы выбрали правило: ${rule.name}")
                            sendTextMessage(it.from, "введите количество слов для тестирования")
                            return@onUnhandledDataCallbackQuery
                        }
                    }
                    user.status !== NONE -> {
                        deleteMessage(it.from.id, it.message!!.messageId)
                        send(
                            it.from,
                            "хотите завершить задание?",
                            replyMarkup =
                                inlineKeyboard {
                                    row {
                                        dataButton("да", "reset_testing")
                                        dataButton("нет", "noreset_testing")
                                    }
                                },
                        )
                    }
                }
            }
            onText(initialFilter = CommonMessageFilterExcludeCommand()) {
                val text = it.text!!
                when (getUser(it.from).status) {
                    WAIT_COUNT_OF_WORD -> {
                        val count = text.toIntOrNull()
                        val user = getUser(it.from)
                        if (count == null) {
                            sendTextMessage(it.chat, "введите число")
                            return@onText
                        }
                        if (count <= 0) {
                            sendTextMessage(it.chat, "Введите число больше нуля")
                            return@onText
                        }

                        user.count = count
                        if (count > user.currRule!!.words.size) {
                            sendTextMessage(it.chat, "к сожалению, у нас нет столько слов")
                            user.status = NONE
                            user.words.clear()
                        } else {
                            user.status = TESTING
                            user.words.addAll(user.currRule!!.getWord(count))
                            sendTextMessage(it.chat, user.words[0].name)
                        }
                    }
                    TESTING -> {
                        val user = getUser(it.from)
                        Trace.save("answer", mapOf())
                        if (user.words[0].answer.equals(text, ignoreCase = true)) {
                            sendTextMessage(it.chat, "правильно")
                            user.words.removeAt(0)
                            if (user.words.isEmpty()) {
                                Trace.save("complete", mapOf())
                                sendTextMessage(it.chat, "вы завершили прохождение правила")
                                sendTextMessage(it.chat, user.getTestingResult())
                                Redis.markRuleAsComplete(user)
                                send(it.chat, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
                                user.reset()
                                return@onText
                            }
                            sendTextMessage(it.chat, user.words[0].name)
                            Redis.markWordAsRight(user)
                        } else {
                            sendTextMessage(it.chat, "неправильно")
                            Redis.markWordAsWrong(user)
                            val temp = user.words[0]
                            user.words.removeAt(0)
                            user.words.add(temp)
                            user.wrongWords.add(temp)
                            sendTextMessage(it.chat, user.words[0].name)
                        }
                    }
                    else -> {}
                }
            }
        }
        .second
        .join()
}

fun getUser(chat: User?): TelegramUser =
    chat?.let {
        users.putIfAbsent(it, TelegramUser(chat.rowId()))
        users[it]
    }!!
