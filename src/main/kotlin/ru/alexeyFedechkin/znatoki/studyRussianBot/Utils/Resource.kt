package ru.alexeyFedechkin.znatoki.studyRussianBot.Utils

import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import ru.alexeyFedechkin.znatoki.studyRussianBot.Config
import java.io.IOException
import java.io.StringWriter
import java.util.*

object Resource {
    private val logger = KotlinLogging.logger {  }
    private val resourceBundle = ResourceBundle.getBundle("string")

    /**
     * get string from resource bundle file
     * @param key key of string constant
     * @return string by giving constant if value is exist
     */
    fun getStringByKey(key: String): String {
        if (Config.isTesting) {
            logger.info("get string from resource bundle string.properties. key = $key")
        }
        return resourceBundle.getString(key)
    }

    /**
     * get string from resource folder file
     *
     * @param fileName name of file to search
     * @return string from file that placed in resource folder
     */
    @Throws(IOException::class)
    fun getStringFromResources(fileName: String): String {
        val classLoader = ClassLoader.getSystemClassLoader()
        val inputStream = classLoader.getResourceAsStream(fileName)
        val writer = StringWriter()
        IOUtils.copy(Objects.requireNonNull(inputStream), writer)
        return writer.toString()
    }
}