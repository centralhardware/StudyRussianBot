package me.centralhardware.znatoki.studyRussianBot

import dev.inmo.tgbotapi.types.chat.User

fun User.rowId() = id.chatId.long