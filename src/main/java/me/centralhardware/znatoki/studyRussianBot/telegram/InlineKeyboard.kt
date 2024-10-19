package me.centralhardware.znatoki.studyRussianBot.telegram

import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.buttons.inline.dataInlineButton
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.runBlocking
import me.centralhardware.znatoki.studyRussianBot.WordManager
import me.centralhardware.znatoki.studyRussianBot.objects.Rule
import me.centralhardware.znatoki.studyRussianBot.rowId
import me.centralhardware.znatoki.studyRussianBot.utils.Redis

object InlineKeyboard {

    fun getRules(pageNumber: Int, user: User) = inlineKeyboard {
        WordManager.rules.filter { it.pageNumber == pageNumber }.forEach {
            row {
                if (runBlocking { Redis.isCheckRule(user.rowId(), it.name) }) {
                    dataButton("✅" + it.name, it.section)
                } else {
                    dataButton(it.name, it.section)
                }
            }
        }

        when{
            pageNumber == 0 -> {
                row {
                    dataButton("следующая", "to_1")
                    dataButton("меню", "menu")
                }
            }
            pageNumber < Rule.maxRulePage -> row {
                dataButton("назад - $pageNumber", "to_${pageNumber - 1}")
                dataButton("следующая-${pageNumber + 2}", "to_${pageNumber + 1}")
                dataButton("меню", "menu")
            }
            pageNumber == Rule.maxRulePage -> row {
                dataButton("назад", "to_${pageNumber - 1}")
                dataButton("меню", "menu")
            }
        }
    }

    fun getMenu() = inlineKeyboard {
        row {
            dataButton("тестирование", "testing")
            dataButton("профиль", "profile")
            dataButton("справка", "help")
        }
    }
}