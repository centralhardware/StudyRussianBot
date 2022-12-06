package ru.centralhardware.znatoki.studyRussianBot.utils

import java.io.File
import java.util.*

/**
 *provide access to resource bundle
 */
object Resource {
    private val resourceBundle = ResourceBundle.getBundle("string")

    /**
     * get string from resource bundle file
     * @param key key of string constant
     * @return string by giving constant if value is exist
     */
    fun getStringByKey(key: String): String {
        return resourceBundle.getString(key)
    }

    /**
     * get string from file
     * @param path path to file
     * @return string from file
     */
    fun loadFromPath(path: String): String {
        return File(path).readText();
    }
}