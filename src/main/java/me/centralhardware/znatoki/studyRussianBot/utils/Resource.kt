package me.centralhardware.znatoki.studyRussianBot.utils

import java.io.File
import java.util.*

object Resource {
    fun loadFromPath(path: String): String {
        return File(path).readText();
    }
}