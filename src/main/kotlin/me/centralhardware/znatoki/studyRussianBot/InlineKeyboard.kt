package me.centralhardware.znatoki.studyRussianBot

import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

/** Index of the last page for [ruleCount] rules (0 when there are none). */
fun lastPage(ruleCount: Int): Int = maxOf(0, (ruleCount + RULES_PAGE_SIZE - 1) / RULES_PAGE_SIZE - 1)

object InlineKeyboard {

    suspend fun getRules(requestedPage: Int, chatId: Long) = run {
        val last = lastPage(WordMapper.countRules())
        val page = requestedPage.coerceIn(0, last)
        val rules = WordMapper.getRulePage(page)
        val checked = Progress.checkedRules(chatId)
        inlineKeyboard {
            rules.forEach {
                row { dataButton((if (it.name in checked) "✅" else "") + it.name, it.id.toString()) }
            }
            row {
                // Labels show 1-based page numbers.
                if (page > 0) dataButton("назад - $page", "to_${page - 1}")
                if (page < last) dataButton("следующая - ${page + 2}", "to_${page + 1}")
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
