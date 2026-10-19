package me.centralhardware.znatoki.studyRussianBot

import dev.inmo.micro_utils.common.Warning
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
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
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.sync.withLock
import me.centralhardware.znatoki.studyRussianBot.objects.TelegramUser
import me.centralhardware.znatoki.studyRussianBot.objects.profileText
import java.util.concurrent.ConcurrentHashMap
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus.*
import org.flywaydb.core.Flyway

/** In-progress tests, by user id. Handlers run concurrently. */
val users = ConcurrentHashMap<Long, TelegramUser>()

private val HELP =
    """
    Доступные команды:
     - /start: стартовая команда. Сбрасывает текущее тестирование
     - /help: показать данное сообщение.
     - /rules: показать меню выбора правил для начала тестирования.
     - /profile: показать информацию об профиле.
     - /menu: показать меню.
     Автор - @centralhardware
    """
        .trimIndent()

private fun migrate() {
    Flyway.configure().dataSource(Db.dataSource).load().migrate()
}

suspend fun profile(chatId: Long): String {
    val checked = Progress.checkedRules(chatId)
    return profileText(
        Progress.getRightCount(chatId),
        Progress.getWrongCount(chatId),
        WordMapper.getRules().map { it.name }.filter { it in checked },
    )
}

@OptIn(RiskFeature::class, Warning::class, PreviewFeature::class)
suspend fun main() {
    migrate()

    longPolling("studyRussianBot") {
            setMyCommands(
                BotCommand("start", "Стартовая команда. Сбрасывает текущее тестирование"),
                BotCommand("help", "Справка"),
                BotCommand("rules", "Показать меню выбора правил"),
                BotCommand("profile", "Вывести данные профиля"),
                BotCommand("menu", "показать меню"),
            )
            onCommand("start") {
                val user = getUser(it.from) ?: return@onCommand
                user.lock.withLock { user.reset() }
                send(
                    it.chat,
                    text = "Здравствуйте, это бот для тренировки правил русского языка",
                    replyMarkup = InlineKeyboard.getMenu(),
                )
            }
            onCommand("help") { sendTextMessage(it.chat, HELP) }
            onCommand("rules") {
                val from = it.from ?: return@onCommand
                send(it.chat, text = "правила", replyMarkup = InlineKeyboard.getRules(0, from.rowId()))
            }
            onCommand("profile") {
                val from = it.from ?: return@onCommand
                sendTextMessage(it.chat, profile(from.rowId()))
            }
            onCommand("menu") { send(it.chat, text = "Меню", replyMarkup = InlineKeyboard.getMenu()) }
            onUnhandledCommand { sendTextMessage(it.chat, "команда не распознана") }
            onDataCallbackQuery("reset_testing") {
                answerCallbackQuery(it)
                it.message?.let { m -> deleteMessage(it.from.id, m.messageId) }
                val user = getUser(it.from)!!
                user.lock.withLock { user.reset() }
                send(it.from, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
            }
            onDataCallbackQuery("noreset_testing") {
                answerCallbackQuery(it)
                it.message?.let { m -> deleteMessage(it.from.id, m.messageId) }
                val user = getUser(it.from)!!
                // Pick up where the user was: the current word, or the count
                // question if the test has not started yet.
                val current = user.lock.withLock { user.words.firstOrNull()?.name ?: user.status }
                when (current) {
                    is String -> sendTextMessage(it.from, current)
                    WAIT_COUNT_OF_WORD -> sendTextMessage(it.from, "введите количество слов для тестирования")
                    else -> send(it.from, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
                }
            }
            onDataCallbackQuery("testing") {
                answerCallbackQuery(it)
                send(it.from, text = "правила", replyMarkup = InlineKeyboard.getRules(0, it.from.rowId()))
            }
            onDataCallbackQuery("profile") {
                answerCallbackQuery(it)
                sendTextMessage(it.from, profile(it.from.rowId()))
            }
            onDataCallbackQuery("help") {
                answerCallbackQuery(it)
                sendTextMessage(it.from, HELP)
            }
            onDataCallbackQuery("menu") {
                answerCallbackQuery(it)
                val user = getUser(it.from)!!
                if (user.status !== TESTING && user.status !== WAIT_COUNT_OF_WORD) {
                    it.message?.let { m -> deleteMessage(it.from.id, m.messageId) }
                    send(it.from, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
                }
            }
            onDataCallbackQuery(Regex("to_\\d+")) {
                answerCallbackQuery(it)
                val user = getUser(it.from)!!
                val page = it.data.removePrefix("to_").toIntOrNull() ?: return@onDataCallbackQuery
                val message = it.message ?: return@onDataCallbackQuery
                if (user.status !== WAIT_COUNT_OF_WORD && user.status !== TESTING) {
                    editMessageReplyMarkup(
                        it.from,
                        message.messageId,
                        replyMarkup = InlineKeyboard.getRules(page, it.from.rowId()),
                    )
                }
            }
            onUnhandledDataCallbackQuery {
                answerCallbackQuery(it)
                val user = getUser(it.from)!!
                // Stale or foreign buttons carry data that is not a rule id.
                val ruleId = it.data.toIntOrNull() ?: return@onUnhandledDataCallbackQuery
                if (user.status === NONE) {
                    val rule = WordMapper.getRuleById(ruleId) ?: return@onUnhandledDataCallbackQuery
                    user.lock.withLock {
                        user.status = WAIT_COUNT_OF_WORD
                        user.currRule = rule
                    }
                    sendTextMessage(it.from, "вы выбрали правило: ${rule.name}")
                    sendTextMessage(it.from, "введите количество слов для тестирования")
                } else {
                    it.message?.let { m -> deleteMessage(it.from.id, m.messageId) }
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
            onText(initialFilter = CommonMessageFilterExcludeCommand()) {
                val text = it.text ?: return@onText
                val user = getUser(it.from) ?: return@onText
                user.lock.withLock {
                    when (user.status) {
                        WAIT_COUNT_OF_WORD -> {
                            val count = text.trim().toIntOrNull()
                            if (count == null) {
                                sendTextMessage(it.chat, "введите число")
                                return@onText
                            }
                            if (count <= 0) {
                                sendTextMessage(it.chat, "Введите число больше нуля")
                                return@onText
                            }
                            val words = WordMapper.getWords(user.currRule!!.id)
                            if (count > words.size) {
                                sendTextMessage(it.chat, "к сожалению, у нас нет столько слов")
                                user.reset()
                            } else {
                                user.start(words.shuffled().take(count))
                                sendTextMessage(it.chat, user.words[0].name)
                            }
                        }
                        TESTING -> {
                            val outcome = user.answer(text)
                            if (outcome.correct) {
                                Progress.markWordAsRight(user.chatId, outcome.word)
                                sendTextMessage(it.chat, "правильно")
                            } else {
                                Progress.markWordAsWrong(user.chatId, outcome.word)
                                sendTextMessage(it.chat, "неправильно")
                            }
                            val next = outcome.next
                            if (next == null) {
                                sendTextMessage(it.chat, "вы завершили прохождение правила")
                                sendTextMessage(it.chat, user.getTestingResult())
                                Progress.markRuleAsComplete(user.chatId, user.currRule!!)
                                send(it.chat, text = "Меню", replyMarkup = InlineKeyboard.getMenu())
                                user.reset()
                            } else {
                                sendTextMessage(it.chat, next.name)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
        .second
        .join()
}

fun getUser(user: User?): TelegramUser? =
    user?.let { users.computeIfAbsent(it.rowId()) { id -> TelegramUser(id) } }
