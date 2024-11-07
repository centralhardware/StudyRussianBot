package me.centralhardware.znatoki.studyRussianBot

import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.runBlocking

object InlineKeyboard {

    fun getRules(pageNumber: Int, user: User) = inlineKeyboard {
        WordMapper.getRulePage(pageNumber).forEach {
            row {
                if (runBlocking { Redis.isCheckRule(user.rowId(), it.name) }) {
                    dataButton("✅" + it.name, it.id.toString())
                } else {
                    dataButton(it.name, it.id.toString())
                }
            }
        }

        when {
            pageNumber == 0 -> {
                row {
                    dataButton("следующая", "to_1")
                    dataButton("меню", "menu")
                }
            }
            pageNumber < 4 ->
                row {
                    dataButton("назад - $pageNumber", "to_${pageNumber - 1}")
                    dataButton("следующая-${pageNumber + 2}", "to_${pageNumber + 1}")
                    dataButton("меню", "menu")
                }
            pageNumber == 4 ->
                row {
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
